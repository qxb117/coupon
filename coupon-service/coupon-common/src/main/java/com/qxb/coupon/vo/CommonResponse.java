package com.qxb.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author qiuxiaobin
 * @create 2020-02-04 15:25
 * @description 通用响应对象定义
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonResponse<T> implements Serializable {
    private Integer code;
    private String message;
    private T data;

    public CommonResponse(Integer code , String message){
        this.code = code;
        this.message = message;
    }
}
