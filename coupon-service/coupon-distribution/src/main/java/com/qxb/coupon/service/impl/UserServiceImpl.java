package com.qxb.coupon.service.impl;

import com.alibaba.fastjson.JSON;
import com.qxb.coupon.constant.Constant;
import com.qxb.coupon.constant.CouponStatus;
import com.qxb.coupon.dao.CouponDao;
import com.qxb.coupon.entity.Coupon;
import com.qxb.coupon.exception.CouponException;
import com.qxb.coupon.feign.SettlementClient;
import com.qxb.coupon.feign.TemplateClient;
import com.qxb.coupon.service.IRedisService;
import com.qxb.coupon.service.IUserService;
import com.qxb.coupon.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.NotWritablePropertyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author qiuxiaobin
 * @date 2020-02-13 16:54
 * @description 用户服务相关的接口实现
 * 核心思想：所有的操作过程，状态都保存在 redis 中，并通过 kafka 把消息传递到 mysql 中
 */
@Slf4j
@Service
public class UserServiceImpl implements IUserService {
    /** coupom Dao */
    private final CouponDao couponDao;
    /** redis 服务 */
    private final IRedisService redisService;
    /** 模板微服务客户端 */
    private final TemplateClient templateClient;
    /** 结算微服务客户端 */
    private final SettlementClient settlementClient;
    /** kafka 客户端 */
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public UserServiceImpl(CouponDao couponDao, IRedisService redisService, TemplateClient templateClient,
                           SettlementClient settlementClient, KafkaTemplate<String, String> kafkaTemplate) {
        this.couponDao = couponDao;
        this.redisService = redisService;
        this.templateClient = templateClient;
        this.settlementClient = settlementClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * 根据用户 id 和状态查询优惠券记录
     * @param userId 用户 id
     * @param status 优惠券状态
     * @return {@link Coupon}s
     */
    @Override
    public List<Coupon> findCouponByStatus(Long userId, Integer status) throws CouponException {

        List<Coupon> curCache = redisService.getCacheCoupons(userId, status);
        List<Coupon> preTarget;
        if (CollectionUtils.isNotEmpty(curCache)) {
            log.debug("Coupon cache is not empty:{},{}", userId, status);
            preTarget = curCache;
        }else {
            log.debug("Coupon cache is empty,get coupon from DB:{},{}", userId, status);
            List<Coupon> dbCoupons = couponDao.findAllByUserIdAndStatus(userId, CouponStatus.of(status));
            // 如果在数据库中没有记录，直接返回就可以，cache 中会有一张无效的优惠券
            if (CollectionUtils.isEmpty(dbCoupons)) {
                log.debug("User do not have coupon: {},{}", userId, status);
                return dbCoupons;
            }
            //填充 dbCoupons 的 TemplateSdk 字段
            Map<Integer, CouponTemplateSDK> id2TemplateSDk = templateClient.findIds2TemplateSDK(
                    dbCoupons.stream().map(Coupon::getTemplateId).collect(Collectors.toList())
            ).getData();
            dbCoupons.forEach(dbc ->dbc.setTemplateSDK(id2TemplateSDk.get(dbc.getTemplateId())));

            // 数据库中存在记录
            preTarget = dbCoupons;
            // 将数据库的数据同步到 cache 中
            redisService.addCouponToCache(userId, preTarget, status);
        }
        // 剔除无效的优惠券
        preTarget = preTarget.stream().filter(p -> p.getId() != -1).collect(Collectors.toList());
        // 如果当前优惠券包含可用的优惠券，还对已过期的优惠券做延迟处理
        if (CouponStatus.of(status) == CouponStatus.USABLE) {
            CouponClassify classify = CouponClassify.classify(preTarget);
            // 如果已过期的状态不为空，需要做延迟处理
            if (CollectionUtils.isNotEmpty(classify.getExpired())) {
                log.info("Add Expired Coupons To Cache From FindCouponByStatus:{},{}", userId, status);
                redisService.addCouponToCache(userId, classify.getExpired(), CouponStatus.EXPIRED.getCode());
                // 发送 kafka 中做异步处理
                kafkaTemplate.send(Constant.TOPIC, JSON.toJSONString(new CouponKafkaMessage(
                        CouponStatus.EXPIRED.getCode(),
                        classify.getExpired().stream().map(Coupon::getId).collect(Collectors.toList()))
                ));
            }
            return classify.getUsable();
        }
        return preTarget;
    }

    /**
     * 根据用户 id 查找当前可以领取的优惠券模板
     * @param userId 用户 id
     * @return {@link CouponTemplateSDK}s
     */
    @Override
    public List<CouponTemplateSDK> findAvailableTemplate(Long userId) throws CouponException {
        Long curTime = new Date().getTime();
        List<CouponTemplateSDK> couponTemplateSDKS = templateClient.findAllUsableTemplate().getData();
        log.debug("Find all template （from templateClient） count：{}", couponTemplateSDKS.size());

        // 过滤过期的优惠券
        couponTemplateSDKS.stream().filter(c -> c.getRule().getExpiration()
                .getDeadline() > curTime).collect(Collectors.toList());
        log.info("Find Usable Template Count:{}",couponTemplateSDKS.size());

        // Key 是 TemplateId
        //value 中的 left 是 Template limitation(限制的领取次数), right 是优惠券模板
        Map<Integer, Pair<Integer, CouponTemplateSDK>> limit2Template = new HashMap<>(couponTemplateSDKS.size());
        couponTemplateSDKS.forEach(
                t -> limit2Template.put(
                        t.getId(),
                        Pair.of(t.getRule().getLimitation(), t)
                )
        );
        List<CouponTemplateSDK> result = new ArrayList<>(limit2Template.size());
        List<Coupon> userUsableCoupons = findCouponByStatus(userId, CouponStatus.USABLE.getCode());
        log.debug("current user has usable coupons:{},{}", userId, userUsableCoupons.size());

        // Key 是 TemplateId
        Map<Integer, List<Coupon>> templateId2Coupons = userUsableCoupons.stream()
                .collect(Collectors.groupingBy(Coupon::getTemplateId));

        // 根据Template 的 Rule 判断是否可以领取优惠券
        limit2Template.forEach((k,v) -> {
            int limittation = v.getKey();
            CouponTemplateSDK templateSDK = v.getRight();
            if (templateId2Coupons.containsKey(k) && templateId2Coupons.get(k).size() >= limittation) {
                return;
            }
            result.add(templateSDK);
        });
        return result;
    }

    /**
     * 用户领取优惠券
     * 1. 从 TemplateClient 中拿到对应的优惠券，并检查是否过期
     * 2. 根据 limitation 判断用户是否能领取
     * 3. save to db
     * 4. 填充 TemplateSDK
     * 5. save to cache
     * @param request {@link AcquireTemplateRequest}
     * @return {@link Coupon}
     */
    @Override
    public Coupon acquireTemplate(AcquireTemplateRequest request) throws CouponException {

        Map<Integer, CouponTemplateSDK> id2Template = templateClient.findIds2TemplateSDK(
                Collections.singleton(request.getTemplateSDK().getId())
        ).getData();

        //优惠券模板是需要存在的
        if (id2Template.size() <= 0) {
            log.error("Can not acquire template from templateClient:{}", request.getTemplateSDK().getId());
            throw new CouponException("Can not acquire template from templateClient");
        }

        // 判断用户是否能领取优惠券
        List<Coupon> userUsableTemplate = findCouponByStatus(
                request.getUserId(), CouponStatus.USABLE.getCode()
        );
        Map<Integer, List<Coupon>> templateId2Coupons = userUsableTemplate
                .stream().collect(Collectors.groupingBy(Coupon::getTemplateId));

        if (templateId2Coupons.containsKey(request.getTemplateSDK().getId()) &&
                templateId2Coupons.get(request.getTemplateSDK().getId()).size()
                        >= request.getTemplateSDK().getRule().getLimitation()) {
            log.error("Exceed Template Assign limitation:{}", request.getTemplateSDK().getId());
            throw new CouponException("Exceed Template Assign limitation");
        }

        // 尝试获取优惠券码
        String couponCode = redisService.tryToAcquireCouponCodeFromCahce(request.getTemplateSDK().getId());
        if (StringUtils.isEmpty(couponCode)) {
            log.error("Can not acquire coupon code:{}", request.getTemplateSDK().getId());
            throw new CouponException("Can not acquire coupon code");
        }

        Coupon newCoupon = new Coupon(
                request.getTemplateSDK().getId(), request.getUserId(),
                couponCode, CouponStatus.USABLE
        );
        newCoupon = couponDao.save(newCoupon);

        // 填充 coupon 对象的 CouponTemplateSDK，一定要在放入缓存之前填充
        newCoupon.setTemplateSDK(request.getTemplateSDK());

        // 放入缓存
        redisService.addCouponToCache(
                request.getUserId(),
                Collections.singletonList(newCoupon),
                CouponStatus.USABLE.getCode()
        );
        return newCoupon;
    }

    /**
     * 结算（核销）优惠券
     * 这里需要注意，规则相关的处理是交由 settlement 微服务处理的，这里仅仅只是做一个业务处理（校验过程）
     * @param info {@link SettlementInfo}
     * @return {@link SettlementInfo}
     */
    @Override
    public SettlementInfo settlement(SettlementInfo info) throws CouponException {

        // 当没有传递优惠券信息时，直接返回商品总价
        List<SettlementInfo.CouponAndTemplateInfo> ctInfos = info.getCouponAndTemplateInfos();
        if (CollectionUtils.isEmpty(ctInfos)) {
            log.info("Empty coupon from settlement");
            double goodsSum = 0.0;
            for (GoodsInfo gi : info.getGoodsInfos()) {
                goodsSum += gi.getPrice() * gi.getCount();
            }
            // 没有优惠券即不需要优惠券的核销，settlement里的字段不需要修改
            info.setCost(retain2Decimals(goodsSum));
            return info;
        }

        // 校验优惠券是否时用户自己的
        List<Coupon> coupons = findCouponByStatus(info.getUserId(), CouponStatus.USABLE.getCode());
        Map<Integer, Coupon> id2Coupon = coupons.stream().collect(
                Collectors.toMap(
                        Coupon::getId,
                        Function.identity()
                )
        );
        if (MapUtils.isEmpty(id2Coupon) || !CollectionUtils.isSubCollection(
                ctInfos.stream().map(SettlementInfo.CouponAndTemplateInfo::getId).collect(Collectors.toList()),
                id2Coupon.keySet())
        ) {
            log.info("{}", id2Coupon.keySet());
            log.info("{}", ctInfos.stream().map(SettlementInfo.CouponAndTemplateInfo::getId).collect(Collectors.toList()));
            log.error("User coupon has some problem,It is not subCollection of Coupons");
            throw new CouponException("User coupon has some problem,It is not subCollection of Coupons");
        }

        log.info("Current Settlement Coupon Is User's:{}", ctInfos.size());
        List<Coupon> settleCoupons = new ArrayList<>(ctInfos.size());
        ctInfos.forEach(ct -> settleCoupons.add(id2Coupon.get(ct.getId())));

        // 通过调用结算微服务，结算金额
        SettlementInfo processendInfo = settlementClient.couponRule(info).getData();

        if (processendInfo.getEmploy() && CollectionUtils.isNotEmpty(processendInfo.getCouponAndTemplateInfos())) {
            log.info("Settle user coupon:{},{}", info.getUserId(), JSON.toJSONString(settleCoupons));
            // 更新缓存
            redisService.addCouponToCache(
                    info.getUserId(),
                    settleCoupons,
                    CouponStatus.USED.getCode()
            );
            // 更新DB
            kafkaTemplate.send(
                    Constant.TOPIC,
                    JSON.toJSONString(new CouponKafkaMessage(
                            CouponStatus.USED.getCode(),
                            settleCoupons.stream().map(Coupon::getId).collect(Collectors.toList())
                    ))
            );

        }

        return processendInfo;
    }

    /** 保留两位小数 */
    private Double retain2Decimals(Double value) {

        return new BigDecimal(value)
                .setScale(2,BigDecimal.ROUND_HALF_UP)//保留两位小数，四舍五入
                .doubleValue();
    }
}
