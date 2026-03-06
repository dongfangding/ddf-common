package com.ddf.boot.common.script.i18n;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

public class ExcelLocalTranslatorHelperToDeckApi {

    // 定义 properties 文件与表头名称的映射关系
    private static final Map<String, String> FILE_HEADER_MAP = new LinkedHashMap<>();

    static {
        FILE_HEADER_MAP.put("biz_messages.properties", "中文简体");
        FILE_HEADER_MAP.put("biz_messages_ar.properties", "Arabic");
        FILE_HEADER_MAP.put("biz_messages_bn.properties", "Bangala");
        FILE_HEADER_MAP.put("biz_messages_en.properties", "English");
        FILE_HEADER_MAP.put("biz_messages_es.properties", "Spanish");
        FILE_HEADER_MAP.put("biz_messages_fil.properties", "Filipino");
        FILE_HEADER_MAP.put("biz_messages_fr.properties", "French");
        FILE_HEADER_MAP.put("biz_messages_hi.properties", "Hindi");
        FILE_HEADER_MAP.put("biz_messages_id.properties", "Indonesian");
        FILE_HEADER_MAP.put("biz_messages_ja.properties", "Japanese");
        FILE_HEADER_MAP.put("biz_messages_ko.properties", "Korean");
        FILE_HEADER_MAP.put("biz_messages_pt.properties", "Portuguese");
        FILE_HEADER_MAP.put("biz_messages_vi.properties", "Viet Nam");
        FILE_HEADER_MAP.put("biz_messages_zh-hans.properties", "中文简体");
        FILE_HEADER_MAP.put("biz_messages_zh-hant.properties", "中文繁体");
        FILE_HEADER_MAP.put("biz_messages_de.properties", "German");
        FILE_HEADER_MAP.put("biz_messages_ru.properties", "Russian"); //俄语
        FILE_HEADER_MAP.put("biz_messages_tr.properties", "Turkish"); //土耳其语
        FILE_HEADER_MAP.put("biz_messages_am.properties", "Amharic"); //埃塞俄比亚语-阿姆哈拉语
    }

    // 输出目录：写入到 src/main/resources/i18n 目录下
    String OUTPUT_DIR = System.getProperty("user.dir") + "/deck/deck-api/src/main/resources/i18n/";

    /**
     * 导出 Excel 多语言内容到 properties 文件
     *
     * @param excelFilePath Excel 文件路径
     * @param appendMode    true-追加模式，false-覆盖模式
     * @throws IOException
     * @throws InvalidFormatException
     */
    public void exportTranslations(String excelFilePath, boolean appendMode)
            throws IOException, InvalidFormatException {

        // 确保输出目录存在
        File outDir = new File(OUTPUT_DIR);
        if (!outDir.exists() && !outDir.mkdirs()) {
            throw new IOException("无法创建输出目录: " + OUTPUT_DIR);
        }

        // 打开 Excel 文件
        FileInputStream fis = new FileInputStream(new File(excelFilePath));
        Workbook workbook = WorkbookFactory.create(fis);
        Sheet sheet = workbook.getSheet("java");
        if (sheet == null) {
            throw new RuntimeException("未找到 sheet 名称为 'java' 的工作表！");
        }

        // 读取表头，构建：标题 -> 列索引 映射（忽略大小写）
        Map<String, Integer> headerMap = new HashMap<>();
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            throw new RuntimeException("Excel 文件第一行为空，无法识别表头！");
        }
        for (Cell cell : headerRow) {
            String headerValue = getCellStringValue(cell).trim();
            if (!headerValue.isEmpty()) {
                headerMap.put(headerValue.toLowerCase(), cell.getColumnIndex());
            }
        }
        System.out.println("表头映射: " + headerMap);

        // 使用自定义的 OrderedProperties 确保顺序
        Map<String, OrderedProperties> fileProps = new HashMap<>();
        for (Map.Entry<String, String> entry : FILE_HEADER_MAP.entrySet()) {
            String fileName = entry.getKey();
            OrderedProperties props = new OrderedProperties();
            File propFile = new File(OUTPUT_DIR + fileName);
            if (appendMode && propFile.exists()) {
                try (FileInputStream fisProp = new FileInputStream(propFile)) {
                    props.load(new InputStreamReader(fisProp, StandardCharsets.UTF_8));
                }
            }
            fileProps.put(fileName, props);
        }

        // 遍历 Excel 中的每一行（从第二行开始）
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            // 仅处理第一列不为 "0" 的行
            String flag = getCellStringValue(row.getCell(0)).trim();
            if ("0".equals(flag)) {
                continue;
            }

            // 获取错误码（第二列，索引 1）
            Cell codeCell = row.getCell(1);
            if (codeCell == null) continue;
            String errorCode = getCellStringValue(codeCell).trim();
            if (errorCode.isEmpty()) continue;
            System.out.println("处理第 " + (i + 1) + " 行，错误码: " + errorCode);

            // 遍历每个文件对应的列
            for (Map.Entry<String, String> entry : FILE_HEADER_MAP.entrySet()) {
                String fileName = entry.getKey();
                String headerName = entry.getValue();
                Integer colIndex = headerMap.get(headerName.toLowerCase());
                if (colIndex == null) {
                    continue;
                }
                Cell valueCell = row.getCell(colIndex);
                String message = valueCell == null ? "" : getCellStringValue(valueCell).trim();
                System.out.println("语言【" + headerName + "】的翻译: " + message);
                // 添加到对应的 OrderedProperties 中，保留插入顺序
                OrderedProperties props = fileProps.get(fileName);
                props.setProperty(errorCode, message);
            }
        }

        // 写入 properties 文件，按 OrderedProperties 中的顺序输出
        for (Map.Entry<String, OrderedProperties> entry : fileProps.entrySet()) {
            String fileName = entry.getKey();
            OrderedProperties props = entry.getValue();
            File outFile = new File(OUTPUT_DIR + fileName);
            System.out.println("写入文件: " + outFile.getAbsolutePath() + "，总条数: " + props.size());
            try (OutputStream os = new FileOutputStream(outFile)) {
                // 使用自定义方法写入，保证顺序
                storeOrdered(props, os, null);
            }
        }

        workbook.close();
        fis.close();
    }

    /**
     * 将 Properties 按照插入顺序写入输出流（UTF-8编码），模拟 Properties.store() 的行为
     */
    private void storeOrdered(OrderedProperties props, OutputStream out, String comments) throws IOException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
        if (comments != null) {
            bw.write("#" + comments);
            bw.newLine();
        }
        // 写入日期注释
        bw.write("#" + new Date());
        bw.newLine();
        // 遍历 OrderedProperties 中的 key（按插入顺序）
        for (Object key : props.keySet()) {
            String k = key.toString();
            String val = props.getProperty(k);
            // 转义特殊字符（简单处理）
            k = saveConvert(k, true);
            val = saveConvert(val, false);
            bw.write(k + "=" + val);
            bw.newLine();
        }
        bw.flush();
    }

    // 参考 java.util.Properties 的 saveConvert 方法，简单实现转义（这里只处理常见字符）
    private String saveConvert(String theString, boolean escapeSpace) {
        theString = theString.replaceAll("###\\[","").replaceAll("]###","");
        theString = theString.replaceAll("###\\{","").replaceAll("}###","");
        int len = theString.length();

        StringBuilder outBuffer = new StringBuilder(len * 2);
        for (int x = 0; x < len; x++) {
            char aChar = theString.charAt(x);
            switch (aChar) {
                case ' ':
                    if (x == 0 || escapeSpace)
                        outBuffer.append('\\');
                    outBuffer.append(' ');
                    break;
                case '\\':
                    outBuffer.append("\\\\");
                    break;
                case '\t':
                    outBuffer.append("\\t");
                    break;
                case '\n':
                    outBuffer.append("\\n");
                    break;
                case '\r':
                    outBuffer.append("\\r");
                    break;
                case '\f':
                    outBuffer.append("\\f");
                    break;
                case '=':
                case ':':
                case '#':
                    outBuffer.append('\\').append(aChar);
                    break;
                default:
                    outBuffer.append(aChar);
            }
        }
        return outBuffer.toString();
    }

    /**
     * 将单元格内容转换为字符串（支持不同类型）
     */
    private String getCellStringValue(Cell cell) {
        if (cell == null) return "";
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell);
    }

    /**
     * 自定义 OrderedProperties，继承自 Properties，使用 LinkedHashSet 保留 key 插入顺序
     */
    public static class OrderedProperties extends Properties {
        private final LinkedHashSet<Object> keys = new LinkedHashSet<>();

        @Override
        public synchronized Object put(Object key, Object value) {
            keys.add(key);
            return super.put(key, value);
        }

        @Override
        public Set<Object> keySet() {
            return keys;
        }

        @Override
        public synchronized Enumeration<Object> keys() {
            return Collections.enumeration(keys);
        }
    }

    // 测试 main 方法
    public static void main(String[] args) {
        ExcelLocalTranslatorHelperToDeckApi exporter = new ExcelLocalTranslatorHelperToDeckApi();
        String excelPath = System.getProperty("user.dir") + "/wheel/wheel-docs/src/main/resources/toDeckApi.xlsx";  // 请替换为实际路径
        boolean appendMode; // true 为追加模式，false 为覆盖模式


        // 创建 Scanner 对象以读取控制台输入
        Scanner scanner = new Scanner(System.in);

        try {
            while (true) {
                System.out.println("请选择模式：");
                System.out.println("1. 追加模式");
                System.out.println("2. 覆盖模式");
                System.out.print("请输入选项 (1 或 2): ");
                String input = scanner.nextLine().trim();

                if ("1".equals(input)) {
                    appendMode = true;
                    break;
                } else if ("2".equals(input)) {
                    appendMode = true;
                    break;
                } else {
                    System.out.println("无效输入，请重新输入。");
                }
            }

            exporter.exportTranslations(excelPath, appendMode);
            System.out.println("导出完成！");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            scanner.close(); // 关闭 Scanner
        }
    }
}
