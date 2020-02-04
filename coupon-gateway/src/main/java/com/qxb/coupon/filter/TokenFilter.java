package com.qxb.coupon.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * @author qiuxiaobin
 * @create 2020-02-03 19:49
 * @description：
 */
@Slf4j
@Component
public class TokenFilter extends AbstractPreZuulFilter{
    @Override
    protected Object cRun() {
        HttpServletRequest request = context.getRequest();
        log.info(String.format("%s request to %s"),request.getMethod(),request.getRequestURL().toString());
        Object token = request.getParameter("token");
        if(null == token){
            log.error("Token is Empty！");
            return fail(401, "Token is Empty！");
        }
        return success();
    }

    @Override
    public int filterOrder() {
        return 1;
    }
}
