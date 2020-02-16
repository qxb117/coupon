package com.qxb.coupon.feign;

import com.qxb.coupon.exception.CouponException;
import com.qxb.coupon.feign.hystrix.SettlementClientHystrix;
import com.qxb.coupon.vo.CommonResponse;
import com.qxb.coupon.vo.SettlementInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author qiuxiaobin
 * @date 2020-02-12 22:28
 * @description 优惠券结算微服务 feign 接口定义
 */
@FeignClient(value = "eureka-client-coupon-settlement",fallback = SettlementClientHystrix.class)
public interface SettlementClient {

    /** 优惠券规则计算 */
    @RequestMapping(value = "/coupon-settlement/settlement/compute", method = RequestMethod.POST)
    CommonResponse<SettlementInfo> couponRule(@RequestBody SettlementInfo settlement) throws CouponException;

}
