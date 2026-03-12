package com.ddf.common.boot.mqttclient.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * <p>mqtt client 配置类</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2022/03/18 13:54
 */
@AutoConfiguration
@Slf4j
@ComponentScan(basePackages = {"com.ddf.common.boot.mqttclient"})
public class MqttClientAutoConfiguration {

}
