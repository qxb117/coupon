package com.qxb.coupon.converter;

import com.alibaba.fastjson.JSON;
import com.qxb.coupon.vo.TemplateRule;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * @author qiuxiaobin
 * @create 2020-02-06 22:26
 * @description 优惠券规则属性转换器(转换成JSON形式的String)
 */
@Converter
public class RuleConverter implements AttributeConverter<TemplateRule,String> {
    @Override
    public String convertToDatabaseColumn(TemplateRule rule) {
        return JSON.toJSONString(rule);
    }

    @Override
    public TemplateRule convertToEntityAttribute(String rule) {
        return JSON.parseObject(rule, TemplateRule.class);
    }
}
