package com.ddf.boot.common.ext.sms.aliyun;

import com.ddf.boot.common.ext.sms.SmsApi;
import com.ddf.boot.common.ext.sms.aliyun.helper.AliYunSmsHelper;
import com.ddf.boot.common.ext.sms.model.SmsSendRequest;
import com.ddf.boot.common.ext.sms.model.SmsSendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <p>基于阿里云实现的短信服务</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/05/19 23:39
 */
@Component
@RequiredArgsConstructor(onConstructor_={@Autowired})
@Slf4j
public class AliYunSmsApiImpl implements SmsApi {

    private final AliYunSmsHelper aliYunSmsHelper;

    /**
     * 发送模板短信
     *
     * @param request
     * @return
     */
    @Override
    public SmsSendResponse send(SmsSendRequest request) {
        return aliYunSmsHelper.sendSms(request);
    }
}
