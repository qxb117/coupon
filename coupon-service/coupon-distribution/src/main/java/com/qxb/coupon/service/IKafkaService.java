package com.qxb.coupon.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * @author qiuxiaobin
 * @date 2020-02-11 15:12
 * @description kafka相关的服务接口定义
 */
public interface IKafkaService {

    /**
     * 消费优惠券 Kafka 消息
     * @param record {@link ConsumerRecord}
     * */
    void consumeCouponKafkaMessage(ConsumerRecord<?, ?> record);
}
