package com.qxb.coupon.filter;

import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;

/**
 * @author qiuxiaobin
 * @create 2020-02-03 17:25
 * @description：
 */
public abstract class AbstractPostZuulFilter extends AbstractZuulFilter{
    @Override
    public String filterType() {
        return FilterConstants.POST_TYPE;
    }
}
