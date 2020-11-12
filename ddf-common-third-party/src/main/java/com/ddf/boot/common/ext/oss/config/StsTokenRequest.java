package com.ddf.boot.common.ext.oss.config;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * <p>token请求参数类， 主要是为了对ObjectKey进行路径区分</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2020/10/28 11:27
 */
@Data
public class StsTokenRequest {


    /**
     * 平台隔离， 如果有多个客户端，可以用来隔离客户端，或者也可以用来隔离不同功能等
     */
    @NotEmpty(message = "platform不能为空")
    private String platform;

    /**
     * 身份隔离， 如不同用户， 如果是服务级别，建议使用方使用一个系统级别的默认值
     */
    @NotEmpty(message = "身份标识不能为空")
    private String identity;
}
