package com.ddf.boot.common.ext.sms.domain;

import lombok.Data;

/**
 * <p>description</p >
 *
 * https://help.aliyun.com/document_detail/101341.html?spm=a2c4g.11186623.6.617.7bca7ce8NvHEbH
 * https://help.aliyun.com/document_detail/101414.html?spm=a2c4g.11186623.2.13.13ce3e2cyyH2aX
 *
 *
 * 这个不是发送给阿里云供应商的，而是给自己内部提供的发送短信的传递参数
 *
 * @author Snowball
 * @version 1.0
 * @date 2020/10/14 10:59
 */
@Data
public class AliYunSmsRequest {

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

    /**
     * 接收短信的手机号码。
     * 国内短信：11位手机号码，例如15951955195。
     * 国际/港澳台消息：国际区号+号码，例如85200000000。
     * 多个用逗号分隔
     */
    private String phoneNumbers;

    /**
     * 短信模板变量对应的实际值，JSON格式
     * 模板：您的验证码${code}，该验证码5分钟内有效，请勿泄漏于他人！
     *
     * 参数内容： {"code":"1111"}
     *
     */
    private String templateParam;

}
