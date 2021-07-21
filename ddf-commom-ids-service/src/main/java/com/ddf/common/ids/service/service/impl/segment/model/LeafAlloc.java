package com.ddf.common.ids.service.service.impl.segment.model;

import java.io.Serializable;
import lombok.Data;

@Data
public class LeafAlloc implements Serializable {

    private static final long serialVersionUID = -909210655925093800L;

    /**
     * 业务tag
     */
    private String key;
    /**
     * 起始id
     */
    private long maxId;
    /**
     * 步长， 即每次本地没有缓存或缓存用完时下一个阶段的id起始段
     */
    private int step;
    /**
     * 期望的id的长度， 如果id长度小于这个长度，则会返回填充， 为0时不处理
     */
    private int fillLength;
    private String updateTime;

}
