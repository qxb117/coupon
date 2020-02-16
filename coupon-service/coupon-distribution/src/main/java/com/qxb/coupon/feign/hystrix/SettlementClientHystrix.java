package com.qxb.coupon.feign.hystrix;

import com.qxb.coupon.exception.CouponException;
import com.qxb.coupon.feign.SettlementClient;
import com.qxb.coupon.vo.CommonResponse;
import com.qxb.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author qiuxiaobin
 * @date 2020-02-12 23:03
 * @description 优惠券结算微服务 feign 接口的熔断降级策略
 */
@Slf4j
@Component
public class SettlementClientHystrix implements SettlementClient {

    /**
     * 优惠券规则计算
     * @param settlement {@link SettlementInfo}
     */
    @Override
    public CommonResponse<SettlementInfo> couponRule(SettlementInfo settlement) throws CouponException {

        log.error("[eureka-client-coupon-settlement] couponRule request error!");
        settlement.setEmploy(false);
        settlement.setCost(-1.0);
        return new CommonResponse<>(
                -1,
                "[eureka-client-coupon-settlement]  request error!",
                settlement
        );
    }
}
