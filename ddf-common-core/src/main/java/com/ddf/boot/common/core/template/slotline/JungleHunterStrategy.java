package com.ddf.boot.common.core.template.slotline;

import java.util.List;

/**
 * 丛林猎人策略
 */
public class JungleHunterStrategy implements MachineStrategy {
    @Override
    public void initSymbols(List<TigerMachineApp.Symbol> s) {
        s.add(new TigerMachineApp.Symbol(1, "钻石", "Wild", 0, 0, 0));
        s.add(new TigerMachineApp.Symbol(2, "金钻石", "Jackpot", 0, 0, 0));
        s.add(new TigerMachineApp.Symbol(3, "10", "Normal", 5, 10, 50));
        s.add(new TigerMachineApp.Symbol(4, "J", "Normal", 5, 15, 60));
        s.add(new TigerMachineApp.Symbol(5, "Q", "Normal", 10, 20, 80));
        s.add(new TigerMachineApp.Symbol(6, "K", "Normal", 10, 30, 100));
        s.add(new TigerMachineApp.Symbol(7, "A", "Normal", 15, 50, 150));
        s.add(new TigerMachineApp.Symbol(8, "猩猩", "Normal", 30, 80, 500));
        s.add(new TigerMachineApp.Symbol(9, "鳄鱼", "Normal", 35, 100, 600));
        s.add(new TigerMachineApp.Symbol(10, "犀牛", "Normal", 40, 150, 800));
        s.add(new TigerMachineApp.Symbol(11, "大象", "Normal", 50, 300, 1000));
        s.add(new TigerMachineApp.Symbol(12, "狮子", "Normal", 100, 500, 1500));
    }

    @Override
    public List<List<TigerMachineApp.SymbolPool>> getPoolConfigs() {
        return List.of(
                List.of(new TigerMachineApp.SymbolPool(new int[] {10, 2, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10}, this),
                        new TigerMachineApp.SymbolPool(new int[] {10, 50, 50, 0, 0, 0, 50, 200, 50, 0, 0, 10}, this)),
                List.of(new TigerMachineApp.SymbolPool(new int[] {10, 5, 10, 10, 10, 20, 20, 20, 20, 20, 20, 20}, this),
                        new TigerMachineApp.SymbolPool(new int[] {10, 50, 50, 0, 0, 0, 200, 50, 50, 0, 20, 20}, this)));
    }

    @Override
    public List<String> getIllegalSymbolStrings() {
        return List.of("1,4,7,10,13", "0,3,6,9,12", "2,5,8,11,14", "0,4,8,10,12", "2,4,6,10,14", "1,5,8,11,13",
                "0,3,7,11,14", "1,3,6,9,13", "2,5,7,9,12", "1,5,7,9,13", "1,3,7,11,13", "0,4,7,10,12", "2,4,7,10,14",
                "2,4,7,10,12", "0,4,7,10,14");
    }

    /**
     * @param matrix 参数
     * @param r 参数
     * @param c 参数
     * @param isFreeSpin 参数
     */
    @Override
    public void handleSpecialMatrixLogic(int[][] matrix, int r, int c, boolean isFreeSpin) {
    }

    @Override
    public String getExportFile() {
        return "jungle_hunter.xlsx";
    }
}
