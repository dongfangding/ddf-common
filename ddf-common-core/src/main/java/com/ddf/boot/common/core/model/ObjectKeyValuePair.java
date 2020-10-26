package com.ddf.boot.common.core.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 泛型键值对$
 *
 * @author dongfang.ding
 * @date 2020/10/26 0026 22:24
 */
@Data
public class ObjectKeyValuePair<K, V> implements Serializable {

    private static final long serialVersionUID = 1974507147453838012L;

    /**
     * key
     */
    private K key;

    /**
     * value
     */
    private V value;
}
