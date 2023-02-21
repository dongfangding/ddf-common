package com.ddf.common.ons.console.config;

import com.ddf.boot.common.core.util.PreconditionUtil;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>支持自定义多环境的Ons SDK客户端初始化相关参数配置</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/05/17 13:32
 */
@Data
@ConfigurationProperties(prefix = "customs.ons.console")
public class EnvClientProperties {

    public static final String CLIENTS_PROPERTY_NAME = "clients";

    /**
     * 需要认证的管理员用户名, 除开权限系统外，还必须匹配用户名才可操作
     */
    private List<String> adminUserName;

    /**
     * 系统Topic, 防误操作， 这里的Topic不可删除, 真要删除需要联动apollo修改配置
     */
    private List<String> systemTopic;

    /**
     * 系统Group, 防误操作， 这里的Group不可删除, 真要删除需要联动apollo修改配置
     */
    private List<String> systemGroup;

    /**
     * 多环境客户端配置， key为不同环境关键字，value为对应环境的客户端配置
     */
    private LinkedHashMap<String, ClientProperties> clients;

    /**
     * Ons SDK客户端初始化相关参数配置
     */
    @Data
    public static class ClientProperties {

        /**
         * AccessKeyId
         */
        private String accessKeyId;

        /**
         * AccessKeySecret
         */
        private String accessKeySecret;

        /**
         * 访问端点
         */
        private String endpoint;

        /**
         * 实例id
         */
        private String instanceId;

        public void checkRequired() {
            PreconditionUtil.checkArgument(
                    Objects.nonNull(accessKeyId) && Objects.nonNull(accessKeySecret) && Objects.nonNull(endpoint)
                            && Objects.nonNull(instanceId), new IllegalArgumentException("Ons SDK客户端初始化参数配置有误"));
        }
    }

}
