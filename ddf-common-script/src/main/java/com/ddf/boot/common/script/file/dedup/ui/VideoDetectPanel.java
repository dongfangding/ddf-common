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
import java.util.LinkedHashSet;
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
        if (!Files.isDirectory(root)) {
            showAlert("所选路径不是有效目录");
            return;
        }

        scanButton.setDisable(true);
        results.clear();
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        statusLabel.setText("扫描中...");

        new Thread(() -> {
            try {
                LinkedHashSet<Path> dirs = new LinkedHashSet<>();
                Files.walkFileTree(root, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        if (isVideoFile(file)) {
                            dirs.add(file.getParent());
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) {
                        return FileVisitResult.CONTINUE;
                    }
                });
                Platform.runLater(() -> {
                    results.addAll(dirs.stream().sorted().toList());
                    statusLabel.setText(String.format("完成 - 找到 %d 个含视频目录", results.size()));
                    progressBar.setProgress(1);
                    scanButton.setDisable(false);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    showAlert("扫描失败: " + ex.getMessage());
                    scanButton.setDisable(false);
                    progressBar.setProgress(0);
                    statusLabel.setText("扫描失败");
                });
            }
        }).start();
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
