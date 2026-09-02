# 本地文件去重工具 — 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 ddf-common-script 模块中实现基于 SHA-256 的 JavaFX 桌面文件去重工具

**Architecture:** 单进程 JavaFX 桌面应用，3 个类：DedupApplication（UI 入口）、DedupService（扫描+哈希+移动）、DedupResult（数据模型）。扫描目录 → 按文件名排序 → SHA-256 分组 → 重复文件移入目标文件夹。

**Tech Stack:** Java 17, JavaFX 17.0.14, Maven

---

### Task 1: 添加 JavaFX 依赖和 Maven 插件

**Files:**

- Modify: `ddf-common-script/pom.xml`

- [ ] **Step 1: 在 pom.xml 添加 JavaFX 依赖和 javafx-maven-plugin**

在 `<dependencies>` 块内，`poi-ooxml` 依赖之后添加三个 JavaFX 依赖：

```xml
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>17.0.14</version>
</dependency>
```

在 `<project>` 级（和 `<dependencies>` 同级）添加 build/plugins 块：

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-maven-plugin</artifactId>
            <version>0.0.8</version>
            <configuration>
                <mainClass>com.ddf.boot.common.script.file.dedup.DedupApplication</mainClass>
            </configuration>
        </plugin>
    </plugins>
</build>
```

完整修改后的 pom.xml：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>ddf-common</artifactId>
        <groupId>io.github.dongfangding</groupId>
        <version>${revision}</version>
        <relativePath>../pom.xml</relativePath>
    </parent>
    <modelVersion>4.0.0</modelVersion>
    <version>${revision}</version>
    <artifactId>ddf-common-script</artifactId>
    <name>ddf-common-script</name>
    <description>Internal scripts and offline utility tools used by the project.</description>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>${java.version}</maven.compiler.source>
        <maven.compiler.target>${java.version}</maven.compiler.target>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.apache.poi</groupId>
            <artifactId>poi-ooxml</artifactId>
            <version>5.2.3</version>
        </dependency>
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-controls</artifactId>
            <version>17.0.14</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.openjfx</groupId>
                <artifactId>javafx-maven-plugin</artifactId>
                <version>0.0.8</version>
                <configuration>
                    <mainClass>com.ddf.boot.common.script.file.dedup.DedupApplication</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>
```

- [ ] **Step 2: 验证依赖下载成功**

```bash
cd /mnt/d/IdeaWorkspaces/ddf-common && mvn dependency:resolve -pl ddf-common-script -Dmaven.repo.local=/mnt/d/maven_repository
```

Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git -C /mnt/d/IdeaWorkspaces/ddf-common add ddf-common-script/pom.xml
git -C /mnt/d/IdeaWorkspaces/ddf-common commit -m "chore(ddf-common-script): add JavaFX dependencies and plugin for dedup tool"
```

---

### Task 2: 创建 DedupResult 数据模型

**Files:**

- Create: `ddf-common-script/src/main/java/com/ddf/boot/common/script/file/dedup/DedupResult.java`

- [ ] **Step 1: 创建 DedupResult record**

```java
package com.ddf.boot.common.script.file.dedup;

import java.nio.file.Path;
import java.util.List;

public record DedupResult(
        String fileName,
        String hash,
        List<Path> files
) {
    public boolean hasDuplicates() {
        return files.size() > 1;
    }

    public Path kept() {
        return files.get(0);
    }

    public List<Path> duplicates() {
        return files.subList(1, files.size());
    }
}
```

- [ ] **Step 2: 提交**

```bash
git -C /mnt/d/IdeaWorkspaces/ddf-common add ddf-common-script/src/main/java/com/ddf/boot/common/script/file/dedup/DedupResult.java
git -C /mnt/d/IdeaWorkspaces/ddf-common commit -m "feat(dedup): add DedupResult data model"
```

---

### Task 3: 创建 DedupService 扫描和整理逻辑

**Files:**

- Create: `ddf-common-script/src/main/java/com/ddf/boot/common/script/file/dedup/DedupService.java`

- [ ] **Step 1: 创建 DedupService**

```java
package com.ddf.boot.common.script.file.dedup;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DedupService {

    public List<DedupResult> scan(Path scanDir) throws IOException {
        List<Path> allFiles;
        try (Stream<Path> stream = Files.walk(scanDir)) {
            allFiles = stream
                    .filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .collect(Collectors.toList());
        }

        Map<String, List<Path>> hashGroups = new LinkedHashMap<>();
        MessageDigest md = getDigest();
        byte[] buffer = new byte[8192];
        int total = allFiles.size();
        int processed = 0;

        for (Path file : allFiles) {
            try {
                String hash = computeHash(file, md, buffer);
                hashGroups.computeIfAbsent(hash, k -> new ArrayList<>()).add(file);
            } catch (IOException e) {
                System.err.println("跳过无法读取的文件: " + file);
            }
            processed++;
            if (processed % 100 == 0 || processed == total) {
                System.out.printf("\r扫描进度: %d/%d", processed, total);
            }
        }
        System.out.println();

        return hashGroups.entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .map(e -> new DedupResult(
                        e.getValue().get(0).getFileName().toString(),
                        e.getKey(),
                        e.getValue()))
                .sorted(Comparator.comparing(DedupResult::fileName))
                .collect(Collectors.toList());
    }

    public int moveDuplicates(DedupResult result, Path outputDir) throws IOException {
        String baseName = result.fileName();
        int lastDot = baseName.lastIndexOf('.');
        String nameWithoutExt = lastDot > 0 ? baseName.substring(0, lastDot) : baseName;
        String ext = lastDot > 0 ? baseName.substring(lastDot) : "";

        Path targetDir = resolveUniqueDir(outputDir, "重复自_" + nameWithoutExt);
        Files.createDirectories(targetDir);

        int moved = 0;
        for (Path dup : result.duplicates()) {
            try {
                Path dest = resolveUniqueFile(targetDir, dup.getFileName().toString());
                Files.move(dup, dest);
                moved++;
            } catch (IOException e) {
                System.err.println("移动失败: " + dup + " -> " + e.getMessage());
            }
        }
        return moved;
    }

    private Path resolveUniqueDir(Path parent, String name) {
        Path candidate = parent.resolve(name);
        if (!Files.exists(candidate)) {
            return candidate;
        }
        for (int i = 1; ; i++) {
            candidate = parent.resolve(name + "_" + i);
            if (!Files.exists(candidate)) {
                return candidate;
            }
        }
    }

    private Path resolveUniqueFile(Path dir, String fileName) {
        Path candidate = dir.resolve(fileName);
        if (!Files.exists(candidate)) {
            return candidate;
        }
        int lastDot = fileName.lastIndexOf('.');
        String base = lastDot > 0 ? fileName.substring(0, lastDot) : fileName;
        String ext = lastDot > 0 ? fileName.substring(lastDot) : "";
        for (int i = 1; ; i++) {
            candidate = dir.resolve(base + "_" + i + ext);
            if (!Files.exists(candidate)) {
                return candidate;
            }
        }
    }

    private String computeHash(Path file, MessageDigest md, byte[] buffer) throws IOException {
        md.reset();
        try (InputStream in = Files.newInputStream(file)) {
            int n;
            while ((n = in.read(buffer)) != -1) {
                md.update(buffer, 0, n);
            }
        }
        return HexFormat.of().formatHex(md.digest());
    }

    private static MessageDigest getDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
```

- [ ] **Step 2: 验证编译通过**

```bash
cd /mnt/d/IdeaWorkspaces/ddf-common && mvn compile -pl ddf-common-script -Dmaven.repo.local=/mnt/d/maven_repository
```

Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git -C /mnt/d/IdeaWorkspaces/ddf-common add ddf-common-script/src/main/java/com/ddf/boot/common/script/file/dedup/DedupService.java
git -C /mnt/d/IdeaWorkspaces/ddf-common commit -m "feat(dedup): add DedupService scan and move logic"
```

---

### Task 4: 创建 DedupApplication JavaFX 界面

**Files:**

- Create: `ddf-common-script/src/main/java/com/ddf/boot/common/script/file/dedup/DedupApplication.java`

- [ ] **Step 1: 创建 DedupApplication 主窗口**

```java
package com.ddf.boot.common.script.file.dedup;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class DedupApplication extends Application {

    private final TextField scanDirField = new TextField();
    private final TextField outputDirField = new TextField();
    private final Button scanButton = new Button("扫描重复文件");
    private final Button processAllButton = new Button("全部整理");
    private final ProgressBar progressBar = new ProgressBar(0);
    private final Label statusLabel = new Label("就绪");
    private final ListView<DedupResult> resultList = new ListView<>();
    private final ObservableList<DedupResult> results = FXCollections.observableArrayList();

    private final DedupService service = new DedupService();
    private Path scanDir;
    private Path outputDir;

    @Override
    public void start(Stage stage) {
        stage.setTitle("文件去重工具");

        VBox topSection = buildTopSection();
        resultList.setItems(results);
        resultList.setCellFactory(lv -> new DedupResultCell());
        VBox.setVgrow(resultList, Priority.ALWAYS);

        BorderPane root = new BorderPane();
        root.setTop(topSection);
        root.setCenter(resultList);
        root.setBottom(buildBottomBar());

        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    private VBox buildTopSection() {
        HBox scanRow = new HBox(8, new Label("扫描目录:"), scanDirField, createBrowseButton(scanDirField));
        HBox outputRow = new HBox(8, new Label("输出目录:"), outputDirField, createBrowseButton(outputDirField));
        HBox actionRow = new HBox(8, scanButton, progressBar, statusLabel);
        HBox.setHgrow(progressBar, Priority.ALWAYS);
        progressBar.setMaxWidth(Double.MAX_VALUE);

        scanButton.setOnAction(e -> startScan());

        VBox box = new VBox(8, scanRow, outputRow, actionRow);
        box.setPadding(new Insets(12));
        return box;
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

    private HBox buildBottomBar() {
        HBox bar = new HBox(8, processAllButton);
        bar.setPadding(new Insets(12));
        processAllButton.setDisable(true);
        processAllButton.setOnAction(e -> processAll());
        return bar;
    }

    private void startScan() {
        String scanText = scanDirField.getText().trim();
        String outText = outputDirField.getText().trim();
        if (scanText.isEmpty() || outText.isEmpty()) {
            showAlert("请先选择扫描目录和输出目录");
            return;
        }
        scanDir = Path.of(scanText);
        outputDir = Path.of(outText);

        scanButton.setDisable(true);
        processAllButton.setDisable(true);
        results.clear();
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        statusLabel.setText("扫描中...");

        new Thread(() -> {
            try {
                List<DedupResult> found = service.scan(scanDir);
                Platform.runLater(() -> {
                    results.setAll(found);
                    int totalDups = found.stream().mapToInt(r -> r.duplicates().size()).sum();
                    statusLabel.setText(String.format("完成 - %d 组重复, %d 个重复文件", found.size(), totalDups));
                    progressBar.setProgress(1);
                    scanButton.setDisable(false);
                    processAllButton.setDisable(found.isEmpty());
                });
            } catch (IOException ex) {
                Platform.runLater(() -> {
                    showAlert("扫描失败: " + ex.getMessage());
                    scanButton.setDisable(false);
                    progressBar.setProgress(0);
                    statusLabel.setText("扫描失败");
                });
            }
        }).start();
    }

    private void processAll() {
        processAllButton.setDisable(true);
        statusLabel.setText("整理中...");
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);

        new Thread(() -> {
            int totalMoved = 0;
            int failed = 0;
            for (DedupResult r : results) {
                try {
                    totalMoved += service.moveDuplicates(r, outputDir);
                } catch (IOException ex) {
                    failed++;
                }
            }
            final int moved = totalMoved;
            final int f = failed;
            Platform.runLater(() -> {
                statusLabel.setText(String.format("已移动 %d 个文件, %d 组失败", moved, f));
                progressBar.setProgress(1);
                processAllButton.setDisable(false);
                results.clear();
            });
        }).start();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private class DedupResultCell extends ListCell<DedupResult> {
        private final VBox container = new VBox(4);
        private final Label header = new Label();
        private final Label paths = new Label();
        private final Button moveBtn = new Button("整理此组");

        DedupResultCell() {
            header.setFont(Font.font(null, FontWeight.BOLD, 13));
            paths.setFont(Font.font(null, 12));
            paths.setTextFill(Color.GRAY);
            moveBtn.setOnAction(e -> {
                DedupResult item = getItem();
                if (item != null) {
                    new Thread(() -> {
                        try {
                            int moved = service.moveDuplicates(item, outputDir);
                            Platform.runLater(() -> {
                                results.remove(item);
                                statusLabel.setText("已移动 " + moved + " 个文件");
                            });
                        } catch (IOException ex) {
                            Platform.runLater(() -> showAlert("移动失败: " + ex.getMessage()));
                        }
                    }).start();
                }
            });
            container.getChildren().addAll(header, paths, moveBtn);
            container.setPadding(new Insets(6));
        }

        @Override
        protected void updateItem(DedupResult item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                String keptStr = item.kept().toString();
                StringBuilder sb = new StringBuilder();
                sb.append(keptStr).append("  ← 保留\n");
                for (Path d : item.duplicates()) {
                    sb.append(d.toString()).append("  ← 重复\n");
                }
                header.setText(String.format("%s (%d个文件)", item.fileName(), item.files().size()));
                paths.setText(sb.toString().trim());
                setGraphic(container);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
```

- [ ] **Step 2: 验证编译通过**

```bash
cd /mnt/d/IdeaWorkspaces/ddf-common && mvn compile -pl ddf-common-script -Dmaven.repo.local=/mnt/d/maven_repository
```

Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git -C /mnt/d/IdeaWorkspaces/ddf-common add ddf-common-script/src/main/java/com/ddf/boot/common/script/file/dedup/DedupApplication.java
git -C /mnt/d/IdeaWorkspaces/ddf-common commit -m "feat(dedup): add JavaFX DedupApplication UI"
```

---

### Task 5: 验证完整运行

- [ ] **Step 1: 编译**

```bash
cd /mnt/d/IdeaWorkspaces/ddf-common && mvn compile -pl ddf-common-script -Dmaven.repo.local=/mnt/d/maven_repository
```

Expected: BUILD SUCCESS

- [ ] **Step 2: 启动 GUI**（需要桌面环境）

```bash
cd /mnt/d/IdeaWorkspaces/ddf-common && mvn javafx:run -pl ddf-common-script -Dmaven.repo.local=/mnt/d/maven_repository
```

Expected: 窗口弹出，可操作
