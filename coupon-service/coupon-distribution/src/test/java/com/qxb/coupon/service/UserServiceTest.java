package com.qxb.coupon.service;

import com.alibaba.fastjson.JSON;
import com.qxb.coupon.constant.CouponStatus;
import com.qxb.coupon.exception.CouponException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author qiuxiaobin
 * @date 2020-02-14 23:22
 * @description 用户服务测试
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class UserServiceTest {
    /** fake 一个虚假的用户id */
    private Long fakeUserId = 2001L;

    @Autowired
    private IUserService userService;

    @Test
    public void testFindCouponByStatus()throws CouponException {
        System.out.println(JSON.toJSONString(
                userService.findCouponByStatus(
                        fakeUserId,
                        CouponStatus.USABLE.getCode()
                )
        ));
    }

    @Test
    public void testFindAvailableTemplate() throws CouponException {
        System.out.println(JSON.toJSONString(
                userService.findAvailableTemplate(fakeUserId)
        ));
    }
}
