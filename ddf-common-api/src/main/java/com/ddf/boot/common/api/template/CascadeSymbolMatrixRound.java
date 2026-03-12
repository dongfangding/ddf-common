package com.ddf.boot.common.api.template;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * 代表一个回合的所有符号矩阵列表
 *
 * @author snowball
 * @since 2025/11/7 15:38
 **/
@Data
public class CascadeSymbolMatrixRound {

    /**
     * 总倍数，这个倍数是最终所有的倍数
     */
    private int totalMultiple;

    /**
     * 本次图案基础倍数和
     */
    private int normalSymbolMultiple;

    /**
     * 本次魔法图案倍数和， 只有最后一个图案，不存在连消时才计算
     */
    private int magicSymbolMultiple;

    /**
     * 额外的魔法图案倍数和， 如免费模式下，每一个免费次数最终的魔法倍数和，如果下一次免费次数也存在连消的话，是要累加到下一次的魔石倍数中的
     */
    private int additionalMagicSymbolMultiple;

    /**
     * 免费次数
     */
    private int freeSpins;

    /**
     * 图案矩阵回合
     */
    private List<CascadeSymbolMatrixMetadata> round = new ArrayList<>();

}
