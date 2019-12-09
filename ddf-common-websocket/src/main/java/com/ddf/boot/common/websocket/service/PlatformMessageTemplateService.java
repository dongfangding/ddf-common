package com.ddf.boot.common.websocket.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.ddf.boot.common.websocket.model.entity.PlatformMessageTemplate;

import java.util.List;

/**
 * 收款短信模板服务类
 * @author xujinquan
 * @create 2019-08-22
 */
public interface PlatformMessageTemplateService extends IService<PlatformMessageTemplate> {

    /**
     * 获取云闪付到账消息模板
     * @return
     */
    List<PlatformMessageTemplate> getUPayMessageTemplate();


    /**
     * 获取银行短信模板列表
     * @return
     */
    List<PlatformMessageTemplate> getBankSmsTemplates(String credit);

    /**
     * 获取垃圾短信模板,数据过来后必须先经过垃圾短信模板的拦截，确认不是垃圾短信再进入业务匹配
     * @return
     */
    List<PlatformMessageTemplate> getGarbageSmsTemplate();


    /**
     * 获得所有参与短信模板匹配的模板
     *
     * @param
     * @return
     * @author dongfang.ding
     * @date 2019/9/26 15:17
     */
    List<PlatformMessageTemplate> getAllTemplateOrderBySort();

}
