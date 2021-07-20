package com.ddf.common.ids.service.model.common;

import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>${description}</p >
 *
 * @author shuaishuai.xiao
 * @version 1.0: IdsMultiListData.java
 * @date 2019/12/18 18:43
 */
@Data
@Accessors(chain = true)
public class IdsMultiListData implements Serializable {
    /**
     * 序列ID
     */
    private List<String> sequenceIds;
    /**
     * 雪花ID
     */
    private List<String> snowflakeIds;
}
