package com.ddf.boot.common.alarm.config;

import cn.hutool.core.collection.CollUtil;
import java.util.Map;
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * <p>description</p >
 *
 * @author snowball
 * @version 1.0
 * @since 2024/06/04 14:56
 */
@Data
@RefreshScope
@Configuration
@ConfigurationProperties(prefix = "customizer.infra.alarm.dingtalk")
public class DingTalkProperties {

    /**
     * 每日发送数量限制
     */
    private Integer dailyLimit = 100;

    /**
     * 业务告警-资源告警机器人
     */
   private Properties bizResource;

    /**
     * 业务告警-代码异常告警机器人
     */
   private Properties codeException;

    /**
     * 自定义映射告警机器人配置
     */
    private Map<String, Properties> mappingException;


    @Data
    public static class Properties {

        /**
         * 是否开启机器人
         */
        private boolean enabled = true;

        /**
         * 访问token
         */
        private String accessToken;

        /**
         * 加签密钥
         */
        private String secret;

        public static Properties defaultInstance() {
            final Properties value = new Properties();
            value.setEnabled(false);
            return value;
        }
    }

    /**
     * 获取告警机器人配置
     *
     * @param mappingCode 映射编码
     * @return
     */
    public Properties getCodeProperties(String mappingCode) {
        if (CollUtil.isNotEmpty(mappingException) && mappingException.containsKey(mappingCode)) {
            return mappingException.get(mappingCode);
        }
        return ObjectUtils.defaultIfNull(codeException, Properties.defaultInstance());
    }

    /**
     * 获取告警机器人配置
     *
     * @param mappingCode 映射编码
     * @return
     */
    public Properties getBizProperties(String mappingCode) {
        if (CollUtil.isNotEmpty(mappingException) && mappingException.containsKey(mappingCode)) {
            return mappingException.get(mappingCode);
        }
        return ObjectUtils.defaultIfNull(bizResource, Properties.defaultInstance());
    }
}
