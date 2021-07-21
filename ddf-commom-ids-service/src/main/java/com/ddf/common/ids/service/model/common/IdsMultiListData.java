package com.ddf.common.ids.service.model.common;

import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 *
 * 批量组合id
 *
 * @author dongfang.ding
 * @date 2021/7/21 15:55
 **/
@Data
@Accessors(chain = true)
public class IdsMultiListData implements Serializable {

    private static final long serialVersionUID = 865829966599939822L;


    /**
     * 序列ID
     */
    private List<String> sequenceIds;
    /**
     * 雪花ID
     */
    private List<String> snowflakeIds;
}
