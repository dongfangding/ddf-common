package com.ddf.boot.common.ext.sms;

import com.ddf.boot.common.ext.sms.model.SmsSendRequest;
import com.ddf.boot.common.ext.sms.model.SmsSendResponse;

/**
 * <p>短信相关接口</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2022/05/send:36
 */
public interface SmsApi {

    /**
     * 发送模板短信
     *
     * @param request 请求对象
     */
    SmsSendResponse send(SmsSendRequest request);
}
