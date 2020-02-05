package com.qxb.coupon.advice;

import com.qxb.coupon.exception.CouponException;
import com.qxb.coupon.vo.CommonResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * @author qiuxiaobin
 * @create 2020-02-05 14:39
 * @description 全局异常处理
 */
@RestControllerAdvice
public class GlobalExceptionAdvice {

    /**
    * 对CouponException进行统一处理
    * */
    @ExceptionHandler(value = CouponException.class)
    public CommonResponse<String> handlerCouponExcetion(HttpServletRequest req, CouponException cex){
        CommonResponse<String> commonResponse = new CommonResponse<>(
                -1,"business error"
        );
        commonResponse.setData(cex.getMessage());
        return commonResponse;
    }
}
