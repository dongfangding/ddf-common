package com.ddf.boot.common.core.template.slotline;


import java.util.List;

/**
 * 老虎机游戏策略接口：解耦不同机台的特定逻辑
 */
public interface MachineStrategy {
    void initSymbols(List<TigerMachineApp.Symbol> symbols);
    List<List<TigerMachineApp.SymbolPool>> getPoolConfigs();
    List<String> getIllegalSymbolStrings();
    // 处理特殊机台的生成逻辑
    void handleSpecialMatrixLogic(int[][] matrix, int r, int c, boolean isFreeSpin);

    String getExportFile();
}
