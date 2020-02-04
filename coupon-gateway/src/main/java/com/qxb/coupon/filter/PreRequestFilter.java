package com.qxb.coupon.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author qiuxiaobin
 * @create 2020-02-03 20:17
 * @description 在过滤器中存储发起请求的时间
 */
@Slf4j
@Component
public class PreRequestFilter extends AbstractPreZuulFilter {
    @Override
    protected Object cRun() {
        context.set("startTime",System.currentTimeMillis());
        return success();
    }

    @Override
    public int filterOrder() {
        return 0;
    }
}
