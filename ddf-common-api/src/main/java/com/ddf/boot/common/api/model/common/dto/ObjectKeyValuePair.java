package com.ddf.boot.common.api.model.common.dto;

import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 泛型键值对$
 *
 * @author dongfang.ding
 * @date 2020/10/26 0026 22:24
 */
@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class ObjectKeyValuePair<K, V> implements Serializable {

    @Serial
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
