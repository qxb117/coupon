package com.qxb.coupon.schedule;

import com.qxb.coupon.dao.CouponTemplateDao;
import com.qxb.coupon.entity.CouponTemplate;
import com.qxb.coupon.vo.TemplateRule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author qiuxiaobin
 * @date 2020-02-09 19:01
 * @description 定时清理已过期的优惠券模板
 */
@Slf4j
@Component
public class ScheduleTask {

    /** CouponTemplate Dao */
    private final CouponTemplateDao templateDao;

    @Autowired
    public ScheduleTask(CouponTemplateDao templateDao) {
        this.templateDao = templateDao;
    }

    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void offlineCouponTemplate(){

        log.info("Start To Expire CouponTemplate");

        List<CouponTemplate> templates = templateDao.findAllByExpired(false);
        if (CollectionUtils.isEmpty(templates)){
            log.info("Done To Expire CouponTemplate.");
            return;
        }

        Date cur = new Date();
        List<CouponTemplate> expiredTemplates = new ArrayList<>(templates.size());
        templates.forEach(t ->{
            //根据优惠券模板规则中的“过期规则”校验模板是否过期
            TemplateRule rule = t.getRule();
            if (rule.getExpiration().getDeadline() < cur.getTime()){
                t.setExpired(true);
                expiredTemplates.add(t);
            }
        });
        if (CollectionUtils.isNotEmpty(expiredTemplates)){
            log.info("Expired CouponTemplate Num:{}",templateDao.saveAll(expiredTemplates));
        }
    }
}
