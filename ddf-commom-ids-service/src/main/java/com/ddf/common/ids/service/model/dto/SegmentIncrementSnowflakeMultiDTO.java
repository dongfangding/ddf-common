package com.ddf.common.ids.service.model.dto;

import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>绝对递增-雪花组合响应参数</p >
 *
 * @author shuaishuai.xiao
 * @version 1.0: SegmentIncrementSnowflakeMultiDTO.java
 * @date 2020/1/13 15:53
 */
@Data
@Accessors(chain = true)
public class SegmentIncrementSnowflakeMultiDTO implements Serializable {
    /**
     * key->id序列
     */
    private SegmentIncrementMultiDTO segmentIncrementMultiDTO;
    /**
     * 雪花id
     */
    private List<String> snowflakeIds;
}
