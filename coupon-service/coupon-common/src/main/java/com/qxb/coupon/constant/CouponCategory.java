package com.qxb.coupon.constant;

import com.qxb.coupon.exception.CouponException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author qiuxiaobin
 * @create 2020-02-06 19:51
 * @description 优惠券分类
 */
@Getter
@AllArgsConstructor
public enum CouponCategory {

    MANJIAN("满减券","001"),
    ZHEKOU("折扣券","002"),
    LIJIAN("立减券","003");

    /** 优惠券描述（分类）*/
    private String description;

    /** 优惠券分类编码 */
    private String code;

    /**根据code去查找枚举类*/
    public static CouponCategory of(String code){

        Objects.requireNonNull(code);

        return Stream.of(values())//先封装成stream流
                .filter(bean->bean.code.equals(code))//过滤出相同code的值
                .findAny()
                .orElseThrow(()->new IllegalArgumentException(code + "not exists"));
    }
}
