package com.ddf.boot.common.api.template;

import lombok.Data;


/**
 * slot连消结果简单模型
 *
 * @author snowball
 */
@Data
public class CascadeSymbolExtraInfo {

    /**
     * 符合中奖条件的符号索引
     */
    private Integer index;

    /**
     * 符号ID
     */
    private int symbolId;
    /**
     * 符号对应倍数
     */
    private int multiple;
}
