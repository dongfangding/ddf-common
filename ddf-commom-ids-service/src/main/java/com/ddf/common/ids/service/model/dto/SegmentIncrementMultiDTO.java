package com.ddf.common.ids.service.model.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>批量获取多个key的绝对递增序列返回值</p >
 *
 * @author shuaishuai.xiao
 * @version 1.0: SegmentIncrementMultiDTO.java
 * @date 2020/1/13 14:31
 */
@Data
@Accessors(chain = true)
public class SegmentIncrementMultiDTO implements Serializable {
    /**
     * key->id序列
     */
    private Map<String, List<String>> keyIds;
}
