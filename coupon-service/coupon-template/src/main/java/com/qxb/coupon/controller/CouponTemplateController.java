package com.qxb.coupon.controller;

import com.alibaba.fastjson.JSON;
import com.qxb.coupon.entity.CouponTemplate;
import com.qxb.coupon.exception.CouponException;
import com.qxb.coupon.service.IBuildTemplateService;
import com.qxb.coupon.service.ITemplateBaseService;
import com.qxb.coupon.vo.CouponTemplateSDK;
import com.qxb.coupon.vo.TemplateRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author qiuxiaobin
 * @date 2020-02-09 20:20
 * @description 优惠券模板相关的功能控制器
 */
@Slf4j
@RestController
public class CouponTemplateController {

    /** 构建优惠券模板服务 */
    private final IBuildTemplateService buildTemplateService;

    /** 优惠券模板基础服务 */
    private final ITemplateBaseService templateBasicService;

    @Autowired
    public CouponTemplateController(IBuildTemplateService buildTemplateService, ITemplateBaseService templateBasicService) {
        this.buildTemplateService = buildTemplateService;
        this.templateBasicService = templateBasicService;
    }

    /**
     * 构建优惠券模板
     * 127.0.0.1:7001/coupon-template/template/build
     * 网关转发：127.0.0.1:9000/coupon/coupon-template/template/build
     * */
    @PostMapping("/template/build")
    public CouponTemplate buildTemplate(@RequestBody TemplateRequest request) throws CouponException{
        log.info("Build Template:{}", JSON.toJSONString(request));
        return buildTemplateService.buildTemplate(request);
    }

    /**
     * 构造优惠券模板详情
     * 127.0.0.1:7001/coupon-template/template/info?id=
     * 网关转发：127.0.0.1:9000/coupon/coupon-template/template/info?id=
     * */
    @GetMapping("/template/info")
    public CouponTemplate buildTemplateInfo(@RequestParam("id") Integer id) throws CouponException{
        log.info("Build Template Info For:{}",id);
        return templateBasicService.buildTemplateInfo(id);
    }

    /**
     * 查找所有能用的优惠券模板
     * 127.0.0.1:7001/coupon-template/template/sdk/all
     * 网关转发：127.0.0.1:9000/coupon/coupon-template/template/sdk/all
     * */
    @GetMapping("/template/sdk/all")
    public List<CouponTemplateSDK> findAllUsableTemplate(){
        log.info("Find All Usable Template.");
        return templateBasicService.findAllUsableTemplate();
    }

    /**
     * 获取模板 ids 到 CouponTemplateSDK 的映射
     * 127.0.0.1:7001/coupon-template/template/sdk/infos
     * 网关转发：127.0.0.1:9000/coupon/coupon-template/template/sdk/infos
     * */
    @GetMapping("/template/sdk/infos")
    public Map<Integer,CouponTemplateSDK> findIds2TemplateSDK(@RequestParam("ids") Collection<Integer> ids){
        log.info("FindIdsTemplateSDK:{}",JSON.toJSONString(ids));
        return templateBasicService.findIds2TemplateSDK(ids);
    }
}
