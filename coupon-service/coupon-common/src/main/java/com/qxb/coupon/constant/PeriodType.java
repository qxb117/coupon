package com.qxb.coupon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author qiuxiaobin
 * @create 2020-02-06 20:19
 * @description 有效期类型
 */
@Getter
@AllArgsConstructor
public enum PeriodType {

    REGULAR("固定的（固定日期）",1),
    SHIFT("变动的（以领取之日开始计算）", 2);

    /**有效期描述*/
    private String description;
    /**有效期编码*/
    private Integer code;

    public static PeriodType of(Integer code){
        Objects.requireNonNull(code);

        return Stream.of(values())//先把所有的封装成stream流
                .filter(bean->bean.code.equals(code))//过滤出相同code的值
                .findAny()
                .orElseThrow(()->new IllegalArgumentException(code + "not exists"));
    }
}
