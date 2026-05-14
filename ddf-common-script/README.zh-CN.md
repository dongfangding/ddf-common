# ddf-common-script

> 开发辅助脚本与离线工具模块。提供基于 Apache POI 的 Excel 处理能力和常用运维脚本，
> 用于开发阶段的批量数据处理和运维操作。

[English](./README.md) · 简体中文

---

## 1. 适用场景

`ddf-common-script` 解决的是 **"开发过程中需要批量数据处理或运维脚本支撑"** 问题。

| 场景        | 典型问题               | 模块提供的能力                   |
|-----------|--------------------|---------------------------|
| 批量数据导入/导出 | 需要读写 Excel 文件做数据迁移 | Apache POI 封装，支持 .xlsx 读写 |
| 开发环境初始化   | 重复的数据库初始化、配置生成工作   | 脚本化批量执行                   |
| 离线数据统计    | 不启动服务，直接处理本地数据文件   | 独立的命令行工具入口                |
| 运维脚本沉淀    | 团队内运维操作分散，难以复用     | 统一脚本目录，版本化管理              |

> **注意**：本模块默认不在 Maven Central 发布集合中（见父 `pom.xml` 的 `excludeArtifacts`），
> 也不包含在任何 starter 中。如需使用请在本地构建或单独配置。

---

## 2. 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-script</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

---

## 3. 最小化配置

本模块无 Spring Boot 自动配置，作为纯工具库使用。引入依赖后直接调用相关工具类即可。

---

## 4. 核心 API

### 4.1 Excel 读取

```java
// 读取 Excel 文件到数据列表
List<List<String>> data = ExcelUtil.readExcel(fileInputStream);

// 按指定 Sheet 读取
List<Map<String, Object>> sheetData = ExcelUtil.readExcel(
    fileInputStream,
    0,           // Sheet 索引
    true         // 是否将第一行作为表头
);
```

### 4.2 Excel 写入

```java
// 构建表头和数据
List<String> headers = Arrays.asList("ID", "名称", "状态");
List<List<Object>> rows = Arrays.asList(
    Arrays.asList(1, "Item A", "生效"),
    Arrays.asList(2, "Item B", "失效")
);

// 写入输出流
ExcelUtil.writeExcel(outputStream, headers, rows, "Sheet1");
```

### 4.3 大数据量写入

```java
// 使用 SXSSF 模式处理大数据量（防止内存溢出）
ExcelUtil.writeLargeExcel(
    outputStream,
    headers,
    dataSupplier,    // 数据提供者，分页获取
    "Sheet1"
);
```

---

## 5. 进阶用法 / 扩展点

### 5.1 自定义单元格样式

```java
CellStyle style = workbook.createCellStyle();
style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

ExcelUtil.writeWithStyle(outputStream, headers, rows, cellStyleMap);
```

### 5.2 脚本执行

模块脚本位于 `src/main/script/` 目录，可通过以下方式执行：

```bash
# 数据库迁移脚本
sh ddf-common-script/src/main/script/db-migrate.sh

# 赋予执行权限后执行
chmod +x ddf-common-script/src/main/script/*.sh
```

---

## 6. 与其他模块协作

| 模块                              | 协作方式                   |
|---------------------------------|------------------------|
| `ddf-common-core`               | 工具类复用和基础支撑             |
| `ddf-common-data-mysql-starter` | 脚本生成的数据可直接导入 MySQL 数据库 |

---

## 7. FAQ

**Q1：本模块为什么不在 starter 中？**
`script` 是开发辅助和离线工具模块，不适合随业务服务一起启动，因此不纳入任何 starter 聚合。

**Q2：Excel 处理支持 .xls 格式吗？**
当前基于 Apache POI 的 `poi-ooxml`，主要面向 `.xlsx`（Office Open XML）格式。如需兼容 `.xls`（BIFF8），需额外引入 `poi` 依赖。

**Q3：大数据量 Excel 导出如何避免 OOM？**
使用 `writeLargeExcel` 方法，底层采用 SXSSF 流式写入，仅缓存指定行数在内存，其余写入临时磁盘文件。

**Q4：脚本文件如何版本管理？**
脚本与源码一起放在 Git 仓库中，通过 Maven 模块方式管理版本，确保脚本与代码版本对应。

---

## 8. 参考

- 源码：`ExcelUtil` 及相关脚本文件
- Apache POI 文档：https://poi.apache.org/
