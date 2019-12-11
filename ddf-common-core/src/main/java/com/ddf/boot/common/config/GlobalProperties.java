package com.ddf.boot.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 *
 * 存放一些全局的自定义属性，根据需要决定是否可配置
 *
 * @author dongfang.ding on 2019/1/25
 */
@Component
@ConfigurationProperties(prefix = "customs.global-properties")
@Getter
@Setter
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
}
