package com.qxb.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

/**
 * @author qiuxiaobin
 * @create 2020-02-03 15:45
 * @description
 * 1.@EnableZuulProxy标识当前应用是 Zuul Server
 * 2.@SpringCloudApplication组合了SpringBoot应用 + 服务发现 + 熔断
 */
@EnableZuulProxy
@SpringCloudApplication
public class ZuulApplication {
    public static void main(String[] args) {
        SpringApplication.run(ZuulApplication.class, args);
    }
}

