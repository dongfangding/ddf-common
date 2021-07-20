package com.ddf.common.ids.service.model.bo;

import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>雪花-绝对递增组合</p >
 *
 * @author shuaishuai.xiao
 * @version 1.0: SegmentIncrementSnowflakeMultiBO.java
 * @date 2020/1/13 14:31
 */
@Data
@Accessors(chain = true)
public class SegmentIncrementSnowflakeMultiBO implements Serializable {
    /**
     * 雪花长度
     */
    private Integer snowflakeSize;
    /**
     * 绝对递增序列
     */
    private List<SegmentIncrementMultiBO> segmentIncrementMultiBOList;
}
