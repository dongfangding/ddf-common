package com.ddf.common.ids.service.service.impl.segment.model;

import java.util.concurrent.atomic.AtomicLong;
import lombok.Data;

@Data
public class Segment {
    private AtomicLong value = new AtomicLong(0);
    private volatile long max;
    private volatile int step;
    /**
     * 期望的id的长度， 如果id长度小于这个长度，则会返回填充， 为0时不处理
     */
    private int fillLength;
    private SegmentBuffer buffer;

    public Segment(SegmentBuffer buffer) {
        this.buffer = buffer;
    }

    public long getIdle() {
        return this.getMax() - getValue().get();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Segment(");
        sb.append("value:");
        sb.append(value);
        sb.append(",max:");
        sb.append(max);
        sb.append(",step:");
        sb.append(step);
        sb.append(")");
        return sb.toString();
    }
}
