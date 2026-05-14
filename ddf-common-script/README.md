# ddf-common-script

> Development helper scripts and offline tools module. Provides Apache POI-based Excel processing capabilities and
> common DevOps scripts for batch data processing and operations during development.

English · [简体中文](./README.zh-CN.md)

---

## 1. When to Use This Module

`ddf-common-script` solves the **"development process needs batch data processing or ops script support"** problem.

| Scenario                               | Typical Problem                                          | What the Module Provides                            |
|----------------------------------------|----------------------------------------------------------|-----------------------------------------------------|
| Batch data import/export               | Need to read/write Excel files for data migration        | Apache POI encapsulation, supports .xlsx read/write |
| Development environment initialization | Repetitive database initialization and config generation | Scripted batch execution                            |
| Offline data statistics                | Process local data files without starting the service    | Standalone command-line tool entry                  |
| Ops script accumulation                | Team ops operations are scattered and hard to reuse      | Unified script directory, version-controlled        |

> **Note**: This module is **not** in the default Maven Central release set (see `excludeArtifacts` in parent `pom.xml`),
> and is **not** included in any starter. For use, build locally or configure separately.

---

## 2. Maven Dependency

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-script</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

---

## 3. Minimum Configuration

This module has no Spring Boot auto-configuration; use it as a pure utility library. Invoke relevant utility classes directly after adding the dependency.

---

## 4. Core API

### 4.1 Read Excel

```java
// Read Excel file into data list
List<List<String>> data = ExcelUtil.readExcel(fileInputStream);

// Read specified sheet
List<Map<String, Object>> sheetData = ExcelUtil.readExcel(
    fileInputStream,
    0,           // Sheet index
    true         // Whether to treat the first row as header
);
```

### 4.2 Write Excel

```java
// Build headers and data
List<String> headers = Arrays.asList("ID", "Name", "Status");
List<List<Object>> rows = Arrays.asList(
    Arrays.asList(1, "Item A", "Active"),
    Arrays.asList(2, "Item B", "Inactive")
);

// Write to output stream
ExcelUtil.writeExcel(outputStream, headers, rows, "Sheet1");
```

### 4.3 Large Data Write

```java
// Use SXSSF mode for large datasets (prevents OOM)
ExcelUtil.writeLargeExcel(
    outputStream,
    headers,
    dataSupplier,    // Data provider, paginated fetch
    "Sheet1"
);
```

---

## 5. Advanced Usage / Extension Points

### 5.1 Custom Cell Styles

```java
CellStyle style = workbook.createCellStyle();
style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

ExcelUtil.writeWithStyle(outputStream, headers, rows, cellStyleMap);
```

### 5.2 Script Execution

Module scripts are located in `src/main/script/` and can be executed as follows:

```bash
# Database migration script
sh ddf-common-script/src/main/script/db-migrate.sh

# Grant execute permission before running
chmod +x ddf-common-script/src/main/script/*.sh
```

---

## 6. Interplay with Other Modules

| Module                          | How They Cooperate                                        |
|---------------------------------|-----------------------------------------------------------|
| `ddf-common-core`               | Utility reuse and foundational support                    |
| `ddf-common-data-mysql-starter` | Script-generated data can be directly imported into MySQL |

---

## 7. FAQ

**Q1: Why is this module not in any starter?**
`script` is a development helper and offline tool module, not suitable for bundling with business services, so it is excluded from all starter aggregations.

**Q2: Does Excel processing support .xls format?**
Currently based on Apache POI's `poi-ooxml`, primarily targeting `.xlsx` (Office Open XML). For `.xls` (BIFF8) compatibility, add the `poi` dependency separately.

**Q3: How to avoid OOM when exporting large Excel files?**
Use the `writeLargeExcel` method, which uses SXSSF streaming under the hood — only a configured number of rows are cached in memory; the rest is written to temporary disk files.

**Q4: How are script files version-controlled?**
Scripts are placed in the Git repository alongside source code and managed through the Maven module for versioning, ensuring scripts correspond to code versions.

---

## 8. References

- Source: `ExcelUtil` and related script files
- Apache POI docs: https://poi.apache.org/
