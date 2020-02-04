package com.qxb.coupon.conf;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;

/**
 * @author qiuxiaobin
 * @create 2020-02-04 15:14
 * @description Jaskson的自定义配置
 */
@Configuration
public class JacksonConfig {
    @Bean
    public ObjectMapper getObjectMapper(){
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss"
        ));
        return mapper;
    }
}
