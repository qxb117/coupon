package com.qxb.coupon.controller;

import com.qxb.coupon.annotation.IgnoreResponseAdvice;
import com.qxb.coupon.feign.TemplateClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * @author qiuxiaobin
 * @date 2020-02-14 17:30
 * @description ribbon 应用 controller
 */
@Slf4j
@RestController
public class RibbonController {

    /** rest 客户端 */
    private final RestTemplate restTemplate;

    @Autowired
    public RibbonController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 通过 ribbon 组件调用模板微服务
     * coupon-distribution/info
     * */
    @GetMapping("/info")
    @IgnoreResponseAdvice
    public TemplateInfo getTemplateInfo(){
        String infoUrl = "http://eureka-client-coupon-template/coupon-template/info";
        return restTemplate.getForEntity(
                infoUrl,
                TemplateInfo.class
        ).getBody();
    }

    /**
     * 模板微服务元信息
     */
    private static class TemplateInfo {

        private Integer code;
        private String message;
        private List<Map<String, Object>> data;
    }
}
