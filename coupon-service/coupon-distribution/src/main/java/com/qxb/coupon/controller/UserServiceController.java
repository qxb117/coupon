package com.qxb.coupon.controller;

import com.alibaba.fastjson.JSON;
import com.qxb.coupon.entity.Coupon;
import com.qxb.coupon.exception.CouponException;
import com.qxb.coupon.service.IUserService;
import com.qxb.coupon.vo.AcquireTemplateRequest;
import com.qxb.coupon.vo.CouponTemplateSDK;
import com.qxb.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.zip.Inflater;

/**
 * @author qiuxiaobin
 * @date 2020-02-14 17:46
 * @description 用户服务 Controller
 */
@Slf4j
@RestController
public class UserServiceController {

    /** 用户服务接口 */
    private final IUserService userService;

    @Autowired
    public UserServiceController(IUserService userService) {
        this.userService = userService;
    }

    /**
     * 根据用户 id 和优惠券记录查找优惠券
     * */
    @GetMapping("/coupons")
    public List<Coupon> findCouponsByStatus(@RequestParam("userId") Long userId, @RequestParam("status") Integer status)
            throws CouponException {
        log.info("Find Coupons By Status: {},{}", userId, status);
        return userService.findCouponByStatus(userId, status);
    }

    /**
     * 根据用户 id 查找可用的优惠券
     * */
    @GetMapping("/template")
    public List<CouponTemplateSDK> findAvailableTemplate(@RequestParam("userId") Long userId) throws CouponException {
        log.info("Find Available Coupons:{}", userId);
        return userService.findAvailableTemplate(userId);
    }

    /**
     * 用户领取优惠券
     * */
    @PostMapping("/acquire/template")
    public Coupon acquireTemplate(@RequestBody AcquireTemplateRequest request)throws CouponException {
        log.info("Acquire Template: {}", JSON.toJSONString(request));
        return userService.acquireTemplate(request);
    }

     /**
     * 结算（核销）优惠券
     * */
     @PostMapping("/settlement")
     public SettlementInfo settlement(@RequestBody SettlementInfo info) throws CouponException {
         log.info("Settlement Coupon: {}", JSON.toJSONString(info));
         return userService.settlement(info);
     }
}
