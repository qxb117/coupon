package com.qxb.coupon.service;

import com.qxb.coupon.entity.CouponTemplate;

/**
 * @author qiuxiaobin
 * @date 2020年 02月 08日 19:21
 * @description 异步服务接口定义
 */
public interface IAsyncService {
    /**
     * 根据模板异步的创建优惠券码
     * @param template {@link CouponTemplate} 优惠券模板实体
     * */
    void asyncConstructCouponByTemplate(CouponTemplate template);
}
