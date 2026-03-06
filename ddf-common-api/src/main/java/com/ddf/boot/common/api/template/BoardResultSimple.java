package com.ddf.boot.common.api.template;

import java.util.List;
import java.util.Set;
import lombok.Data;

@Data
public class BoardResultSimple {
    /**
     * 当前图案的倍数
     */
    private int totalMultiple;
    /**
     * 当前图案的免费次数
     */
    private int freeSpins;
    /**
     * 是否免费
     */
    private Boolean isFree = Boolean.TRUE;
    /**
     * 图案id数组
     */
    private List<Integer> ids;
    /**
     * 点亮的图案id角标
     */
    private Set<Integer> lightIndex;
}
