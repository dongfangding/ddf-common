package com.ddf.common.ids.service.service.impl.segment.model;

import java.util.concurrent.atomic.AtomicLong;
import lombok.Data;

@Data
public class Segment {
    private AtomicLong value = new AtomicLong(0);
    private volatile long max;
    private volatile int step;
    private SegmentBuffer buffer;

    public Segment(SegmentBuffer buffer) {
        this.buffer = buffer;
    }
}
