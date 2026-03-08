package com.ddf.boot.common.api.template;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * slot连消结果响应模型，这个是响应给客户端的。
 * totalMultiple累加了全部了所有的生效的倍数
 *
 * @author snowball
 */
@Data
public class CascadeSymbolResultResponse {

    /**
     * 总倍数
     */
    private int totalMultiple;
    /**
     * 总免费次数
     */
    private int freeSpins;

    /**
     * 第一次的付费结果, 如果存在连消，则是多组符号矩阵，每个元素是一组图案矩阵
     */
    private CascadeSymbolMatrixRound self;

    /**
     * 免费结果
     */
    private List<CascadeSymbolMatrixRound> children = new ArrayList<>();
    /**
     * @param self 参数
     * @param totalMultiple 参数
     * @param freeSpins 参数
     */
    public CascadeSymbolResultResponse(CascadeSymbolMatrixRound self, int totalMultiple, int freeSpins) {
        this.self = self;
        this.totalMultiple = totalMultiple;
        this.freeSpins = freeSpins;
    }

    public CascadeSymbolResultResponse() {
    }
}