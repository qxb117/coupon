package com.qxb.coupon.service.impl;

import com.alibaba.fastjson.JSON;
import com.qxb.coupon.constant.Constant;
import com.qxb.coupon.constant.CouponStatus;
import com.qxb.coupon.entity.Coupon;
import com.qxb.coupon.exception.CouponException;
import com.qxb.coupon.service.IRedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author qiuxiaobin
 * @date 2020-02-11 15:16
 * @description Redis相关的操作服务接口实现
 */
@Slf4j
@Service
public class RedisServiceImpl implements IRedisService {

    private final StringRedisTemplate redisTemplate;

    @Autowired
    public RedisServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 根据 userId 和状态找到缓存的优惠券列表数据
     * @param userId 用户id
     * @param status 优惠券状态 {@link com.qxb.coupon.constant.CouponStatus}
     * @return {@link Coupon}s,注意，可能会返回null，代表从未有过记录
     */
    @Override
    public List<Coupon> getCacheCoupons(long userId, Integer status) {

        log.info("Get Coupon From Cache: {},{}", userId, status);
        String redisKey = status2RedisKey(status, userId);

        List<String> couponStrs = redisTemplate.opsForHash().values(redisKey)
                .stream()
                .map(o -> Objects.toString(o,null))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(couponStrs)){
            saveEmptyCouponListToCache(userId, Collections.singletonList(status));
            return Collections.emptyList();
        }
        return couponStrs.stream().map(cs -> JSON.parseObject(cs,Coupon.class)).collect(Collectors.toList());
    }

    /**
     * 保存一些空的优惠券列表到缓存中
     * 目的：避免缓存穿透
     * @param userId 用户id
     * @param status 优惠券状态列表
     */
    @Override
    @SuppressWarnings("all")
    public void saveEmptyCouponListToCache(Long userId, List<Integer> status) {

        log.info("Save Empty List To Cache For User:{},Status:{}", userId, JSON.toJSONString(status));

        Map<String, String> invalidCoupon = new HashMap<>();
        invalidCoupon.put("-1", JSON.toJSONString(Coupon.invalidCoupon()));

        /**
         * 用户优惠券缓存信息
         * KV
         * K: status ->redisKey
         * V: {coupon_id：序列化的 Coupon}
         * */

        // 使用 SessioCallBcak 把数据命令放入到 Redis 的 pipeline
        SessionCallback<Object> sessionCallback = new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {

                status.forEach(s ->{
                    String redisKey = status2RedisKey(s, userId);
                    redisOperations.opsForHash().putAll(redisKey,invalidCoupon);
                });
                return null;
            }
        };
        log.info("Pipeline Exe Result:{}",JSON.toJSONString(redisTemplate.executePipelined(sessionCallback)));
    }

    /**
     * 尝试从 cache 中获取一个优惠券码
     * @param templateId 模板id
     * @return 优惠券码
     * */
    @Override
    public String tryToAcquireCouponCodeFromCahce(Integer templateId) {
        String redisKey = String.format("%s%s", Constant.RedisPrefix.COUPON_TEMPLATE, templateId.toString());

        /** 因为优惠券码不存在顺序关系，左边 pop 或右边 pop,没有影响 */
        String couponCode = redisTemplate.opsForList().leftPop(redisKey);

        log.info("Acquire Coupon Code: {}, {}, {}",templateId,redisKey,couponCode);
        return couponCode;
    }

    /**
     * 将优惠券保存到 cache 中
     * @param userId 用户id
     * @param coupons {@link Coupon}s
     * @param status 优惠券状态
     * @return 保存成功的个数
     * */
    @Override
    public Integer addCouponToCache(Long userId, List<Coupon> coupons, Integer status) throws CouponException {

        log.info("Add Coupon TO Cache: {}, {}, {}", userId, JSON.toJSONString(coupons), status);

        Integer result = -1;
        CouponStatus couponStatus = CouponStatus.of(status);
        switch (couponStatus){
            case USABLE:
                result = addCouponTiCacheForUsable(userId, coupons);
                break;
            case USED:
                result = addCouponToCacheForUsed(userId, coupons);
                break;
            case EXPIRED:
                result = addCouponToCacheForExpired(userId, coupons);
                break;
        }

        return result;
    }

    /**
    * 新增加优惠券到 Cache 中
    * */
    private Integer addCouponTiCacheForUsable(Long userId,List<Coupon> coupons){
        // 如果 status 是USABLE, 代表是新增加的优惠券
        //只会影响到一个 Cache: USER_COUPON_USABLE
        log.debug("Add Coupon To Cache For Usable");

        Map<String, String> needCacheObject = new HashMap<>();
        coupons.forEach(c ->needCacheObject.put(
                c.getId().toString(),
                JSON.toJSONString(c)
        ));
        String redisKey = status2RedisKey(CouponStatus.USABLE.getCode(), userId);
        redisTemplate.opsForHash().putAll(redisKey,needCacheObject);
        log.info("Add {} Coupons To Cache: {}, {}", needCacheObject.size(), userId, redisKey);

        //设置过期时间
        redisTemplate.expire(
                redisKey,
                getRandomExpirationTime(1, 2),
                TimeUnit.SECONDS
        );
        return needCacheObject.size();
    }

    /**
     * 将已使用的优惠券加入到 cache 中
     * */
    @SuppressWarnings("all")
    private Integer addCouponToCacheForUsed(Long userId, List<Coupon> coupons) throws CouponException{

        // 如果 status 是 USED, 代表用户操作是使用当前优惠券，影响到两个 Cache
        // USABLE,USED

        log.debug("Add Coupon To Cache For Used");

        Map<String, String> needCacheForUsed = new HashMap<>(coupons.size());

        String redisKeyForUsable = status2RedisKey(
                CouponStatus.USABLE.getCode(), userId
        );
        String redisKeyForUsed = status2RedisKey(
                CouponStatus.USED.getCode(), userId
        );

        // 获取当前用户可用的优惠券
        List<Coupon> curUsableCoupons = getCacheCoupons(userId, CouponStatus.USABLE.getCode());

        // 当前可用的优惠券个数一定是大于1的
        assert curUsableCoupons.size() > coupons.size();

        coupons.forEach(
                c -> needCacheForUsed.put(
                        c.getId().toString(),
                        JSON.toJSONString(c)
                )
        );

        // 校验当前的优惠券参数是否与 Cache 中的匹配
        List<Integer> curUsableIds = curUsableCoupons.stream().map(Coupon::getId).collect(Collectors.toList());
        List<Integer> paramIds = coupons.stream().map(Coupon::getId).collect(Collectors.toList());

        if (!CollectionUtils.isSubCollection(paramIds, curUsableIds)) {
            log.error("CurCoupons Is Not Equal To Cache：{},{},{}",
                    userId, JSON.toJSONString(curUsableIds), JSON.toJSONString(paramIds));
            throw new CouponException("CurCoupons Is Not Equal To Cache!");
        }

        List<String> needCleanKey = paramIds.stream().map(i -> i.toString()).collect(Collectors.toList());
        SessionCallback<Object> sessionCallback = new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {

                //1.已使用的优惠券 Cache 缓存添加
                redisOperations.opsForHash().putAll(redisKeyForUsed,needCacheForUsed);
                //2.可用的优惠券 Cache 缓存清理
                redisOperations.opsForHash().delete(redisKeyForUsable,needCleanKey.toArray());
                //3.重置过期时间
                redisOperations.expire(
                        redisKeyForUsable,
                        getRandomExpirationTime(1, 2),
                        TimeUnit.SECONDS
                );
                redisOperations.expire(
                        redisKeyForUsed,
                        getRandomExpirationTime(1, 2),
                        TimeUnit.SECONDS
                );
                return null;
            }
        };
        log.info("Pipeline Exe Redis: {}", JSON.toJSONString(redisTemplate.executePipelined(sessionCallback)));
        return coupons.size();
    }

    /**
     * 将过期优惠券加入到 Cache 中
     * */
    private Integer addCouponToCacheForExpired(Long userId, List<Coupon> coupons) throws CouponException {

        // status 是 EXPIRED,代表是已有的优惠券过期了，影响到两个Cache
        // USABLE, EXPIRED

        log.debug("Add Coupon To Cache For Expired");

        // 最终需要保存的 Cache
        Map<String, String> needCacheForExpired = new HashMap<>(coupons.size());

        String redisKeyForUsable = status2RedisKey(
                CouponStatus.USABLE.getCode(), userId
        );
        String redisKeyForExpired = status2RedisKey(
                CouponStatus.EXPIRED.getCode(), userId
        );

        List<Coupon> curUsableCoupons = getCacheCoupons(userId, CouponStatus.USABLE.getCode());

        // 当前的可用的优惠券个数一定是大于1的
        assert curUsableCoupons.size() > coupons.size();

        coupons.forEach(c -> needCacheForExpired.put(
                c.getId().toString(),
                JSON.toJSONString(c)
        ));

        // 校验当前的优惠券参数是否与 Cache 中的匹配
        List<Integer> curUsableIds = curUsableCoupons.stream().map(Coupon::getId).collect(Collectors.toList());
        List<Integer> paramIds = coupons.stream().map(Coupon::getId).collect(Collectors.toList());
        if (CollectionUtils.isSubCollection(paramIds, curUsableIds)) {
            log.error("CurCoupons Is Not Equal To Cache：{},{},{}",
                    userId, JSON.toJSONString(curUsableIds), JSON.toJSONString(paramIds));
            throw new CouponException("CurCoupons Is Not Equal To Cache!");
        }

        List<String> needCleanKey = paramIds.stream().map(i -> i.toString()).collect(Collectors.toList());
        SessionCallback<Object> sessionCallback = new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                // 1.已过期的优惠券缓存起来
                redisOperations.opsForHash().putAll(redisKeyForExpired, needCacheForExpired);
                // 2.可用的优惠券 Cache 需要清理
                redisOperations.opsForHash().delete(redisKeyForUsable, needCleanKey.toArray());
                // 3.重置过期时间
                redisOperations.expire(
                        redisKeyForUsable,
                        getRandomExpirationTime(1, 2),
                        TimeUnit.SECONDS
                );
                redisOperations.expire(
                        redisKeyForExpired,
                        getRandomExpirationTime(1, 2),
                        TimeUnit.SECONDS
                );
                return null;
            }
        };
        log.info("Pipeline Exe Redis: {}", JSON.toJSONString(redisTemplate.executePipelined(sessionCallback)));
        return coupons.size();
    }

    /**
     * 根据 status 获取到对应的 RedisKey
     * */
    private String status2RedisKey(Integer status, Long userId) {

        String redisKey = null;
        CouponStatus couponStatus = CouponStatus.of(status);

        switch (couponStatus){
            case USABLE:
                redisKey = String.format("%s%s", Constant.RedisPrefix.USER_COUPON_USABLE,userId);
                break;
            case USED:
                redisKey = String.format("%s%s", Constant.RedisPrefix.USER_COUPON_USED, userId);
                break;
            case EXPIRED:
                redisKey = String.format("%s%s", Constant.RedisPrefix.USER_COUPON_EXPIRED, userId);
                break;
        }
        return redisKey;
    }

    /**
     * 获取一个随机的过期时间
     * 缓存雪崩：Key 在同一时间失效
     * @param min 最小的小时数
     * @param max 最大的小时数
     * @return 返回[min,max]之间的随机秒数
     * */
    private Long getRandomExpirationTime(Integer min,Integer max){

        return RandomUtils.nextLong(
                min * 60 * 60,
                max * 60 * 60
        );
    }
}
