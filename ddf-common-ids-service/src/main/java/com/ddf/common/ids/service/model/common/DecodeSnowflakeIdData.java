package com.ddf.common.ids.service.model.common;

import java.io.Serializable;
import lombok.Data;

/**
 * <p>根据雪花id反解析后的信息</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/07/21 16:55
 */
@Data
public class DecodeSnowflakeIdData implements Serializable {

    private static final long serialVersionUID = -1036666729538599808L;

    /**
     * 原始时间戳
     */
    private long originTimestamp;

    private long workerId;

    private long sequence;
}
