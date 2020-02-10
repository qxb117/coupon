package com.qxb.coupon.vo;

import com.qxb.coupon.constant.PeriodType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * @author qiuxiaobin
 * @create 2020-02-06 20:24
 * @description 优惠券规则对象定义
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplateRule {

    /** 优惠券过期规则 */
    private Expiration expiration;

    /** 折扣规则 */
    private Discount discount;

    /** 每个人最多领取几张的限制 */
    private Integer limitation;

    /** 范围规则 :地域 + 商品类型*/
    private Usage usage;

    /** 权重(可以和那些优惠券叠加使用，同一类的优惠券一定不能叠加)：list[]优惠券的唯一编码 */
    private String weight;

    public boolean validate(){
        return expiration.validate() && discount.validate()
                && limitation>0 && usage.validate() && StringUtils.isNotEmpty(weight);
    }

    /**
     * 有效期限规则
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Expiration{
        /** 有效期规则，对应 PeriodType 的 code 字段*/
        private Integer period;

        /** 有效间隔，只对变动性有效期有效*/
        private Integer gap;

        /** 优惠券模板的失效日期，两类规则都有效*/
        private Long deadline;

        /** 验证一下是否是正确的*/
        boolean validate(){
            return null != PeriodType.of(period) && gap>0 && deadline>0;
        }
    }

    /**
     * 折扣，需要与类型配合决定
     * */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Discount{
        /** 额度：满减（20），折扣（85），立减（7）*/
        private Integer quota;

        /** 基准：需要满多少才能用*/
        private Integer base;

        boolean validate(){
            return quota>0 && base>0;
        }
    }

    /**
     * 范围
     * */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Usage{
        /** 省份*/
        private String province;
        /** 城市*/
        private String city;
        /** 商品类型：list[全品类，生鲜，家居，电子产品]*/
        private String goodsType;

        boolean validate(){
            return StringUtils.isNoneEmpty(province)
                    && StringUtils.isNotEmpty(city)
                    && StringUtils.isNotEmpty(goodsType);
        }
    }
}
