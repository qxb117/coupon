package com.qxb.coupon.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author qiuxiaobin
 * @create 2020-02-04 15:30
 * @description 忽略统一响应定义
 * 1.@Target():注解可以作用在那些类型上面
 * 2.@Retention()：运行时的环境
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreResponseAdvice {
}
