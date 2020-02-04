package com.qxb.coupon;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @author qiuxiaobin
 * @create 2020-02-01-22:21
 */
@SpringBootApplication
@EnableEurekaServer
@Slf4j
public class EurekaApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaApplication.class,args);
        String name = "qxb";
        String age = "23";
        log.info("Eureka Server start success! my name is {}, {} years old",name,age);
    }
}
