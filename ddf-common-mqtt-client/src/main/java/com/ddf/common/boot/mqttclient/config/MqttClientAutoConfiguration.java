package com.ddf.common.boot.mqttclient.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * <p>mqtt client 配置类</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/18 13:54
 */
@Configuration
@Slf4j
@ComponentScan(basePackages = {"com.ddf.common.boot.mqttclient"})
public class MqttClientAutoConfiguration {

}
