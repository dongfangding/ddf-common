package com.ddf.boot.common.ids.exception;

/**
 * 无法获取雪花id异常$
 *
 * @author dongfang.ding
 * @date 2020/8/15 0015 18:27
 */
public class SnowflakeException extends RuntimeException {


    public SnowflakeException(String message) {
        super(message);
    }
}
