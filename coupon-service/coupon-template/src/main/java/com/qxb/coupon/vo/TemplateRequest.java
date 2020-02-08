package com.qxb.coupon.vo;

import com.qxb.coupon.constant.CouponCategory;
import com.qxb.coupon.constant.DistributeTarget;
import com.qxb.coupon.constant.ProductLine;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * @author qiuxiaobin
 * @create 2020-02-08 17:14
 * @description 优惠券模板创建请求对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplateRequest {

    /** 优惠券名称 */
    private String name;

    /** 优惠券logo */
    private String logo;

    /** 优惠券简介 */
    private String desc;

    /** 优惠券类型 */
    private String category;

    /** 产品线 */
    private Integer productLine;

    /** 总数 */
    private Integer count;

    /** 创建用户 */
    private Long userId;

    /** 目标用户 */
    private Integer target;

    /** 优惠券使用规则 */
    private TemplateRule rule;

    /** 校验对象的合法性 */
    public Boolean validate(){
        boolean stringUtils = StringUtils.isNotEmpty(name)
                && StringUtils.isNotEmpty(logo)
                && StringUtils.isNotEmpty(desc);
        boolean enumValid = null!=CouponCategory.of(category)
                && null!= ProductLine.of(productLine)
                && null!=DistributeTarget.of(target);
        boolean numValid = count>0 && userId>0;

        return stringUtils && enumValid && numValid && rule.validate();
    }
}
