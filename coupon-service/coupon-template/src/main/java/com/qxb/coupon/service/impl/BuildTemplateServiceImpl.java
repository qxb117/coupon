package com.qxb.coupon.service.impl;

import com.qxb.coupon.TemplateApplication;
import com.qxb.coupon.dao.CouponTemplateDao;
import com.qxb.coupon.entity.CouponTemplate;
import com.qxb.coupon.exception.CouponException;
import com.qxb.coupon.service.IAsyncService;
import com.qxb.coupon.service.IBuildTemplateService;
import com.qxb.coupon.vo.TemplateRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author qiuxiaobin
 * @date 2020-02-09 16:16
 * @description 构建优惠券模板接口实现
 */
@Slf4j
@Service
public class BuildTemplateServiceImpl implements IBuildTemplateService {

    /** 异步服务 */
    private final IAsyncService asyncService;
    /** CouponTemplate Dao */
    private final CouponTemplateDao templateDao;

    @Autowired
    public BuildTemplateServiceImpl(IAsyncService asyncService, CouponTemplateDao templateDao) {
        this.asyncService = asyncService;
        this.templateDao = templateDao;
    }

    /**
     * 创建优惠券模板
     * @param templateRequest {@link TemplateRequest} 模板信息请求对象
     * @return {@link CouponTemplate} 优惠券模板实体
     * */
    @Override
    public CouponTemplate buildTemplate(TemplateRequest templateRequest) throws CouponException {

        //参数合法性校验
        if (templateRequest.validate()){
            throw new CouponException("BuildTemplate Param Is Not Valid！");
        }
        if (null != templateDao.findByName(templateRequest.getName())){
            throw new CouponException("Exist Same Name Template！");
        }

        //构造CouponTemplate保存到数据库里
        CouponTemplate template = requestToTemplate(templateRequest);
        template = templateDao.save(template);

        //构造优惠券模板异步生成优惠券码
        asyncService.asyncConstructCouponByTemplate(template);
        return template;
    }

    /**
     * 将 TemplateRequest 转换为 CouponTemplate
     * */
    private CouponTemplate requestToTemplate(TemplateRequest request){

        return new CouponTemplate(
                request.getName(),
                request.getLogo(),
                request.getDesc(),
                request.getCategory(),
                request.getProductLine(),
                request.getCount(),
                request.getUserId(),
                request.getTarget(),
                request.getRule()
        );
    }
}
