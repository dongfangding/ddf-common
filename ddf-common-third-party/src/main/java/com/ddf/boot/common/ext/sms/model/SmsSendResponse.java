package com.ddf.boot.common.ext.sms.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>短信发送结果类</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/05/17 23:33
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SmsSendResponse implements Serializable {

    private static final long serialVersionUID = 1516322558409231083L;

    /**
     * 短信参数内容， 根据不同供应商结果不同，是原始的短信中的变量内容，一般为json
     */
    private String templateParam;

    /**
     * 随机验证码, 简单方式的验证码， 只有一个验证码的时候，会将随机验证码返回
     */
    private String randomCode;
}
