# 文件工具箱 — 设计文档

**日期**: 2026-07-08
**状态**: 待实现

## 概述

将现有的去重工具、FileRestore 中的文件归档功能整合为一个统一的 JavaFX 桌面应用，提供功能选择界面和安全的文件移动保障。

## 整合的功能

| 功能 | 名称       | 来源                                         | 说明                           |
|----|----------|--------------------------------------------|------------------------------|
| 1  | 文件去重     | 已实现的 DedupTool                             | SHA-256 扫描重复文件，可选保留，其余移入子文件夹 |
| 2  | 按拍摄时间归档  | FileRestore.computerReadAndMoveFileToMonth | 读取创建时间，按月份归档                 |
| 3  | 监控视频文件归档 | FileRestore.packageMonitorVideo2           | 解析文件名日期，按 月/日 归档             |
| 4  | 监控录像目录压缩 | FileRestore.packageMonitorVideo            | 缩减目录层级，月/日 重构                |

## 安全移动机制 (CRITICAL)

所有文件移动操作必须通过 `SafeMoveService.move()` 统一执行：

```
move(source, dest):
  1. Files.move(source, dest)
  2. if !Files.exists(dest):
       System.exit(1)  // 无条件终止，防止文件丢失
```

任何功能中涉及的文件移动（去重、归档、压缩）都必须调用此方法，绝无例外。

## 代码结构

```
com.ddf.boot.common.script.file.dedup/
├── DedupApplication.java       # 入口，主布局（左侧功能列表 + 右侧面板）
├── DedupService.java           # 去重扫描 + 移动
├── DedupResult.java            # 去重结果模型
├── SafeMoveService.java        # 安全移动服务（所有移动的唯一入口）
├── ui/
│   ├── DedupPanel.java         # 功能1：去重面板
│   ├── PhotoArchivePanel.java  # 功能2：按拍摄时间归档面板
│   ├── VideoArchivePanel.java  # 功能3：监控视频文件归档面板
│   └── FolderCompressPanel.java# 功能4：监控录像目录压缩面板
```

## 界面布局

```
┌──────────┬───────────────────────────────────┐
│ 左侧功能  │  右侧参数面板（动态切换）             │
│ 列表     │                                    │
│          │  功能标题 + 说明文字                │
│ ○ 文件   │  源目录/输出目录选择                │
│   去重   │  [开始] 按钮 + 进度条 + 状态        │
│          │  结果列表（仅去重功能）              │
│ ○ 按拍摄 │                                    │
│   时间   │                                    │
│   归档   │                                    │
│          │                                    │
│ ○ 监控   │                                    │
│   视频   │                                    │
│   归档   │                                    │
│          │                                    │
│ ○ 目录   │                                    │
│   压缩   │                                    │
└──────────┴───────────────────────────────────┘
```

左侧 ListView 显示功能名称。选中后切换右侧面板。窗口 1050×600。

## 各功能面板

### 功能2：按拍摄时间归档

- 源目录 / 输出目录选择
- 逻辑：VID 开头 → 月份目录；其它 → not_vid 目录
- 保持现有逻辑不变

### 功能3：监控视频文件归档

- 源目录 / 输出目录选择
- 逻辑：文件名按 `_` 分割取第5段日期 → 月/日/ 目录
- 日期校验：年份 ≥ 2000，月份 01-12，不合法则 System.exit(1)
- 非 video 开头文件跳过

### 功能4：监控录像目录压缩

- 源目录 / 输出目录选择
- 逻辑：文件夹名前6位→月，前8位→日 → 月/日/原文件夹名/
- 日期校验同功能3
- 子目录中图片文件删除

## 技术栈

Java 17, JavaFX 17.0.14, Maven, ddf-common-script 模块

## 运行

```bash
mvn javafx:run -pl ddf-common-script -Dmaven.repo.local=/mnt/d/maven_repository
```
