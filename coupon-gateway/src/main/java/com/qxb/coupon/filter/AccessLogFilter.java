package com.qxb.coupon.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * @author qiuxiaobin
 * @create 2020-02-03 20:24
 * @dscription 计算总的请求时间
 */
@Slf4j
@Component
public class AccessLogFilter extends AbstractPostZuulFilter {
    @Override
    protected Object cRun() {
        HttpServletRequest request = context.getRequest();

        //从 PreRequestFilter 中获取设置的请求时间戳
        Long startTime =(Long) context.get("startTime");
        String uri = request.getRequestURI();
        Long duration = (Long) System.currentTimeMillis() - startTime;
        // 从网关通过的请求都会打印日志记录： uri + duration
        log.info("uri: {} , duration: {}",uri,duration);
        return success();
    }

    @Override
    public int filterOrder() {
        return FilterConstants.SEND_RESPONSE_FILTER_ORDER - 1;
    }
}
