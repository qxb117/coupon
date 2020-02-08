package com.qxb.coupon.service;

import com.qxb.coupon.entity.CouponTemplate;
import com.qxb.coupon.exception.CouponException;
import com.qxb.coupon.vo.TemplateRequest;

/**
 * @author qiuxiaobin
 * @create 2020-02-08 17:33
 * @description 构建优惠券模板接口定义
 */
public interface IBuildTemplateService {

    /**
     * 创建优惠券模板
     * @param templateRequest {@link TemplateRequest} 模板信息请求对象
     * @return {@link CouponTemplate} 优惠券模板实体
     * */
    CouponTemplate buildTemplate(TemplateRequest templateRequest) throws CouponException;
}