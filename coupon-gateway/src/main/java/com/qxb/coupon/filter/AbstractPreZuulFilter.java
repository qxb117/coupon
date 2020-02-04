package com.qxb.coupon.filter;

import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;

/**
 * @author qiuxiaobin
 * @create 2020-02-03 17:24
 * @description：
 */
public abstract class AbstractPreZuulFilter extends AbstractZuulFilter {
    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }
}
