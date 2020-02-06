package com.qxb.coupon.converter;

import com.qxb.coupon.constant.CouponCategory;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * @author qiuxiaobin
 * @create 2020-02-06 22:09
 * @description 优惠券分类枚举属性转换器
 * AttributeConverter<X,Y>
 *     X:是实体属性的类型
 *     Y:是数据库字段的类型
 */
@Converter
public class CouponCategoryConverter implements AttributeConverter<CouponCategory,String> {

    /**
     * 将实体属性X转化为Y存储在数据库中，插入和更新时的操作
     * */
    @Override
    public String convertToDatabaseColumn(CouponCategory couponCategory) {
        return couponCategory.getCode();
    }

    /**
     * 将数据库中的字段Y转化实体属性X，查询操作时执行的操作
     * */
    @Override
    public CouponCategory convertToEntityAttribute(String code) {
        return CouponCategory.of(code);
    }
}
