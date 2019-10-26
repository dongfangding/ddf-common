package com.ddf.common.websocket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ddf.common.websocket.mapper.PlatformMessageTemplateMapper;
import com.ddf.common.websocket.model.entity.PlatformMessageTemplate;
import com.ddf.common.websocket.service.PlatformMessageTemplateService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 收款短信模板 服务实现类
 */
@Service
public class PlatformMessageTemplateServiceImpl extends ServiceImpl<PlatformMessageTemplateMapper, PlatformMessageTemplate>
        implements PlatformMessageTemplateService {

    /**
     * 获取云闪付到账消息模板
     *
     * @returnW
     */
    @Override
    public List<PlatformMessageTemplate> getUPayMessageTemplate() {
        LambdaQueryWrapper<PlatformMessageTemplate> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.in(PlatformMessageTemplate::getType,
                PlatformMessageTemplate.Type.UNION_PAY_NORMAL_INCOME_MESSAGE.getValue(),
                PlatformMessageTemplate.Type.UNION_PAY_MERCHANT_INCOME_MESSAGE.getValue(),
                PlatformMessageTemplate.Type.UNION_PAY_PAY_MESSAGE.getValue());
        return list(queryWrapper);
    }


    /**
     * 获取银行收款短信模板
     *
     * @return
     */
    @Override
    public List<PlatformMessageTemplate> getBankSmsTemplates(String credit) {
        if (StringUtils.isBlank(credit)) {
            return null;
        }
        LambdaQueryWrapper<PlatformMessageTemplate> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(PlatformMessageTemplate::getType, PlatformMessageTemplate.Type.BANK_INCOME_SMS.getValue());
        queryWrapper.eq(PlatformMessageTemplate::getCredit, credit);
        return list(queryWrapper);
    }


    /**
     * 获取垃圾短信模板,数据过来后必须先经过垃圾短信模板的拦截，确认不是垃圾短信再进入业务匹配
     *
     * @return
     */
    @Override
    public List<PlatformMessageTemplate> getGarbageSmsTemplate() {
        LambdaQueryWrapper<PlatformMessageTemplate> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(PlatformMessageTemplate::getType, PlatformMessageTemplate.Type.GARBAGE_SMS.getValue());
        return list(queryWrapper);
    }


    /**
     * 获得所有参与短信模板匹配的模板
     *
     * @param
     * @return
     * @author dongfang.ding
     * @date 2019/9/26 15:17
     */
    @Override
    public List<PlatformMessageTemplate> getAllTemplateOrderBySort() {
        LambdaQueryWrapper<PlatformMessageTemplate> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.ne(PlatformMessageTemplate::getType, PlatformMessageTemplate.Type.UNION_PAY_NORMAL_INCOME_MESSAGE.getValue());
        queryWrapper.ne(PlatformMessageTemplate::getType, PlatformMessageTemplate.Type.UNION_PAY_MERCHANT_INCOME_MESSAGE.getValue());
        queryWrapper.ne(PlatformMessageTemplate::getType, PlatformMessageTemplate.Type.UNION_PAY_PAY_MESSAGE.getValue());
        queryWrapper.ne(PlatformMessageTemplate::getType, PlatformMessageTemplate.Type.SYSTEM.getValue());
        queryWrapper.orderByAsc(PlatformMessageTemplate::getSort);
        return list(queryWrapper);
    }
}
