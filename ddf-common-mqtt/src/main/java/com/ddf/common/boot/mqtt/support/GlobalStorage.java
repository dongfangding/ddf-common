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
     * 系统当前使用的端口
     */
    public static int APPLICATION_PORT;

    /**
     * topic 路径分隔符
     */
    public static final String TOPIC_SEPARATOR = "/";

    /**
     * 系统clientId前缀
     * 这个值会在mqtt client 初始化的时候改变
     */
    public static String SYSTEM_CLIENT_ID_PREFIX = TOPIC_SEPARATOR + "DEFAULT_CLIENT_ID";

    /**
     * 获取系统clientId前缀，因为这个值可以配置，所以提供判断方法获取
     *
     * @return
     */
    public static String getSystemClientIdPrefix() {
        return GlobalStorage.SYSTEM_CLIENT_ID_PREFIX.startsWith(GlobalStorage.TOPIC_SEPARATOR) ?
                GlobalStorage.SYSTEM_CLIENT_ID_PREFIX :
                GlobalStorage.TOPIC_SEPARATOR + GlobalStorage.SYSTEM_CLIENT_ID_PREFIX;
    }

    /**
     * 点对点通用topic前缀
     */
    public static final String POINT_TO_POINT_TOPIC_PREFIX = TOPIC_SEPARATOR + "point";

    /**
     * 群组通用topic前缀
     */
    public static final String GROUP_TOPIC_PREFIX = TOPIC_SEPARATOR + "group";

    /**
     * 通知类topic前缀
     */
    public static final String NOTICE_TOPIC = TOPIC_SEPARATOR + "NOTICE";

    /**
     * 私聊类topic前缀
     */
    public static final String PRIVATE_MESSAGE_TOPIC = TOPIC_SEPARATOR + "PRIVATE_MESSAGE";

    /**
     * 群聊类topic前缀
     */
    public static final String CHAT_ROOM_MESSAGE_TOPIC = TOPIC_SEPARATOR + "CHAT_ROOM";

    /**
     * emq broker支持通配符， 这里定义通配符的字符
     */
    public static final String WILDCARD_CHARACTER = "#";

}
