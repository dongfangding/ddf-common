package com.ddf.common.ids.service.model.common;

import java.io.Serializable;
import lombok.Data;

@Data
public class SegmentBufferView implements Serializable {

    private static final long serialVersionUID = -1994351347114212849L;

    private String key;
    private long value0;
    private int step0;
    private long max0;

    private long value1;
    private int step1;
    private long max1;
    private int pos;
    private boolean nextReady;
    private boolean initOk;
}
