package com.ddf.common.ids.service.model.bo;

import java.io.Serializable;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>批量获取多个key的绝对递增序列</p >
 *
 * @author shuaishuai.xiao
 * @version 1.0: SegmentIncrementMultiBO.java
 * @date 2020/1/13 14:31
 */
@Data
@Accessors(chain = true)
public class SegmentIncrementMultiBO implements Serializable {
    /**
     * key
     */
    private String key;
    /**
     * 长度
     */
    private Integer size;
}
