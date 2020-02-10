package com.qxb.coupon.service.impl;

import com.google.common.base.Stopwatch;
import com.qxb.coupon.constant.Constant;
import com.qxb.coupon.dao.CouponTemplateDao;
import com.qxb.coupon.entity.CouponTemplate;
import com.qxb.coupon.service.IAsyncService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.RandomStringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author qiuxiaobin
 * @date 2020-02-08 20:37
 * @description 异步服务接口实现
 */
@Slf4j
@Service
public class AsyncServiceImpl implements IAsyncService {

    /** CouponTemplate Dao */
    private final CouponTemplateDao templateDao;
    /** 注入 redis 模板类 */
    private final StringRedisTemplate redisTemplate;

    @Autowired
    public AsyncServiceImpl(CouponTemplateDao templateDao, StringRedisTemplate redisTemplate) {
        this.templateDao = templateDao;
        this.redisTemplate = redisTemplate;
    }

    /**
    * 根据模板异步的创建优惠券码
     * @param template {@link CouponTemplate} 优惠券模板实体
    * */
    @Async("getAsyncExecutor")
    @Override
    @SuppressWarnings("all")
    public void asyncConstructCouponByTemplate(CouponTemplate template) {

        Stopwatch watch = Stopwatch.createStarted();
        Set<String> couponCodes = buildCouponCode(template);

        //coupon_template_code_1
        String redisKey = String.format("%s%s", Constant.RedisPrefix.COUPON_TEMPLATE,template.getId().toString());
        log.info("Push CouponCode to Redis：{}",redisTemplate.opsForList().rightPushAll(redisKey,couponCodes));
        template.setAvaillable(true);
        templateDao.save(template);

        watch.stop();
        log.info("Construct CouponCode By Template Cost：{}ms",watch.elapsed(TimeUnit.MILLISECONDS));
        //TODO 发送短信或者邮件通知运营人员优惠券模板已经可用（这里用日志代替一下）
        log.info("CouponTemplate({}) is Availlable!",template.getId());
    }

    /**
     * 构造优惠券码
     * 优惠券码（对应每一张优惠券，18位）
     *  前四位：产品线 + 类型
     *  中间六位：日期随机（200220）
     *  后八位：0 ~ 9 随机数构成
     * @param template {@link CouponTemplate} 实体类
     * @return Set<String> 与 template.count 相同个数的优惠券码
     * */
    @SuppressWarnings("all")
    private Set<String> buildCouponCode(CouponTemplate template){

        Stopwatch watch = Stopwatch.createStarted();//开始计时器

        Set<String> result = new HashSet<>(template.getCount());

        //前四位
        String prefix4 = template.getProductLine().getCode().toString()
                + template.getCategory().getCode().toString();
        //中间六位
        String date = new SimpleDateFormat("yyMMdd").format(template.getCreateTime());
        //后八位
        for (int i = 0; i != template.getCount(); i++) {
            result.add(prefix4 + buildCouponCodeSuffix14(date));
        }

        //防止出现重复随机数导致数量不够
        while (result.size() < template.getCount()){
            result.add(prefix4 + buildCouponCodeSuffix14(date));
        }

        assert result.size() == template.getCount();

        watch.stop();
        log.info("Build CouponCode Cost：{}ms",watch.elapsed(TimeUnit.MILLISECONDS));
        return result;
    }

    /**
     * 构造优惠券码的后 14位
     * @param date 创建优惠券的日期
     * @return 14位优惠券码
     * */
    private String buildCouponCodeSuffix14(String date){

        char[] bases = new char[]{'1','2','3','4','5','6','7','8','9'};

        //中间六位
        List<Character> chars = date.chars().mapToObj(e -> (char) e).collect(Collectors.toList());//将int强转位char，并且收集成list
        Collections.shuffle(chars);//洗牌算法，随机
        String mid6 = chars.stream().map(Objects::toString).collect(Collectors.joining());//把char变成String

        //后八位
        String suffix8 = RandomStringUtils.random(1,bases) //固定第一位是bases集合中的数字
                + RandomStringUtils.randomNumeric(7);//随机7位数字

        return mid6 + suffix8;
    }
}
