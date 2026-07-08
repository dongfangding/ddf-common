package com.ddf.boot.common.script.file.dedup.ui;

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

        Label desc = new Label("适用于文件名格式 video_编号_0_10_日期_日期的文件。按 _ 分割取第5段作为日期，归入 月/日/ 目录。日期校验：年份 >= 2000，月份 01-12。");
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
        startButton.setDisable(true);
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        statusLabel.setText("归档中...");

        new Thread(() -> {
            try {
                com.ddf.boot.common.script.file.FileRestore.packageMonitorVideo2(
                        new String[]{srcText}, outText);
                Platform.runLater(() -> {
                    statusLabel.setText("归档完成");
                    progressBar.setProgress(1);
                    startButton.setDisable(false);
                });
            } catch (Exception ex) {
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
