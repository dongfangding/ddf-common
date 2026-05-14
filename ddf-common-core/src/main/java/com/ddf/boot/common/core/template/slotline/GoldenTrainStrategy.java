package com.ddf.boot.common.core.template.slotline;

import java.util.Collections;
import java.util.List;

/**
 * 黄金列车
 */
public class GoldenTrainStrategy implements MachineStrategy {
    @Override
    public void initSymbols(List<TigerMachineApp.Symbol> s) {
        s.add(new TigerMachineApp.Symbol(1, "板子", "Wild", 0, 0, 0));
        s.add(new TigerMachineApp.Symbol(2, "车头", "Scatter", 0, 0, 0));
        s.add(new TigerMachineApp.Symbol(3, "金库", "Bonus", 0, 0, 0));
        s.add(new TigerMachineApp.Symbol(4, "9", "Normal", 2, 10, 50));
        s.add(new TigerMachineApp.Symbol(5, "10", "Normal", 2, 10, 50));
        s.add(new TigerMachineApp.Symbol(6, "J", "Normal", 2, 10, 50));
        s.add(new TigerMachineApp.Symbol(7, "金砖", "Normal", 3, 25, 60));
        s.add(new TigerMachineApp.Symbol(8, "猫咪", "Normal", 5, 50, 80));
        s.add(new TigerMachineApp.Symbol(9, "贵妇", "Normal", 20, 100, 150));
        s.add(new TigerMachineApp.Symbol(10, "检票员", "Normal", 50, 200, 500));
    }

    @Override
    public List<List<TigerMachineApp.SymbolPool>> getPoolConfigs() {
        int[] w1 = {60, 40, 10, 10, 10, 10, 10, 10, 10, 0};
        int[] w2 = {400, 50, 50, 0, 0, 0, 50, 200, 50, 0};
        return List.of(List.of(new TigerMachineApp.SymbolPool(w1, this), new TigerMachineApp.SymbolPool(w2, this)));
    }

    /**
     * @param matrix 参数
     * @param r 参数
     * @param c 参数
     * @param isFreeSpin 参数
     */
    @Override
    public void handleSpecialMatrixLogic(int[][] matrix, int r, int c, boolean isFreeSpin) {
        if (isFreeSpin) {
            if (c == 1) {
                matrix[r][c] = matrix[r][0];
            } else if (c == 4) {
                matrix[r][c] = matrix[r][3];
            }
        }
    }

    @Override
    public String getExportFile() {
        return "golden_train.xlsx";
    }

    @Override
    public List<String> getIllegalSymbolStrings() {
        return Collections.emptyList();
    }
}
