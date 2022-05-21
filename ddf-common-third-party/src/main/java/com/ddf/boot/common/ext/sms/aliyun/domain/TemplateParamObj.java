package com.ddf.boot.common.ext.sms.aliyun.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>阿里云短信参数对象</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/05/21 21:13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateParamObj {

    /**
     * 短信模板参数原始内容
     */
    private String templateParam;

    /**
     * 验证码内容
     */
    private String code;
}
