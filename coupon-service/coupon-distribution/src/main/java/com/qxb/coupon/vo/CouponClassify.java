package com.qxb.coupon.vo;

import com.qxb.coupon.constant.CouponStatus;
import com.qxb.coupon.constant.PeriodType;
import com.qxb.coupon.entity.Coupon;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.time.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author qiuxiaobin
 * @date 2020-02-13 16:28
 * @description 用户优惠券的分类，根据优惠券状态
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CouponClassify {

    /* 可用的优惠券 */
    private List<Coupon> usable;
    /* 已使用的优惠券 */
    private List<Coupon> used;
    /* 已过期的优惠券 */
    private List<Coupon> expired;

    /**
    * 对当前优惠券进行分类
    * */
    public static CouponClassify classify(List<Coupon> coupons) {
        List<Coupon> usable = new ArrayList<>(coupons.size());
        List<Coupon> used = new ArrayList<>(coupons.size());
        List<Coupon> expired = new ArrayList<>(coupons.size());

        coupons.forEach(c -> {
            //判断优惠券是否过期
            Boolean isTimeExpired;
            Long curTime = new Date().getTime();
            if (c.getTemplateSDK().getRule().getExpiration().getPeriod().equals(PeriodType.REGULAR.getCode())) {
                isTimeExpired = c.getTemplateSDK().getRule().getExpiration().getDeadline() <= curTime;
            } else {
                isTimeExpired = DateUtils.addDays(
                        c.getAssignTime(),
                        c.getTemplateSDK().getRule().getExpiration().getGap()
                ).getTime() <= curTime;
            }
            if (c.getStatus() == CouponStatus.USABLE) {
                used.add(c);
            } else if (c.getStatus() == CouponStatus.EXPIRED || isTimeExpired) {
                expired.add(c);
            } else {
                usable.add(c);
            }
        });
        return new CouponClassify(usable, used, expired);
    }
}
