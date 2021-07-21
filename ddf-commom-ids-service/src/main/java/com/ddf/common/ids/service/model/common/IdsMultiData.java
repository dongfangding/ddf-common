package com.ddf.common.ids.service.model.common;

import java.io.Serializable;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 组合id
 *
 * @author dongfang.ding
 * @date 2021/7/21 15:54
 **/
@Data
@Accessors(chain = true)
public class IdsMultiData implements Serializable {

    private static final long serialVersionUID = -1760809172296914265L;


    /**
     * 序列ID
     */
    private String sequenceId;
    /**
     * 雪花ID
     */
    private String snowflakeId;
}
