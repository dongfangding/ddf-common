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
        startButton.setDisable(true);
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        statusLabel.setText("归档中...");

        new Thread(() -> {
            try {
                com.ddf.boot.common.script.file.FileRestore.computerReadAndMoveFileToMonth(
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
