package com.qxb.coupon.dao;

import com.qxb.coupon.constant.CouponStatus;
import com.qxb.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author qiuxiaobin
 * @date 2020-02-11 14:40
 * @description CouponDao接口定义
 */
@Repository
public interface CouponDao extends JpaRepository<Coupon, Integer> {

    /**
     * 根据 userId + 状态查找优惠券记录
     * where user_id = ...  and  status = ...
     * */
    List<Coupon> findAllByUserIdAndStatus(Long userId, CouponStatus status);
}
