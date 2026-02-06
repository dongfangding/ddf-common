package com.ddf.boot.common.api.enums;

/**
 * <p>redis key的类型</p >
 *
 * @author snowball
 * @version 1.0
 * @since 2023/02/05 23:11
 */
public enum RedisKeyTypeEnum {

    /**
     * redis key 类型,只是标记，无其它实际含义
     */
    STRING,
    LIST,
    HASH,
    HASH_KEY,
    SET,
    ZSET,
    BIT,
    GEO
}
