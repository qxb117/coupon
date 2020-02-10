package com.qxb.coupon.service;

import com.alibaba.fastjson.JSON;
import com.qxb.coupon.constant.CouponCategory;
import com.qxb.coupon.constant.DistributeTarget;
import com.qxb.coupon.constant.PeriodType;
import com.qxb.coupon.constant.ProductLine;
import com.qxb.coupon.exception.CouponException;
import com.qxb.coupon.vo.TemplateRequest;
import com.qxb.coupon.vo.TemplateRule;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

/**
 * @author qiuxiaobin
 * @date 2020-02-10 15:02
 * @description 构造优惠券模板服务测试
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class BuildTemplateTest {

    @Autowired
    private IBuildTemplateService buildTemplateService;

    @Test
    public void testBuildTemplate() throws Exception{
        System.out.println(JSON.toJSONString(
                buildTemplateService.buildTemplate(fakeTemplateRequest())
        ));
        Thread.sleep(3000);
    }

    /**
     * fake TemplateRequest
     * */
    private TemplateRequest fakeTemplateRequest(){
        TemplateRequest request = new TemplateRequest();
        request.setName("优惠券模板-"+new Date().getTime());
        request.setLogo("http://www.baidu.com");
        request.setDesc("这是一张优惠券模板");
        request.setCategory(CouponCategory.MANJIAN.getCode());
        request.setProductLine(ProductLine.DAMAO.getCode());
        request.setCount(1000);
        request.setUserId(1001L);
        request.setTarget(DistributeTarget.SINGLE.getCode());

        TemplateRule rule = new TemplateRule();
        rule.setExpiration(new TemplateRule.Expiration(
                PeriodType.SHIFT.getCode(),
                1, DateUtils.addDays(new Date(),60).getTime()
        ));
        rule.setDiscount(new TemplateRule.Discount(7,2));
        rule.setLimitation(1);
        rule.setUsage(new TemplateRule.Usage("广东省","","" +
                JSON.toJSONString(Arrays.asList("数码产品","文具","家居"))));
        rule.setWeight(JSON.toJSONString(Collections.EMPTY_LIST));
        request.setRule(rule);

        return request;
    }
}
