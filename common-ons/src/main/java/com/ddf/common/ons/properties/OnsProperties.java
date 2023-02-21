package com.ddf.common.ons.properties;

import com.aliyun.openservices.ons.api.PropertyKeyConst;
import java.util.Properties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>ONS配置属性类</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/08/26 14:11
 */
@Data
@ConfigurationProperties(prefix = "customs.ons")
public class OnsProperties {

    /**
     * AccessKey 阿里云身份验证，在阿里云服务器管理控制台创建
     */
    private String accessKey;
    /**
     * SecretKey 阿里云身份验证，在阿里云服务器管理控制台创建
     */
    private String secretKey;
    /**
     * 设置 TCP 接入域名，进入控制台的实例管理页面的“获取接入点信息”区域查看
     */
    private String nameServerAddr;

    /**
     * 生产者配置
     */
    private Producer producer = new Producer();

    /**
     * 消费者配置
     */
    private Consumer consumer = new Consumer();


    public Properties getOnsProperties() {
        Properties properties = new Properties();
        properties.setProperty(PropertyKeyConst.AccessKey, getAccessKey());
        properties.setProperty(PropertyKeyConst.SecretKey, getSecretKey());
        properties.setProperty(PropertyKeyConst.SendMsgTimeoutMillis, getProducer().getTimeoutMillis());
        properties.setProperty(PropertyKeyConst.NAMESRV_ADDR, getNameServerAddr());
        properties.setProperty(PropertyKeyConst.ONSAddr, getNameServerAddr());
        return properties;
    }

    /**
     * 生产端配置
     */
    @Data
    public static class Producer {

        /**
         * 发送消息超时时间，单位毫秒
         */
        private String timeoutMillis;

        /**
         * 发送失败重试开关
         */
        private boolean retryEnabled;

        /**
         * 发送失败重试次数
         */
        private long retryTimes;

        /**
         * 是否记录发送成功日志
         */
        private boolean logSuccessfulEnabled;

        /**
         * 是否记录发送失败日志
         */
        private boolean logFailureEnabled = Boolean.TRUE;

    }


    /**
     * 消费端配置
     */
    @Data
    public static class Consumer {

        /**
         * 消费幂等性过期时间，单位秒
         */
        private long idempotentExpireSeconds;

        /**
         * 是否记录消费成功日志
         */
        private boolean logSuccessfulEnabled;

        /**
         * 是否记录消费失败日志
         */
        private boolean logFailureEnabled = Boolean.TRUE;

    }
}
