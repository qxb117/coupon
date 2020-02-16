package com.qxb.coupon.service;

import com.qxb.coupon.entity.Coupon;
import com.qxb.coupon.exception.CouponException;

import java.util.List;

/**
 * @author qiuxiaobin
 * @date 2020-02-11 14:48
 * @description Redis相关的操作服务接口定义
 * 1.用户的三个状态优惠券 cache 相关操作
 * 2.优惠券模板生成的优惠券码 cache 操作
 */
public interface IRedisService {

    /**
     * 根据 userId 和状态找到缓存的优惠券列表数据
     *
     * @param userId 用户id
     * @param status 优惠券状态 {@link com.qxb.coupon.constant.CouponStatus}
     * @return {@link Coupon}s,注意，可能会返回null，代表从未有过记录
     */
    List<Coupon> getCacheCoupons(long userId, Integer status);

    /**
     * 保存一些空的优惠券列表到缓存中
     * @param userId 用户id
     * @param status 优惠券状态列表
     */
    void saveEmptyCouponListToCache(Long userId, List<Integer> status);

    /**
     * 尝试从 cache 中获取一个优惠券码
     * @param templateId 模板id
     * @return 优惠券码
     * */
    String tryToAcquireCouponCodeFromCahce(Integer templateId);

    /**
     * 将优惠券保存到 cache 中
     * @param userId 用户id
     * @param coupons {@link Coupon}s
     * @param status 优惠券状态
     * @return 保存成功的个数
     * */
    Integer addCouponToCache(Long userId, List<Coupon> coupons, Integer status) throws CouponException;
}
