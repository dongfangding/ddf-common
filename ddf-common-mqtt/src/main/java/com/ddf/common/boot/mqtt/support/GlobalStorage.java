package com.ddf.common.boot.mqtt.support;

import com.ddf.common.boot.mqtt.config.properties.EmqConnectionProperties;

/**
 * <p>全局数据源</p >
 *
 * 该类用来静态存储全局使用的变量， 在系统内部任意地方可全局使用这里的变量。
 * 有些是常量，有些是外部注入对象， 内部会负责将与之对应的数据存储进去， 使用方尽管使用即可
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/19 12:36
 */
public class GlobalStorage {

    /**
     * 外部注入的客户端连接对象
     */
    public static EmqConnectionProperties.ClientConfig clientConfig;

    /**
     * 系统clientId前缀
     */
    public static String SYSTEM_CLIENT_ID_PREFIX;

    /**
     * topic 路径分隔符
     */
    public static final String TOPIC_SEPARATOR = "/";

    /**
     * 聊天类topic前缀
     */
    public static final String IM_TOPIC = "im";

    /**
     * 通知类topic前缀
     */
    public static final String NOTICE_TOPIC = "notice";

}
