# 视频目录检测 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在文件工具箱中新增"视频目录检测"面板，扫描指定目录的直接子目录，标记含视频文件的目录。

**Architecture:** 新增 `VideoDetectPanel.java`（VBox 面板），修改 `DedupApplication.java` 注册面板。扫描逻辑内嵌在面板中，不单独抽取 Service。

**Tech Stack:** Java 17, JavaFX, java.nio.file (Files.walkFileTree), java.awt.Desktop

---

### Task 1: 创建 VideoDetectPanel

**Files:**

- Create: `ddf-common-script/src/main/java/com/ddf/boot/common/script/file/dedup/ui/VideoDetectPanel.java`

- [ ] **Step 1: 编写 VideoDetectPanel.java**

```java
package com.ddf.boot.common.script.file.dedup.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Set;

public class VideoDetectPanel extends VBox {

    private static final Set<String> VIDEO_EXTENSIONS = Set.of(
            "mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "m4v", "ts", "rmvb", "3gp"
    );

    private final TextField rootDirField = new TextField();
    private final Button scanButton = new Button("开始扫描");
    private final ProgressBar progressBar = new ProgressBar(0);
    private final Label statusLabel = new Label("就绪");
    private final ListView<Path> resultList = new ListView<>();
    private final ObservableList<Path> results = FXCollections.observableArrayList();

    public VideoDetectPanel() {
        setPadding(new Insets(12));
        setSpacing(8);

        Label title = new Label("视频目录检测");
        title.setFont(Font.font(null, FontWeight.BOLD, 16));

        HBox scanRow = new HBox(8, new Label("扫描目录:"), rootDirField, createBrowseButton(rootDirField));
        HBox actionRow = new HBox(8, scanButton, progressBar, statusLabel);
        HBox.setHgrow(progressBar, Priority.ALWAYS);
        progressBar.setMaxWidth(Double.MAX_VALUE);

        scanButton.setOnAction(e -> startScan());

        resultList.setItems(results);
        resultList.setCellFactory(lv -> new VideoDirCell());
        VBox.setVgrow(resultList, Priority.ALWAYS);

        getChildren().addAll(title, scanRow, actionRow, resultList);
    }

    private Button createBrowseButton(TextField target) {
        Button btn = new Button("浏览...");
        btn.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("选择目录");
            File dir = chooser.showDialog(null);
            if (dir != null) {
                target.setText(dir.getAbsolutePath());
            }
        });
        return btn;
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void startScan() {
        String rootText = rootDirField.getText().trim();
        if (rootText.isEmpty()) {
            showAlert("请先选择扫描目录");
            return;
        }
        Path root = Path.of(rootText);
        File[] children = root.toFile().listFiles(File::isDirectory);
        if (children == null || children.length == 0) {
            showAlert("该目录下没有子目录");
            return;
        }
        java.util.List<Path> subDirs = Arrays.stream(children).map(File::toPath).toList();

        scanButton.setDisable(true);
        results.clear();
        progressBar.setProgress(0);
        int total = subDirs.size();
        statusLabel.setText("扫描中... 0/" + total);

        new Thread(() -> {
            for (int i = 0; i < subDirs.size(); i++) {
                Path subDir = subDirs.get(i);
                if (hasVideoFile(subDir)) {
                    Platform.runLater(() -> results.add(subDir));
                }
                final int idx = i + 1;
                Platform.runLater(() -> {
                    progressBar.setProgress((double) idx / total);
                    statusLabel.setText(String.format("扫描中... %d/%d", idx, total));
                });
            }
            Platform.runLater(() -> {
                statusLabel.setText(String.format("完成 - 找到 %d 个含视频目录", results.size()));
                progressBar.setProgress(1);
                scanButton.setDisable(false);
            });
        }).start();
    }

    private static boolean hasVideoFile(Path dir) {
        try {
            boolean[] found = {false};
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (isVideoFile(file)) {
                        found[0] = true;
                        return FileVisitResult.TERMINATE;
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            return found[0];
        } catch (IOException e) {
            return false;
        }
    }

    private static boolean isVideoFile(Path file) {
        String name = file.getFileName().toString().toLowerCase();
        int dot = name.lastIndexOf('.');
        return dot > 0 && VIDEO_EXTENSIONS.contains(name.substring(dot + 1));
    }

    private class VideoDirCell extends ListCell<Path> {
        private final HBox row = new HBox(8);
        private final Label pathLabel = new Label();
        private final Button openBtn = new Button("打开目录");

        VideoDirCell() {
            openBtn.setOnAction(e -> {
                Path item = getItem();
                if (item != null) {
                    try {
                        Desktop.getDesktop().open(item.toFile());
                    } catch (IOException ex) {
                        showAlert("无法打开目录: " + ex.getMessage());
                    }
                }
            });
            row.getChildren().addAll(pathLabel, openBtn);
            row.setPadding(new Insets(4));
        }

        @Override
        protected void updateItem(Path item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                pathLabel.setText(item.toString());
                setGraphic(row);
            }
        }
    }
}
```

- [ ] **Step 2: 编译验证**

```bash
mvn -s /mnt/d/develop_tools/apache-maven-3.9.9/conf/settings-snowball-wsl.xml compile -pl ddf-common-script -am -DskipTests
```

Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git -C /mnt/d/IdeaWorkspaces/ddf-common add ddf-common-script/src/main/java/com/ddf/boot/common/script/file/dedup/ui/VideoDetectPanel.java
git -C /mnt/d/IdeaWorkspaces/ddf-common commit -m "feat: add VideoDetectPanel for video directory detection"
```

---

### Task 2: 注册面板到 DedupApplication

**Files:**

- Modify: `ddf-common-script/src/main/java/com/ddf/boot/common/script/file/dedup/DedupApplication.java`

- [ ] **Step 1: 在 DedupApplication 中注册 VideoDetectPanel**

找到 `FUNCTION_NAMES` 数组（第 23-28 行），在末尾追加 `"视频目录检测"`：

```java
private static final String[] FUNCTION_NAMES = {
        "文件去重",
        "按拍摄时间归档",
        "监控视频文件归档",
        "监控录像目录压缩",
        "视频目录检测"
};
```

找到 `start()` 方法中的 `panels.put(...)` 调用块（第 78-81 行），在最后追加一行：

```java
panels.put(FUNCTION_NAMES[4], new VideoDetectPanel());
```

- [ ] **Step 2: 编译验证**

```bash
mvn -s /mnt/d/develop_tools/apache-maven-3.9.9/conf/settings-snowball-wsl.xml compile -pl ddf-common-script -am -DskipTests
```

Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git -C /mnt/d/IdeaWorkspaces/ddf-common add ddf-common-script/src/main/java/com/ddf/boot/common/script/file/dedup/DedupApplication.java
git -C /mnt/d/IdeaWorkspaces/ddf-common commit -m "feat: register VideoDetectPanel in DedupApplication"
```
