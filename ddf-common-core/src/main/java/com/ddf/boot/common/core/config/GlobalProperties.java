package com.ddf.boot.common.core.config;

import com.ddf.boot.common.core.util.SecureUtil;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * 存放一些全局的自定义属性，根据需要决定是否可配置
 *
 * @author dongfang.ding on 2019/1/25
 */
@ConfigurationProperties(prefix = "customizer.infra.global-properties")
@Getter
@Setter
@RefreshScope
public class GlobalProperties {

    /**
     * 雪花算法的workerId
     * worker Id can't be greater than 31 or less than 0
     */
    private long snowflakeWorkerId = 1;


    /**
     * 雪花算法的数据中心id  5位
     * dataCenterId can't be greater than 31 or less than 0
     */
    private long snowflakeDataCenterId = 1;

    /**
     * 是否将异常的状态码同时作为http的状态码，默认false
     * 这里只是做一个尝试， 如果异常状态码可以被转换成int类型则转，如果不能再这种模式下就为500
     */
    private boolean exceptionCodeToResponseStatus;

    /**
     * rsa 通用秘钥
     *
     * @see SecureUtil
     */
    private String rsaPrivateKey;


    /**
     * rsa rsa通用公钥
     *
     * @see SecureUtil
     */
    private String rsaPublicKey;

    /**
     * AES 秘钥
     * 最基本要求，采用对称分组密码体制， 秘钥长度的最少支持为128、192、256位，即16、24、32个字节
     *
     * @see SecureUtil
     */
    private String aesSecret;

    /**
     * 签名算法秘钥, 目前使用的HMAC256, 秘钥最好是256位,即32个字节
     */
    private String signSecret;

    /**
     * 是否开启全局的日志打印详情（默认关闭， 先精准控制日志行为，线上排查问题时，可临时开启）
     */
    private boolean globalLogPrintDetails;

    /**
     * 要忽略的打印异常日志的异常完全类型
     * 在日志拦截和全局异常处理中，会拦截所有的异常然后打印日志，如果匹配某些异常，则不打印日志。
     * 如com.boot.common.api.exception.BusinessException
     */
    private List<String> ignoreLogExceptionClassName;

}
