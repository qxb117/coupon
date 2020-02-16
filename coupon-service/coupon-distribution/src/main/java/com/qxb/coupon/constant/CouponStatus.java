package com.qxb.coupon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.context.annotation.Bean;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author qiuxiaobin
 * @date 2020-02-10 21:16
 * @description 用户优惠券的状态
 */
@Getter
@AllArgsConstructor
public enum  CouponStatus {

    USABLE("可用的",1),
    USED("已使用的",2),
    EXPIRED("过期的（未使用的）",3);

    /** 优惠券状态描述信息 */
    private String description;
    /** 优惠券状态编码 */
    private Integer code;

    /**
     * 根据 code 获取到 CouponStatus
     * */
    public static CouponStatus of(Integer code){
        Objects.requireNonNull(code);
        return Stream.of(values())
                .filter(bean -> bean.code.equals(code))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(code + " not exists"));
    }
}
