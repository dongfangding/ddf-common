package com.ddf.boot.common.core.template.slotline;

import java.util.Collections;
import java.util.List;


/**
 * 金猪
 *
 */
public class GoldenPigStrategy implements MachineStrategy {
    @Override
    public void initSymbols(List<TigerMachineApp.Symbol> s) {
        s.add(new TigerMachineApp.Symbol(1, "板子", "Wild", 0, 0, 0));
        s.add(new TigerMachineApp.Symbol(2, "车头", "Scatter", 0, 0, 0));
        s.add(new TigerMachineApp.Symbol(3, "金币", "Normal", 50, 200, 500));
        s.add(new TigerMachineApp.Symbol(4, "9", "Normal", 2, 10, 50));
        s.add(new TigerMachineApp.Symbol(5, "10", "Normal", 2, 10, 50));
        s.add(new TigerMachineApp.Symbol(6, "J", "Normal", 2, 10, 50));
        s.add(new TigerMachineApp.Symbol(7, "金砖", "Normal", 3, 25, 60));
        s.add(new TigerMachineApp.Symbol(8, "猫咪", "Normal", 5, 50, 80));
        s.add(new TigerMachineApp.Symbol(9, "贵妇", "Normal", 20, 100, 150));
    }

    @Override
    public List<List<TigerMachineApp.SymbolPool>> getPoolConfigs() {
        int[] w1 = {60, 40, 10, 10, 10, 10, 10, 10, 10, 0};
        int[] w2 = {400, 50, 50, 0, 0, 0, 50, 200, 50, 0};
        return List.of(List.of(new TigerMachineApp.SymbolPool(w1, this), new TigerMachineApp.SymbolPool(w2, this)));
    }

    @Override
    public List<String> getIllegalSymbolStrings() {
        return Collections.emptyList();
    }

    @Override
    public void handleSpecialMatrixLogic(int[][] matrix, int r, int c, boolean isFreeSpin) {
    }

    @Override
    public String getExportFile() {
        return "golden_pig.xlsx";
    }
}
