package com.qxb.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.client.RestTemplate;

/**
 * @author qiuxiaobin
 * @date 2020-02-10 20:56
 * @description 分发微服务启动入口
 */
@EnableJpaAuditing
@EnableFeignClients     //开启 feign，允许应用访问其他微服务
@EnableCircuitBreaker  //开启断路器
@EnableEurekaClient
@SpringBootApplication
public class DistributionApplication {

    @Bean
    @LoadBalanced   //负载均衡
    RestTemplate restTemplate(){
        return new RestTemplate();
    }

    public static void main(String[] args) {
        SpringApplication.run(DistributionApplication.class, args);
    }
}
