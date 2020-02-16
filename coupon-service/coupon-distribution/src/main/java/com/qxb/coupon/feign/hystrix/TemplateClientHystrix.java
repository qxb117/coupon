package com.qxb.coupon.feign.hystrix;

import com.qxb.coupon.feign.TemplateClient;
import com.qxb.coupon.vo.CommonResponse;
import com.qxb.coupon.vo.CouponTemplateSDK;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author qiuxiaobin
 * @date 2020-02-12 22:41
 * @description 优惠券模板微服务 feign 接口的熔断降级策略
 */
@Slf4j
@Component
public class TemplateClientHystrix implements TemplateClient {


    /**
     * 查找所有可用的优惠券模板
     */
    @Override
    public CommonResponse<List<CouponTemplateSDK>> findAllUsableTemplate() {
        log.error("[eureka-client-coupon-template] findAllUsableTemplate request error!");

        return new CommonResponse<>(
                -1,
                "[eureka-client-coupon-template] request error!",
                Collections.emptyList()
        );
    }

    /**
     * 获取模板 ids 到 CouponTemplateSDK 的映射
     *
     * @param ids
     */
    @Override
    public CommonResponse<Map<Integer, CouponTemplateSDK>> findIds2TemplateSDK(Collection<Integer> ids) {
        log.error("[eureka-client-coupon-template] findIds2TemplateSDK request error!");
        return new CommonResponse<>(
                -1,
                "[eureka-client-coupon-template] request error!",
                new HashMap<>()
        );
    }
}
