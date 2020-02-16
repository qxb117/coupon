package com.qxb.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author qiuxiaobin
 * @date 2020-02-11 15:41
 * @description fake 商品信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoodsInfo {

    /** 商品类型：{@link com.qxb.coupon.constant.GoodsType} */
    private Integer type;

    /** 商品价格 */
    private Double price;

    /** 商品数量 */
    private Integer count;

    //TODO 商品名称，等使用信息，一般是接入商品模块
}
