package com.ddf.boot.common.api.template;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class BoardResultResponse {
    /**
     * 总倍数
     */
    private int totalMultiple;
    /**
     * 总免费次数
     */
    private int freeSpins;
    /**
     * 第一次的付费结果
     */
    private BoardResultSimple self;
    /**
     * 免费结果
     */
    private List<BoardResultSimple> children = new ArrayList<>();

    /**
     * @param self 参数
     */
    public BoardResultResponse(BoardResultSimple self) {
        this.self = self;
        this.totalMultiple = self.getTotalMultiple();
        this.freeSpins = self.getFreeSpins();
    }

    public BoardResultResponse() {
    }
}
