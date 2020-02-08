package com.qxb.coupon.dao;

import com.qxb.coupon.entity.CouponTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author qiuxiaobin
 * @create 2020-02-07 20:20
 * @description CouponTemplate接口定义
 */
public interface CouponTemplateDao extends JpaRepository<CouponTemplate, Integer> {

    /**
     * 根据模板名称查询模板
     * where name = ...
     * */
    CouponTemplate findByName(String name);

    /**
     * 根据 available 和 expired 标记查找模板记录
     * where available = ... and expired = ...
     * */
    List<CouponTemplate> findAllByAvaillableAndExpired(Boolean available, Boolean expired);

    /**
     * 根据 expired 标记查找模板记录
     * where expired = ...
     * */
    List<CouponTemplate> findAllByExpired(Boolean expired);
}
