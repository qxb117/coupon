package com.qxb.coupon.service.impl;

import com.alibaba.fastjson.JSON;
import com.qxb.coupon.constant.Constant;
import com.qxb.coupon.constant.CouponStatus;
import com.qxb.coupon.dao.CouponDao;
import com.qxb.coupon.entity.Coupon;
import com.qxb.coupon.service.IKafkaService;
import com.qxb.coupon.vo.CouponKafkaMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * @author qiuxiaobin
 * @date 2020-02-12 20:35
 * @description Kafka 相关服务接口实现
 * 核心思想：是将 Cache 中的 Coupon 状态变化同步到 DB 中
 */
@Slf4j
@Component
public class KafkaServiceImpl implements IKafkaService {

    private final CouponDao couponDao;

    @Autowired
    public KafkaServiceImpl(CouponDao couponDao) {
        this.couponDao = couponDao;
    }

    /**
     * 消费优惠券 Kafka 消息
     * @param record {@link ConsumerRecord}
     * */
    @Override
    @KafkaListener(topics = Constant.TOPIC, groupId = "coupon_1")
    public void consumeCouponKafkaMessage(ConsumerRecord<?, ?> record) {
        Optional kafkaMessage = Optional.ofNullable(record.value());
        if (kafkaMessage.isPresent()) {
            Object message = kafkaMessage.get();
            CouponKafkaMessage CouponInfo = JSON.parseObject(
                    message.toString(),
                    CouponKafkaMessage.class
            );
            log.info("Receive KafkaMessage: {}", message.toString() );
            CouponStatus status = CouponStatus.of(CouponInfo.getStatus());
            switch (status) {
                case USABLE:
                    break;
                case USED:
                    processUsedCoupon(CouponInfo, status);
                    break;
                case EXPIRED:
                    processExpiredCoupon(CouponInfo, status);
                    break;
            }
        }
    }


    /**
     * 处理已使用的用户优惠券
     * */
    private void processUsedCoupon(CouponKafkaMessage kafkaMessage, CouponStatus status) {
        //TODO 可以给用户发送短信等
        processCouponByStatus(kafkaMessage, status);
    }

    /**
     * 处理已过期的用户优惠券
     * */
    private void processExpiredCoupon(CouponKafkaMessage kafkaMessage, CouponStatus status) {
        //TODO 可以给用户发送消息推送
        processCouponByStatus(kafkaMessage, status);
    }

    /** 根据状态处理优惠券信息 */
    private void processCouponByStatus(CouponKafkaMessage kafkaMessage, CouponStatus status) {

        List<Coupon> coupons = couponDao.findAllById(kafkaMessage.getIds());
        if (CollectionUtils.isNotEmpty(coupons) || coupons.size() != kafkaMessage.getIds().size()) {
            log.error("Can Not Find Right Coupon Info: {}", JSON.toJSONString(kafkaMessage));
            //TODO ,可以发送邮件或者短信给相关的人员
            return;
        }
        coupons.forEach(c -> c.setStatus(status));
        log.info("CouponKafkaMessage Op Coupon Count:{}", couponDao.saveAll(coupons).size());
    }
}
