package com.ddf.boot.common.core.template.slotline;

import cn.hutool.core.io.FileUtil;
import com.ddf.boot.common.api.template.BoardResultSimple;
import com.ddf.boot.common.api.util.JsonUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

/**
 * 老虎机模拟程序
 * 重构逻辑：使用策略模式解耦不同机台，优化集合包含判定性能
 */
public class TigerMachineApp {

    private static final Logger log = LoggerFactory.getLogger(TigerMachineApp.class);

    // 机台模式常量
    public static final String MOD_GOLDEN_PIG = "金猪";
    public static final String MOD_GOLDEN_TRAIN = "黄金列车";
    public static final String MOD_JUNGLE_HUNTER = "丛林猎人";

    // 当前使用的机台逻辑
    public static final String MOD_USAGE = MOD_JUNGLE_HUNTER;

    static final Map<Integer, Integer> LIMIT_MAP = new HashMap<>();
    private static Set<Set<Integer>> PREPROCESSED_ILLEGAL_SETS = new HashSet<>();

    static {
        // 初始化倍数分布限制
        LIMIT_MAP.put(0, 1500);
        LIMIT_MAP.put(1, 3500);
        LIMIT_MAP.put(100, 3000);
        LIMIT_MAP.put(200, 1000);
        LIMIT_MAP.put(500, 500);
        LIMIT_MAP.put(2000, 200);
        LIMIT_MAP.put(3000, 200);
        LIMIT_MAP.put(4000, 100);
        LIMIT_MAP.put(5000, 100);
        LIMIT_MAP.put(6000, 100);
        LIMIT_MAP.put(7000, 100);
        LIMIT_MAP.put(8000, 100);
        LIMIT_MAP.put(9000, 100);
    }

    // --- 策略工厂 ---

    /**
     * @param mod 参数
     */
    private static MachineStrategy getStrategy(String mod) {
        return switch (mod) {
            case MOD_GOLDEN_PIG -> new GoldenPigStrategy();
            case MOD_GOLDEN_TRAIN -> new GoldenTrainStrategy();
            case MOD_JUNGLE_HUNTER -> new JungleHunterStrategy();
            default -> null;
        };
    }

    public static void main(String[] args) {
        randomTest();
    }

    public static void randomTest() {
        // 1. 根据当前模式获取策略
        MachineStrategy strategy = getStrategy(MOD_USAGE);
        if (strategy == null) {
            return;
        }

        // 2. 初始化资源与预处理
        final ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
        List<List<SymbolPool>> poolLists = strategy.getPoolConfigs();
        int selectedIndex = threadLocalRandom.nextInt(0, poolLists.size());

        SymbolPool pool = poolLists.get(selectedIndex).get(0);
        SymbolPool freePool = poolLists.get(selectedIndex).get(1);

        // 预处理非法符号：将 String 列表转为 Set<Set<Integer>>，大幅提升 checkResults 性能
        PREPROCESSED_ILLEGAL_SETS = strategy.getIllegalSymbolStrings().stream().map(s -> Arrays.stream(s.split(","))
                .map(String::trim)
                .map(Integer::valueOf)
                .collect(Collectors.toCollection(HashSet::new))).collect(Collectors.toSet());

        List<BoardResultExport> totalResultList = new ArrayList<>();
        int maxResult = LIMIT_MAP.values().stream().reduce(Integer::sum).orElse(0);
        Map<Integer, Integer> multipleIntervalMap = new HashMap<>();
        BoardResultExport lastPayBoardResult = null;

        // 3. 模拟运行逻辑
        while (totalResultList.size() < maxResult) {
            int loopTimes = 1;
            int freeTimes = 0;
            int totalFreeTimes = 0;

            for (int i = 0; i < loopTimes; i++) {
                boolean isFree = totalFreeTimes > 0 && freeTimes < totalFreeTimes;
                int[][] matrix;
                BoardResult boardResult;

                if (isFree) {
                    freeTimes++;
                    matrix = freePool.generateSymbolMatrix(3, 5, true);
                    boardResult = new BoardResult(freePool.convertToSymbolMatrix(matrix), true);

                    if (lastPayBoardResult != null) {
                        lastPayBoardResult.getChildren().add(boardResult);
                        lastPayBoardResult.freeSpins += boardResult.getFreeSpins();
                        lastPayBoardResult.totalMultiple += boardResult.getTotalMultiple();
                        if (lastPayBoardResult.freeSpins > 100) {
                            totalResultList.remove(lastPayBoardResult);
                        }
                    }
                } else {
                    matrix = pool.generateSymbolMatrix(3, 5, false);
                    boardResult = new BoardResult(pool.convertToSymbolMatrix(matrix), false);
                    lastPayBoardResult = new BoardResultExport(boardResult);

                    if (checkResults(lastPayBoardResult, multipleIntervalMap)) {
                        totalResultList.add(lastPayBoardResult);
                    }
                }

                if (boardResult.getFreeSpins() > 0) {
                    totalFreeTimes += boardResult.getFreeSpins();
                    loopTimes += boardResult.getFreeSpins();
                }
            }
        }
        // 4. 数据后处理与导出
        exportFinalResults(totalResultList, MOD_USAGE);
    }

    // --- 逻辑判断部分 ---

    /**
     * @param export 参数
     * @param multipleIntervalMap 参数
     */
    public static boolean checkResults(BoardResultExport export, Map<Integer, Integer> multipleIntervalMap) {
        if (export.getTotalMultiple() > 10000) {
            return false;
        }
        if (export.getFreeSpins() > 0 && (double) export.getTotalMultiple() / export.getFreeSpins() < 20) {
            return false;
        }

        // 【包含关系判断核心】: 判断当前亮起的图标集合是否完全包含了任何一个非法组合
        Set<Integer> currentLightIndex = export.getSelf().getLightIndex();
        for (Set<Integer> illegalSet : PREPROCESSED_ILLEGAL_SETS) {
            if (currentLightIndex.containsAll(illegalSet)) {
                System.out.println("主图案包含非法组合: " + illegalSet);
                return false;
            }
        }

        if (!CollectionUtils.isEmpty(export.getChildren())) {
            for (BoardResult child : export.getChildren()) {
                for (Set<Integer> illegalSet : PREPROCESSED_ILLEGAL_SETS) {
                    if (child.getLightIndex().containsAll(illegalSet)) {
                        System.out.println("赠送图案包含非法组合: " + illegalSet);
                        return false;
                    }
                }
            }
        }

        return !checkCountLimit(export.getTotalMultiple(), multipleIntervalMap);
    }

    /**
     * @param multiple 参数
     * @param multipleIntervalMap 参数
     */
    public static boolean checkCountLimit(Integer multiple, Map<Integer, Integer> multipleIntervalMap) {
        final Integer multipleKey = convertCountLimitKey(multiple);
        int currentCount = multipleIntervalMap.getOrDefault(multipleKey, 0);
        if (currentCount >= LIMIT_MAP.get(multipleKey)) {
            return true;
        }
        multipleIntervalMap.put(multipleKey, currentCount + 1);
        return false;
    }

    /**
     * @param multiple 参数
     */
    public static Integer convertCountLimitKey(Integer multiple) {
        if (multiple == 0) {
            return 0;
        }
        int[] thresholds = {9000, 8000, 7000, 6000, 5000, 4000, 3000, 2000, 500, 200, 100, 1};
        for (int t : thresholds) {
            if (multiple > t) {
                return t;
            }
        }
        return 1;
    }

    // --- 核心实体类 ---
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Symbol {
        private int id;
        private String name;
        private String type;
        private int threeMatchMulti;
        private int fourMatchMulti;
        private int fiveMatchMulti;
    }


    public static class SymbolPool {
        private final List<Symbol> symbols = new ArrayList<>();
        /**
         * @param weights 参数
         * @param strategy 参数
         */
        private final int[] weights;
        private final MachineStrategy strategy;

        public SymbolPool(int[] weights, MachineStrategy strategy) {
            this.weights = weights;
            this.strategy = strategy;
            strategy.initSymbols(this.symbols);
        }

        /**
         * @param rows 参数
         * @param cols 参数
         * @param isFreeSpin 参数
         */
        public int[][] generateSymbolMatrix(int rows, int cols, boolean isFreeSpin) {
            int[][] result = new int[rows][cols];
            int originWildWeight = this.weights[0];
            for (int r = 0; r < rows; r++) {
                int bonusCount = 0;
                for (int c = 0; c < cols; c++) {
                    // 逻辑保留：除第三列外权重为0
                    this.weights[0] = (c != 2) ? 0 : originWildWeight;
                    int symbolId;
                    do {
                        symbolId = isFreeSpin ? randomSymbolIdExcludeWildFree() : randomSymbolId();
                    } while ((symbolId == 3 && bonusCount >= 2) || (symbolId == 1 && c != 2));

                    result[r][c] = symbolId;
                    // 特殊逻辑处理
                    strategy.handleSpecialMatrixLogic(result, r, c, isFreeSpin);
                    if (result[r][c] == 3) {
                        bonusCount++;
                    }
                }
            }
            this.weights[0] = originWildWeight;
            return result;
        }

        /**
         * @param idMatrix 参数
         */
        public Symbol[][] convertToSymbolMatrix(int[][] idMatrix) {
            Symbol[][] matrix = new Symbol[idMatrix.length][idMatrix[0].length];
            for (int r = 0; r < idMatrix.length; r++) {
                for (int c = 0; c < idMatrix[0].length; c++) {
                    int id = idMatrix[r][c];
                    matrix[r][c] = symbols.stream().filter(s -> s.id == id).findFirst().orElse(null);
                }
            }
            return matrix;
        }

        private int randomSymbolId() {
            int totalWeight = Arrays.stream(weights).sum();
            int r = ThreadLocalRandom.current().nextInt(totalWeight);
            int sum = 0;
            for (int i = 0; i < weights.length; i++) {
                sum += weights[i];
                if (r < sum) {
                    return i + 1;
                }
            }
            return 1;
        }

        private int randomSymbolIdExcludeWildFree() {
            int originWildWeight = this.weights[0];
            this.weights[0] = 0;
            try {
                while (true) {
                    int id = randomSymbolId();
                    if (id != 4 && id != 5 && id != 6) {
                        return id;
                    }
                }
            } finally {
                this.weights[0] = originWildWeight;
            }
        }
    }


    @Data
    public static class BoardResult {
        private Symbol[][] matrix;
        private List<LineResult> lineResults = new ArrayList<>();
        private int totalMultiple = 0;
        private int totalLines = 0;
        private int freeSpins = 0;
        private Boolean isFree;
        private Set<Integer> lightIndex = new HashSet<>();
        private List<Integer> ids = new ArrayList<>();

        /**
         * @param matrix 参数
         * @param isFree 参数
         */
        public BoardResult(Symbol[][] matrix, Boolean isFree) {
            this.matrix = matrix;
            this.isFree = isFree;
            calculate();
        }

        private void calculate() {
            int rows = matrix.length;
            int cols = matrix[0].length;
            Map<String, List<Integer>> startSymbols = collectStartSymbols(rows);
            int scatterCount = collectIdsAndScatterCount(rows, cols);
            calculateLineResults(rows, cols, startSymbols);
            calculateScatterFreeSpins(rows, cols, scatterCount);
        }

        private Map<String, List<Integer>> collectStartSymbols(int rows) {
            Map<String, List<Integer>> startSymbols = new HashMap<>();
            for (int r = 0; r < rows; r++) {
                Symbol s = matrix[r][0];
                if ("Normal".equals(s.type)) {
                    startSymbols.computeIfAbsent(s.name, k -> new ArrayList<>()).add(r);
                }
            }
            return startSymbols;
        }

        private int collectIdsAndScatterCount(int rows, int cols) {
            int scatterCount = 0;
            for (int c = 0; c < cols; c++) {
                for (int r = 0; r < rows; r++) {
                    Symbol s = matrix[r][c];
                    ids.add(s.id);
                    if ("Scatter".equals(s.type)) {
                        scatterCount++;
                    }
                }
            }
            return scatterCount;
        }

        private void calculateLineResults(int rows, int cols, Map<String, List<Integer>> startSymbols) {
            for (String name : startSymbols.keySet()) {
                List<Integer> colsMatched = findMatchedColumns(rows, cols, name);
                if (colsMatched.size() < 3) {
                    continue;
                }
                int lineCount = calculateLineCountAndLight(rows, cols, name);
                Symbol first = findFirstSymbol(rows, name);
                int multi = resolveMultiplier(colsMatched.size(), first);
                if (multi > 0) {
                    totalMultiple += multi * lineCount;
                    totalLines += lineCount;
                }
            }
        }

        private List<Integer> findMatchedColumns(int rows, int cols, String name) {
            List<Integer> colsMatched = new ArrayList<>(List.of(0));
            for (int c = 1; c < cols; c++) {
                boolean match = false;
                for (int r = 0; r < rows; r++) {
                    if (matrix[r][c].name.equals(name) || "Wild".equals(matrix[r][c].type)) {
                        match = true;
                        break;
                    }
                }
                if (match) {
                    colsMatched.add(c);
                } else {
                    break;
                }
            }
            return colsMatched;
        }

        private int calculateLineCountAndLight(int rows, int cols, String name) {
            int lineCount = 1;
            int flatIdx = 0;
            for (int c = 0; c < cols; c++) {
                int currentLineMatch = 0;
                for (int r = 0; r < rows; r++) {
                    if (matrix[r][c].name.equals(name) || "Wild".equals(matrix[r][c].type)) {
                        currentLineMatch++;
                        lightIndex.add(flatIdx);
                    }
                    flatIdx++;
                }
                if (currentLineMatch == 0) {
                    break;
                }
                lineCount *= currentLineMatch;
            }
            return lineCount;
        }

        private Symbol findFirstSymbol(int rows, String name) {
            for (int r = 0; r < rows; r++) {
                if (matrix[r][0].name.equals(name)) {
                    return matrix[r][0];
                }
            }
            return null;
        }

        private int resolveMultiplier(int matchedColumnCount, Symbol first) {
            return switch (matchedColumnCount) {
                case 3 -> first.threeMatchMulti;
                case 4 -> first.fourMatchMulti;
                case 5 -> first.fiveMatchMulti;
                default -> 0;
            };
        }

        private void calculateScatterFreeSpins(int rows, int cols, int scatterCount) {
            if (scatterCount < 6) {
                return;
            }
            int flatIdx = 0;
            for (int c = 0; c < cols; c++) {
                for (int r = 0; r < rows; r++) {
                    if ("Scatter".equals(matrix[r][c].type)) {
                        lightIndex.add(flatIdx);
                    }
                    flatIdx++;
                }
            }
            if (isFree) {
                freeSpins = 5;
            } else {
                Map<Integer, Integer> s2f = Map.of(6, 5, 7, 10, 8, 15, 9, 20, 10, 40, 11, 80, 12, 150, 13, 250, 14,
                        350, 15, 500);
                freeSpins = s2f.getOrDefault(scatterCount, 0);
            }
        }
    }


    @Data
    public static class BoardResultExport {
        private int totalMultiple;
        private int freeSpins;
        private BoardResult self;
        private List<BoardResult> children = new ArrayList<>();

        /**
         * @param self 参数
         */
        public BoardResultExport(BoardResult self) {
            this.self = self;
            this.totalMultiple = self.getTotalMultiple();
            this.freeSpins = self.getFreeSpins();
        }
    }


    @Data
    @AllArgsConstructor
    public static class LineResult {
        private String name;
        private int columns;
        private int multiplier;
        private List<Integer> columnIndices;
    }

    // --- 导出逻辑 ---
    private static void exportFinalResults(List<BoardResultExport> results, String modUsage) {
        List<BoardResultExport> filtered = results.stream()
                .filter(obj -> obj.getTotalMultiple() > 0 || (obj.getTotalMultiple() == 0 && obj.getChildren()
                        .isEmpty()))
                .sorted(Comparator.comparing(BoardResultExport::getTotalMultiple))
                .toList();

        List<Map<String, Object>> exportList = new ArrayList<>();
        for (BoardResultExport res : filtered) {
            Map<String, Object> map = new HashMap<>();
            map.put("totalMultiple", res.getTotalMultiple());
            map.put("freeSpins", res.getFreeSpins());
            map.put("self", JsonUtil.toJson(toSimple(res.getSelf())));
            map.put("children", res.getChildren().isEmpty() ? "" :
                    JsonUtil.toJson(res.getChildren().stream().map(TigerMachineApp::toSimple).toList()));
            exportList.add(map);
        }

        String desktop = System.getProperty("user.home") + "/Desktop/";
        FileUtil.writeString(JsonUtil.toJson(exportList), new File(desktop + "tiger_machine.txt"), "UTF-8");

        String exportFile = desktop + getStrategy(modUsage).getExportFile();

        // 假定模板存在
        //        try {
        //            EasyExcel
        //                .write(exportFile)
        //                .withTemplate(desktop + "tiger_machine_template.xlsx")
        //                .sheet()
        //                .doFill(exportList);
        //        } catch (Exception e) {
        //            log.warn("Excel 导出失败（可能缺少模板）: {}", e.getMessage());
        //        }
        System.out.println("成功生成数据量: " + filtered.size());
    }

    /**
     * @param r 参数
     */
    private static BoardResultSimple toSimple(BoardResult r) {
        BoardResultSimple s = new BoardResultSimple();
        s.setTotalMultiple(r.getTotalMultiple());
        s.setFreeSpins(r.getFreeSpins());
        s.setIsFree(r.getIsFree());
        s.setIds(r.getIds());
        s.setLightIndex(r.getLightIndex());
        return s;
    }
}
