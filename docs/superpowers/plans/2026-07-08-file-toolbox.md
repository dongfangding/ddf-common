# 文件工具箱 — 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将去重工具 + 三个文件归档功能整合为统一 JavaFX 应用，左侧功能选择 + 右侧面板切换，所有移动操作走 SafeMoveService

**Architecture:** DedupApplication 负责主布局（左侧 ListView + 右侧面板容器），每个功能封装为独立 Panel 类。SafeMoveService 是文件移动的唯一安全入口。DedupService 的移动操作改用 SafeMoveService。

**Tech Stack:** Java 17, JavaFX 17.0.14, Maven

---

### Task 1: 创建 SafeMoveService 安全移动服务

**Files:**

- Create: `ddf-common-script/src/main/java/com/ddf/boot/common/script/file/dedup/SafeMoveService.java`

- [ ] **Step 1: 创建 SafeMoveService**

```java
package com.ddf.boot.common.script.file.dedup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SafeMoveService {

    public static void move(Path source, Path dest) throws IOException {
        Files.move(source, dest);
        if (!Files.exists(dest)) {
            System.err.println("FATAL: 文件移动后目标不存在! source=" + source + " dest=" + dest);
            System.exit(1);
        }
    }
}
```

- [ ] **Step 2: 验证编译**

```bash
cd /mnt/d/IdeaWorkspaces/ddf-common && mvn compile -pl ddf-common-script -Dmaven.repo.local=/mnt/d/maven_repository
```

Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git -C /mnt/d/IdeaWorkspaces/ddf-common add ddf-common-script/src/main/java/com/ddf/boot/common/script/file/dedup/SafeMoveService.java
git -C /mnt/d/IdeaWorkspaces/ddf-common commit -m "feat(toolbox): add SafeMoveService for verified file moves"
```

---

### Task 2: DedupService 改用 SafeMoveService

**Files:**

- Modify: `ddf-common-script/src/main/java/com/ddf/boot/common/script/file/dedup/DedupService.java`

- [ ] **Step 1: 替换所有 Files.move 为 SafeMoveService.move**

将 `DedupService.moveDuplicates` 中的两处 `Files.move` 替换为 `SafeMoveService.move`：

Line 83: `Files.move(keptSrc, keptDest);` → `SafeMoveService.move(keptSrc, keptDest);`
Line 89: `Files.move(dup, dest);` → `SafeMoveService.move(dup, dest);`

修改后的完整 `moveDuplicates` 方法：

```java
public int moveDuplicates(DedupResult result, Path outputDir) throws IOException {
    String keptName = result.kept().getFileName().toString();
    int lastDot = keptName.lastIndexOf('.');
    String nameWithoutExt = lastDot > 0 ? keptName.substring(0, lastDot) : keptName;

    Path targetDir = resolveUniqueDir(outputDir, nameWithoutExt + "_重复");
    Files.createDirectories(targetDir);

    int moved = 0;

    Path keptSrc = result.kept();
    Path keptDest = resolveUniqueFile(outputDir, keptName);
    SafeMoveService.move(keptSrc, keptDest);
    moved++;

    for (Path dup : result.duplicates()) {
        try {
            Path dest = resolveUniqueFile(targetDir, dup.getFileName().toString());
            SafeMoveService.move(dup, dest);
            moved++;
        } catch (IOException e) {
            System.err.println("移动失败: " + dup + " -> " + e.getMessage());
        }
    }
    return moved;
}
```

- [ ] **Step 2: 验证编译**

```bash
cd /mnt/d/IdeaWorkspaces/ddf-common && mvn compile -pl ddf-common-script -Dmaven.repo.local=/mnt/d/maven_repository
```

Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git -C /mnt/d/IdeaWorkspaces/ddf-common add ddf-common-script/src/main/java/com/ddf/boot/common/script/file/dedup/DedupService.java
git -C /mnt/d/IdeaWorkspaces/ddf-common commit -m "refactor(toolbox): use SafeMoveService in DedupService"
```

---

### Task 3: 重构 DedupApplication 主布局 + 提取 DedupPanel

**Files:**

- Create: `ddf-common-script/src/main/java/com/ddf/boot/common/script/file/dedup/ui/DedupPanel.java`
- Modify: `ddf-common-script/src/main/java/com/ddf/boot/common/script/file/dedup/DedupApplication.java`

- [ ] **Step 1: 创建 DedupPanel（将现有去重 UI 提取为独立面板）**

```java
package com.ddf.boot.common.script.file.dedup.ui;

import com.ddf.boot.common.script.file.dedup.DedupResult;
import com.ddf.boot.common.script.file.dedup.DedupService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class DedupPanel extends VBox {

    private final TextField scanDirField = new TextField();
    private final TextField outputDirField = new TextField();
    private final Button scanButton = new Button("扫描重复文件");
    private final Button processAllButton = new Button("全部整理");
    private final ProgressBar progressBar = new ProgressBar(0);
    private final Label statusLabel = new Label("就绪");
    private final ListView<DedupResult> resultList = new ListView<>();
    private final ObservableList<DedupResult> results = FXCollections.observableArrayList();

    private final ImageView previewImage = new ImageView();
    private final Label previewInfo = new Label();
    private final Label previewPath = new Label();

    private final DedupService service = new DedupService();
    private Path scanDir;
    private Path outputDir;

    public DedupPanel() {
        setPadding(new Insets(12));
        setSpacing(8);

        Label title = new Label("文件去重 (SHA-256)");
        title.setFont(Font.font(null, FontWeight.BOLD, 16));

        HBox scanRow = new HBox(8, new Label("扫描目录:"), scanDirField, createBrowseButton(scanDirField));
        HBox outputRow = new HBox(8, new Label("输出目录:"), outputDirField, createBrowseButton(outputDirField));
        HBox actionRow = new HBox(8, scanButton, progressBar, statusLabel);
        HBox.setHgrow(progressBar, Priority.ALWAYS);
        progressBar.setMaxWidth(Double.MAX_VALUE);

        scanButton.setOnAction(e -> startScan());

        resultList.setItems(results);
        resultList.setCellFactory(lv -> new DedupResultCell());
        VBox.setVgrow(resultList, Priority.ALWAYS);

        VBox previewPanel = buildPreviewPanel();

        processAllButton.setDisable(true);
        processAllButton.setOnAction(e -> processAll());

        SplitPane split = new SplitPane(resultList, previewPanel);
        split.setDividerPositions(0.65);
        VBox.setVgrow(split, Priority.ALWAYS);

        getChildren().addAll(title, scanRow, outputRow, actionRow, split, processAllButton);
    }

    private VBox buildPreviewPanel() {
        previewImage.setFitWidth(230);
        previewImage.setPreserveRatio(true);
        previewImage.setVisible(false);
        previewInfo.setFont(Font.font(null, 12));
        previewInfo.setWrapText(true);
        previewPath.setFont(Font.font(null, 11));
        previewPath.setWrapText(true);
        previewPath.setStyle("-fx-text-fill: gray;");

        Label title = new Label("预览");
        title.setFont(Font.font(null, FontWeight.BOLD, 13));

        VBox panel = new VBox(8, title, previewImage, previewInfo, previewPath);
        panel.setPadding(new Insets(12));
        panel.setPrefWidth(260);
        return panel;
    }

    private void showPreview(Path file) {
        previewImage.setVisible(false);
        previewImage.setImage(null);
        previewPath.setText(file.toString());

        String name = file.getFileName().toString();
        int dot = name.lastIndexOf('.');
        String ext = dot > 0 ? name.substring(dot + 1).toLowerCase() : "";
        boolean isImage = ext.matches("jpg|jpeg|png|gif|bmp|webp");

        try {
            long bytes = Files.size(file);
            String sizeStr;
            if (bytes < 1024 * 1024) {
                sizeStr = String.format("%.1f KB", bytes / 1024.0);
            } else if (bytes < 1024 * 1024 * 1024) {
                sizeStr = String.format("%.1f MB", bytes / (1024.0 * 1024));
            } else {
                sizeStr = String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
            }
            StringBuilder info = new StringBuilder();
            info.append("大小: ").append(sizeStr).append("\n");
            info.append("类型: ").append(ext.isEmpty() ? "未知" : ext.toUpperCase()).append("\n");
            if (com.ddf.boot.common.script.file.dedup.DedupService.hasBrackets(file.getFileName().toString())) {
                info.append("⚠ 文件名含括号");
            } else {
                info.append("✓ 文件名无括号");
            }
            previewInfo.setText(info.toString());

            if (isImage) {
                try {
                    Image img = new Image(file.toUri().toString(), 230, 0, true, true);
                    previewImage.setImage(img);
                    previewImage.setVisible(true);
                } catch (Exception ignored) {
                }
            }
        } catch (IOException ignored) {
            previewInfo.setText("无法读取文件信息");
        }
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
        progressBar.setProgress(0);
        statusLabel.setText("扫描中... 0%");

        new Thread(() -> {
            try {
                List<DedupResult> found = service.scan(scanDir,
                        p -> Platform.runLater(() -> {
                            progressBar.setProgress(p);
                            statusLabel.setText(String.format("扫描中... %.0f%%", p * 100));
                        }));
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

    private class DedupResultCell extends ListCell<DedupResult> {
        private final VBox container = new VBox(4);
        private final Label header = new Label();
        private final VBox fileListBox = new VBox(3);
        private final Button moveBtn = new Button("整理此组");
        private final ToggleGroup toggleGroup = new ToggleGroup();

        DedupResultCell() {
            header.setFont(Font.font(null, FontWeight.BOLD, 13));
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
            container.getChildren().addAll(header, fileListBox, moveBtn);
            container.setPadding(new Insets(6));
        }

        @Override
        protected void updateItem(DedupResult item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                header.setText(String.format("%s (%d个文件)", item.fileName(), item.files().size()));
                buildFileRadios(item);
                setGraphic(container);
            }
        }

        private void updateDisplay(DedupResult item) {
            buildFileRadios(item);
        }

        private void buildFileRadios(DedupResult item) {
            fileListBox.getChildren().clear();
            toggleGroup.getToggles().clear();
            for (int i = 0; i < item.files().size(); i++) {
                Path file = item.files().get(i);
                String label = file.toString();
                if (i == item.getKeptIndex()) {
                    label += "  ← 保留";
                } else {
                    label += "  ← 重复";
                }
                RadioButton rb = new RadioButton(label);
                rb.setToggleGroup(toggleGroup);
                rb.setFont(Font.font(null, 12));
                rb.setSelected(i == item.getKeptIndex());
                int idx = i;
                rb.setOnAction(e -> {
                    if (rb.isSelected()) {
                        item.setKeptIndex(idx);
                        updateDisplay(item);
                    }
                });
                Path openFile = file;
                rb.setOnMouseClicked(e -> {
                    if (e.getClickCount() == 2) {
                        try {
                            Desktop.getDesktop().open(openFile.toFile());
                        } catch (IOException ex) {
                            showAlert("无法打开文件: " + ex.getMessage());
                        }
                    } else {
                        showPreview(openFile);
                    }
                });
                fileListBox.getChildren().add(rb);
            }
        }
    }
}
```

- [ ] **Step 2: 重写 DedupApplication 为主布局（左侧导航 + 右侧面板切换）**

```java
package com.ddf.boot.common.script.file.dedup;

import com.ddf.boot.common.script.file.dedup.ui.DedupPanel;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.LinkedHashMap;
import java.util.Map;

public class DedupApplication extends Application {

    private static final String[] FUNCTION_NAMES = {
            "文件去重",
            "按拍摄时间归档",
            "监控视频文件归档",
            "监控录像目录压缩"
    };

    private final Map<String, VBox> panels = new LinkedHashMap<>();
    private final StackPane contentArea = new StackPane();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("文件工具箱");

        panels.put(FUNCTION_NAMES[0], new DedupPanel());
        panels.put(FUNCTION_NAMES[1], new com.ddf.boot.common.script.file.dedup.ui.PhotoArchivePanel());
        panels.put(FUNCTION_NAMES[2], new com.ddf.boot.common.script.file.dedup.ui.VideoArchivePanel());
        panels.put(FUNCTION_NAMES[3], new com.ddf.boot.common.script.file.dedup.ui.FolderCompressPanel());

        ListView<String> navList = new ListView<>();
        navList.getItems().addAll(FUNCTION_NAMES);
        navList.setPrefWidth(160);
        navList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setFont(Font.font(null, 14));
                setPadding(new Insets(10, 12, 10, 12));
            }
        });
        navList.getSelectionModel().select(0);
        switchPanel(FUNCTION_NAMES[0]);
        navList.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> switchPanel(val));

        BorderPane root = new BorderPane();
        root.setLeft(navList);
        root.setCenter(contentArea);

        Scene scene = new Scene(root, 1050, 600);
        stage.setScene(scene);
        stage.show();
    }

    private void switchPanel(String name) {
        VBox panel = panels.get(name);
        if (panel != null) {
            contentArea.getChildren().clear();
            contentArea.getChildren().add(panel);
        }
    }
}
```

- [ ] **Step 3: 验证编译**

Note: 此步骤会因 Task 4-6 的 Panel 类尚未创建而失败。需要先创建占位类。

先创建占位 Panel 类：

**PhotoArchivePanel.java:**

```java
package com.ddf.boot.common.script.file.dedup.ui;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class PhotoArchivePanel extends VBox {
    public PhotoArchivePanel() {
        getChildren().add(new Label("按拍摄时间归档 - 即将实现"));
    }
}
```

**VideoArchivePanel.java:**

```java
package com.ddf.boot.common.script.file.dedup.ui;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class VideoArchivePanel extends VBox {
    public VideoArchivePanel() {
        getChildren().add(new Label("监控视频文件归档 - 即将实现"));
    }
}
```

**FolderCompressPanel.java:**

```java
package com.ddf.boot.common.script.file.dedup.ui;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class FolderCompressPanel extends VBox {
    public FolderCompressPanel() {
        getChildren().add(new Label("监控录像目录压缩 - 即将实现"));
    }
}
```

然后编译：

```bash
cd /mnt/d/IdeaWorkspaces/ddf-common && mvn compile -pl ddf-common-script -Dmaven.repo.local=/mnt/d/maven_repository
```

Expected: BUILD SUCCESS

- [ ] **Step 4: 提交**

```bash
git -C /mnt/d/IdeaWorkspaces/ddf-common add ddf-common-script/src/main/java/com/ddf/boot/common/script/file/dedup/ui/ ddf-common-script/src/main/java/com/ddf/boot/common/script/file/dedup/DedupApplication.java
git -C /mnt/d/IdeaWorkspaces/ddf-common commit -m "refactor(toolbox): extract DedupPanel, add main layout with left nav"
```

---

### Task 4: 实现 PhotoArchivePanel（按拍摄时间归档）

**Files:**

- Modify: `ddf-common-script/src/main/java/com/ddf/boot/common/script/file/dedup/ui/PhotoArchivePanel.java`

- [ ] **Step 1: 实现完整 PhotoArchivePanel**

```java
package com.ddf.boot.common.script.file.dedup.ui;

import com.ddf.boot.common.script.file.dedup.SafeMoveService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class PhotoArchivePanel extends VBox {

    private final TextField sourceDirField = new TextField();
    private final TextField outputDirField = new TextField();
    private final Button startButton = new Button("开始归档");
    private final ProgressBar progressBar = new ProgressBar(0);
    private final Label statusLabel = new Label("就绪");

    public PhotoArchivePanel() {
        setPadding(new Insets(12));
        setSpacing(8);

        Label title = new Label("按拍摄时间归档");
        title.setFont(Font.font(null, FontWeight.BOLD, 16));

        Label desc = new Label("按文件创建时间（月份）归档。VID 开头的文件归入月份目录，其它文件归入 not_vid 目录。");
        desc.setWrapText(true);
        desc.setFont(Font.font(null, 12));

        HBox srcRow = new HBox(8, new Label("源目录:"), sourceDirField, createBrowseButton(sourceDirField));
        HBox outRow = new HBox(8, new Label("输出目录:"), outputDirField, createBrowseButton(outputDirField));
        HBox actionRow = new HBox(8, startButton, progressBar, statusLabel);
        HBox.setHgrow(progressBar, Priority.ALWAYS);
        progressBar.setMaxWidth(Double.MAX_VALUE);

        startButton.setOnAction(e -> startArchive());

        getChildren().addAll(title, desc, srcRow, outRow, actionRow);
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

    private void startArchive() {
        String srcText = sourceDirField.getText().trim();
        String outText = outputDirField.getText().trim();
        if (srcText.isEmpty() || outText.isEmpty()) {
            showAlert("请先选择源目录和输出目录");
            return;
        }
        Path srcDir = Path.of(srcText);
        Path outDir = Path.of(outText);

        startButton.setDisable(true);
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        statusLabel.setText("归档中...");

        new Thread(() -> {
            Path notVidDir = outDir.resolve("not_vid");
            try {
                Files.walkFileTree(srcDir, new java.nio.file.SimpleFileVisitor<>() {
                    @Override
                    public java.nio.file.FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        String fileName = file.getFileName().toString();
                        if (!fileName.startsWith("VID")) {
                            Files.createDirectories(notVidDir);
                            Path dest = notVidDir.resolve(fileName);
                            SafeMoveService.move(file, dest);
                        } else {
                            String month;
                            if (fileName.startsWith("VID_")) {
                                month = fileName.substring(4, 10);
                            } else {
                                month = fileName.substring(3, 9);
                            }
                            Path monthDir = outDir.resolve(month);
                            Files.createDirectories(monthDir);
                            Path dest = monthDir.resolve(fileName);
                            SafeMoveService.move(file, dest);
                        }
                        return java.nio.file.FileVisitResult.CONTINUE;
                    }
                });
                Platform.runLater(() -> {
                    statusLabel.setText("归档完成");
                    progressBar.setProgress(1);
                    startButton.setDisable(false);
                });
            } catch (IOException ex) {
                Platform.runLater(() -> {
                    showAlert("归档失败: " + ex.getMessage());
                    startButton.setDisable(false);
                    progressBar.setProgress(0);
                    statusLabel.setText("归档失败");
                });
            }
        }).start();
    }
}
```

- [ ] **Step 2: 编译验证**

```bash
cd /mnt/d/IdeaWorkspaces/ddf-common && mvn compile -pl ddf-common-script -Dmaven.repo.local=/mnt/d/maven_repository
```

Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git -C /mnt/d/IdeaWorkspaces/ddf-common add ddf-common-script/src/main/java/com/ddf/boot/common/script/file/dedup/ui/PhotoArchivePanel.java
git -C /mnt/d/IdeaWorkspaces/ddf-common commit -m "feat(toolbox): implement PhotoArchivePanel"
```

---

### Task 5: 实现 VideoArchivePanel（监控视频文件归档）

**Files:**

- Modify: `ddf-common-script/src/main/java/com/ddf/boot/common/script/file/dedup/ui/VideoArchivePanel.java`

- [ ] **Step 1: 实现完整 VideoArchivePanel**

```java
package com.ddf.boot.common.script.file.dedup.ui;

import com.ddf.boot.common.script.file.dedup.SafeMoveService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class VideoArchivePanel extends VBox {

    private final TextField sourceDirField = new TextField();
    private final TextField outputDirField = new TextField();
    private final Button startButton = new Button("开始归档");
    private final ProgressBar progressBar = new ProgressBar(0);
    private final Label statusLabel = new Label("就绪");

    public VideoArchivePanel() {
        setPadding(new Insets(12));
        setSpacing(8);

        Label title = new Label("监控视频文件归档");
        title.setFont(Font.font(null, FontWeight.BOLD, 16));

        Label desc = new Label("适用于文件名格式 video_编号_0_10_日期_日期的文件。按 _ 分割取第5段作为日期，归入 月/日/ 目录。日期校验：年份 ≥ 2000，月份 01-12。");
        desc.setWrapText(true);
        desc.setFont(Font.font(null, 12));

        HBox srcRow = new HBox(8, new Label("源目录:"), sourceDirField, createBrowseButton(sourceDirField));
        HBox outRow = new HBox(8, new Label("输出目录:"), outputDirField, createBrowseButton(outputDirField));
        HBox actionRow = new HBox(8, startButton, progressBar, statusLabel);
        HBox.setHgrow(progressBar, Priority.ALWAYS);
        progressBar.setMaxWidth(Double.MAX_VALUE);

        startButton.setOnAction(e -> startArchive());

        getChildren().addAll(title, desc, srcRow, outRow, actionRow);
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

    private void startArchive() {
        String srcText = sourceDirField.getText().trim();
        String outText = outputDirField.getText().trim();
        if (srcText.isEmpty() || outText.isEmpty()) {
            showAlert("请先选择源目录和输出目录");
            return;
        }
        Path srcDir = Path.of(srcText);
        Path outDir = Path.of(outText);

        startButton.setDisable(true);
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        statusLabel.setText("归档中...");

        new Thread(() -> {
            try {
                Files.walkFileTree(srcDir, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        String fileName = file.getFileName().toString();
                        if (!fileName.startsWith("video")) {
                            return FileVisitResult.CONTINUE;
                        }
                        String[] split = fileName.split("_");
                        String dateStr = split[4];
                        String month = dateStr.substring(0, 6);
                        String day = dateStr.substring(0, 8);

                        int year = Integer.parseInt(month.substring(0, 4));
                        int mon = Integer.parseInt(month.substring(4, 6));
                        if (year < 2000 || mon < 1 || mon > 12) {
                            System.err.println("FATAL: 日期解析异常, 文件名=" + fileName + " dateStr=" + dateStr);
                            System.exit(1);
                        }

                        Path targetPath = outDir.resolve(month).resolve(day);
                        Files.createDirectories(targetPath);
                        SafeMoveService.move(file, targetPath.resolve(fileName));
                        return FileVisitResult.CONTINUE;
                    }
                });
                Platform.runLater(() -> {
                    statusLabel.setText("归档完成");
                    progressBar.setProgress(1);
                    startButton.setDisable(false);
                });
            } catch (IOException ex) {
                Platform.runLater(() -> {
                    showAlert("归档失败: " + ex.getMessage());
                    startButton.setDisable(false);
                    progressBar.setProgress(0);
                    statusLabel.setText("归档失败");
                });
            }
        }).start();
    }
}
```

- [ ] **Step 2: 编译验证**

```bash
cd /mnt/d/IdeaWorkspaces/ddf-common && mvn compile -pl ddf-common-script -Dmaven.repo.local=/mnt/d/maven_repository
```

Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git -C /mnt/d/IdeaWorkspaces/ddf-common add ddf-common-script/src/main/java/com/ddf/boot/common/script/file/dedup/ui/VideoArchivePanel.java
git -C /mnt/d/IdeaWorkspaces/ddf-common commit -m "feat(toolbox): implement VideoArchivePanel"
```

---

### Task 6: 实现 FolderCompressPanel（监控录像目录压缩）

**Files:**

- Modify: `ddf-common-script/src/main/java/com/ddf/boot/common/script/file/dedup/ui/FolderCompressPanel.java`

- [ ] **Step 1: 实现完整 FolderCompressPanel**

```java
package com.ddf.boot.common.script.file.dedup.ui;

import com.ddf.boot.common.script.file.dedup.SafeMoveService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FolderCompressPanel extends VBox {

    private static final String[] IMAGE_EXT = {"jpg", "jpeg", "png", "gif", "bmp"};

    private final TextField sourceDirField = new TextField();
    private final TextField outputDirField = new TextField();
    private final Button startButton = new Button("开始压缩");
    private final ProgressBar progressBar = new ProgressBar(0);
    private final Label statusLabel = new Label("就绪");

    public FolderCompressPanel() {
        setPadding(new Insets(12));
        setSpacing(8);

        Label title = new Label("监控录像目录压缩");
        title.setFont(Font.font(null, FontWeight.BOLD, 16));

        Label desc = new Label("适用于时间戳命名的文件夹（如 2024011220）。前6位→月，前8位→日。目标: 月/日/原文件夹/。日期校验：年份 ≥ 2000，月份 01-12。子目录中图片将被删除。");
        desc.setWrapText(true);
        desc.setFont(Font.font(null, 12));

        HBox srcRow = new HBox(8, new Label("源目录:"), sourceDirField, createBrowseButton(sourceDirField));
        HBox outRow = new HBox(8, new Label("输出目录:"), outputDirField, createBrowseButton(outputDirField));
        HBox actionRow = new HBox(8, startButton, progressBar, statusLabel);
        HBox.setHgrow(progressBar, Priority.ALWAYS);
        progressBar.setMaxWidth(Double.MAX_VALUE);

        startButton.setOnAction(e -> startCompress());

        getChildren().addAll(title, desc, srcRow, outRow, actionRow);
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

    private void startCompress() {
        String srcText = sourceDirField.getText().trim();
        String outText = outputDirField.getText().trim();
        if (srcText.isEmpty() || outText.isEmpty()) {
            showAlert("请先选择源目录和输出目录");
            return;
        }
        File srcDir = new File(srcText);
        Path outDir = Path.of(outText);

        startButton.setDisable(true);
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        statusLabel.setText("压缩中...");

        new Thread(() -> {
            try {
                File[] folders = srcDir.listFiles(File::isDirectory);
                if (folders != null) {
                    for (File folder : folders) {
                        String name = folder.getName();
                        String month = name.substring(0, Math.min(name.length(), 6));
                        String day = name.substring(0, Math.min(name.length(), 8));

                        int year = Integer.parseInt(month.substring(0, 4));
                        int mon = Integer.parseInt(month.substring(4, 6));
                        if (year < 2000 || mon < 1 || mon > 12) {
                            System.err.println("FATAL: 日期解析异常, folderName=" + name);
                            System.exit(1);
                        }

                        Path targetPath = outDir.resolve(month).resolve(day).resolve(name);
                        Files.createDirectories(targetPath.getParent());
                        deleteImageFilesRecursively(folder);
                        SafeMoveService.move(folder.toPath(), targetPath);
                    }
                }
                Platform.runLater(() -> {
                    statusLabel.setText("压缩完成");
                    progressBar.setProgress(1);
                    startButton.setDisable(false);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    showAlert("压缩失败: " + ex.getMessage());
                    startButton.setDisable(false);
                    progressBar.setProgress(0);
                    statusLabel.setText("压缩失败");
                });
            }
        }).start();
    }

    private void deleteImageFilesRecursively(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteImageFilesRecursively(file);
                } else if (isImageFile(file)) {
                    file.delete();
                }
            }
        }
    }

    private boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        for (String ext : IMAGE_EXT) {
            if (name.endsWith("." + ext)) {
                return true;
            }
        }
        return false;
    }
}
```

- [ ] **Step 2: 编译验证**

```bash
cd /mnt/d/IdeaWorkspaces/ddf-common && mvn compile -pl ddf-common-script -Dmaven.repo.local=/mnt/d/maven_repository
```

Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git -C /mnt/d/IdeaWorkspaces/ddf-common add ddf-common-script/src/main/java/com/ddf/boot/common/script/file/dedup/ui/FolderCompressPanel.java
git -C /mnt/d/IdeaWorkspaces/ddf-common commit -m "feat(toolbox): implement FolderCompressPanel"
```

---

### Task 7: 最终编译验证

- [ ] **Step 1: 编译**

```bash
cd /mnt/d/IdeaWorkspaces/ddf-common && mvn compile -pl ddf-common-script -Dmaven.repo.local=/mnt/d/maven_repository
```

Expected: BUILD SUCCESS
