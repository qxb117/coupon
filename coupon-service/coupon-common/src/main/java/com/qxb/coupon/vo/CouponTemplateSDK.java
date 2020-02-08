package com.qxb.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author qiuxiaobin
 * @date 2020-02-08 19:36
 * @description 微服务之间用的优惠券模板信息定义
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CouponTemplateSDK {

    /** 主键 */
    private Integer id;

    /** 优惠券名称 */
    private String name;

    /** 优惠券logo */
    private String logo;

    /** 优惠券描述 */
    private String desc;

    /** 优惠券分类 */
    private String category;

    /** 产品线 */
    private Integer productLine;

    /**  优惠券模板编码 */
    private String key;

    /** 目标用户 */
    private Integer target;

    /** 优惠券规则 */
    private TemplateRule rule;

}
