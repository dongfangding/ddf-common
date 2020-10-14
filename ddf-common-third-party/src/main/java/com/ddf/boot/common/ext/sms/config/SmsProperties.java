package com.ddf.boot.common.ext.sms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * <p>阿里云sms配置属性类</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2020/10/14 10:54
 */
@Data
@Component
@ConfigurationProperties(prefix = "customs.ext.sms")
public class SmsProperties {

    public static final String DEFAULT_SMS_ENDPOINT = "dysmsapi.aliyuncs.com";


    /**
     * 服务地址
     * https://help.aliyun.com/document_detail/101511.html?spm=a2c4g.11186623.6.614.663b2b76TIhK9Q
     */
    private String endpoint = DEFAULT_SMS_ENDPOINT;

    /**
     * 请求通信协议 http或https
     */
    private String protocol = "HTTP";

    /**
     * 是否加密accessKeyId、accessKeySecret
     * 如果加密，则使用系统自带的AES算法进行解密
     * @see com.ddf.boot.common.core.util.SecureUtil
     */
    private boolean secretAccessKey;

    /**
     * access_key_id
     */
    private String accessKeyId;

    /**
     * access_key_secret
     */
    private String accessKeySecret;

    /**
     * 短信签名名称
     * 请在控制台签名管理页面签名名称一列查看。
     * 必须是已添加、并通过审核的短信签名。
     */
    private String sinaName;

    /**
     * 短信模板ID。请在控制台模板管理页面模板CODE一列查看。
     * 必须是已添加、并通过审核的短信签名；且发送国际/港澳台消息时，请使用国际/港澳台短信模版。
     */
    private String templateCode;


}
