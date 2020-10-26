package com.ddf.boot.common.core.model;

import lombok.Data;

import java.io.Serializable;

/**
 * String格式的键值对$
 *
 * @author dongfang.ding
 * @date 2020/10/26 0026 22:23
 */
@Data
public class StringKeyValuePair implements Serializable {

    private static final long serialVersionUID = 4058628306908404491L;

    /**
     * key
     */
    private String key;

    /**
     * value
     */
    private String value;
}
