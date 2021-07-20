package com.ddf.common.ids.service.model.common;

import java.io.Serializable;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>${description}</p >
 *
 * @author shuaishuai.xiao
 * @version 1.0: IdsMultiData.java
 * @date 2019/12/18 18:42
 */
@Data
@Accessors(chain = true)
public class IdsMultiData implements Serializable {
    /**
     * 序列ID
     */
    private String sequenceId;
    /**
     * 雪花ID
     */
    private String snowflakeId;
}
