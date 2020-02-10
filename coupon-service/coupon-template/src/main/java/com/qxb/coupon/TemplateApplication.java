package com.qxb.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author qiuxiaobin
 * @create 2020-02-05 19:57
 * @description 优惠券模板微服务启动入口
 */
@EnableScheduling  //允许启动定时任务
@EnableJpaAuditing  //启用SpringDataJpa审计功能，自动填充或更新
@EnableEurekaClient  //标识当前应用是 EurekaClient
@SpringBootApplication
public class TemplateApplication {
    public static void main(String[] args) {
        SpringApplication.run(TemplateApplication.class, args);
    }
}
