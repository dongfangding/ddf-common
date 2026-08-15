package com.ddf.boot.common.alarm.config;

import cn.hutool.core.collection.CollUtil;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * <p>Lark配置</p >
 *
 * @author rebot
 * @version 1.0
 * @since 2024/06/04 14:56
 */
@Data
@RefreshScope
@ConfigurationProperties(prefix = "customizer.infra.alarm.lark")
public class LarkProperties {

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
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Properties {

        /**
         * 是否开启机器人
         */
        private boolean enabled = true;

        /**
         * webhook地址全路径
         * Lark webhook固定地址为https://open.larksuite.com/open-apis/bot/v2/hook/，
         * 然后新增机器人之后还会有一个动态的url后缀，如https://open.larksuite.com/open-apis/bot/v2/hook/ccds-deff-wewe-eww-dsss
         */
        private String webhookUrl;

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
     */
    public Properties getBizProperties(String mappingCode) {
        if (CollUtil.isNotEmpty(mappingException) && mappingException.containsKey(mappingCode)) {
            return mappingException.get(mappingCode);
        }
        return ObjectUtils.defaultIfNull(bizResource, Properties.defaultInstance());
    }
}
