package com.ddf.boot.common.redis.request;

import java.io.Serializable;
import lombok.Data;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2026/01/22 17:52
 */
@Data
public class HashValueUpdateSelectiveCommand implements Serializable {

    /**
     * 哈希表key
     */
    private String key;

    /**
     * 哈希表字段
     */
    private String field;

    /**
     * 哈希表字段值json， 这里必须是一个大对象json，否则没必要使用这个脚本
     */
    private String value;
}
