package com.ddf.boot.common.websocket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ddf.boot.common.websocket.mapper.PlatformMessageTemplateMapper;
import com.ddf.boot.common.websocket.model.entity.PlatformMessageTemplate;
import com.ddf.boot.common.websocket.service.PlatformMessageTemplateService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 收款短信模板 服务实现类
 *
 * @author dongfang.ding
 * @date 2019/12/21
 */
@Service
public class PlatformMessageTemplateServiceImpl extends ServiceImpl<PlatformMessageTemplateMapper, PlatformMessageTemplate>
        implements PlatformMessageTemplateService {

    /**
     * 获取对应支付方式应用到账消息模板, 同时额外查询出忽略模板
     *
     * @param clientChannel
     * @returnW
     */
    @Override
    public List<PlatformMessageTemplate> getTopicMessageTemplate(String clientChannel) {
        LambdaQueryWrapper<PlatformMessageTemplate> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(PlatformMessageTemplate::getRemoved, 0);
        queryWrapper.and((wrapper) -> wrapper.eq(PlatformMessageTemplate::getClientChannel, clientChannel)
                .in(PlatformMessageTemplate::getTemplateType,
                        PlatformMessageTemplate.Type.NORMAL_INCOME_TOPIC_MESSAGE.getValue(),
                        PlatformMessageTemplate.Type.MERCHANT_INCOME_TOPIC_MESSAGE.getValue(),
                        PlatformMessageTemplate.Type.PAY_TOPIC_MESSAGE.getValue())
                .or().eq(PlatformMessageTemplate::getTemplateType, PlatformMessageTemplate.Type.IGNORE_MESSAGE));
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
        queryWrapper.eq(PlatformMessageTemplate::getRemoved, 0);
        queryWrapper.eq(PlatformMessageTemplate::getTemplateType, PlatformMessageTemplate.Type.GARBAGE_SMS.getValue());
        return list(queryWrapper);
    }


    /**
     * 获得所有参与短信模板匹配的模板
     *
     * fixme 短信类型的模板，一个字段标识
     *
     * @param
     * @return

     * @date 2019/9/26 15:17
     */
    @Override
    public List<PlatformMessageTemplate> getAllTemplateOrderBySort() {
        LambdaQueryWrapper<PlatformMessageTemplate> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.ne(PlatformMessageTemplate::getTemplateType, PlatformMessageTemplate.Type.NORMAL_INCOME_TOPIC_MESSAGE.getValue());
        queryWrapper.ne(PlatformMessageTemplate::getTemplateType, PlatformMessageTemplate.Type.MERCHANT_INCOME_TOPIC_MESSAGE.getValue());
        queryWrapper.ne(PlatformMessageTemplate::getTemplateType, PlatformMessageTemplate.Type.PAY_TOPIC_MESSAGE.getValue());
        queryWrapper.eq(PlatformMessageTemplate::getRemoved, 0);
        queryWrapper.orderByAsc(PlatformMessageTemplate::getSort);
        return list(queryWrapper);
    }
}
