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

        Label desc = new Label("按拍摄时间（月份）归档，不支持的文件归入 not_vid 目录。\n"
                + "支持从文件名解析的格式：\n"
                + "  • VID20250101...         → 第4-9位为月份（yyyyMM）\n"
                + "  • VID_20250101...        → 第5-10位为月份\n"
                + "  • VID_20250101_120000_00_001.mp4 → 第5-10位为月份\n"
                + "  • VID_20250101_120000.mp4 → 第5-10位为月份\n"
                + "  • VID20250101120000.mp4   → 第4-9位为月份\n"
                + "  • PRO_VID_20250101_120000_00_001.mp4 → 第9-14位为月份\n"
                + "  • 20240824165007_000205.MP4  → 第1-8位为月份\n"
                + "  • Record_2024-08-28-19-08-36.mp4 → 归入对应月份下的\"录屏\"子目录\n"
                + "  • 图片（jpg/jpeg/png/gif/bmp）→ 归入\"图片\"子目录\n"
                + "  • DJI_*                  → 读取文件实际创建时间\n"
                + "  • lv_0_20230902212343.mp4 → 归入\"剪辑\"子目录\n"
                + "  • TG-2024-05-02-142222410.mp4 → 归入\"剪辑\"子目录\n"
                + "  • share_1cd17aed...mp4    → 归入\"网络分享\"子目录\n"
                + "  • 解析出日期但不合法/不合理（如 2月31日、未来年份）→ 不处理，清单见 日期不合理文件.txt");
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
                com.ddf.boot.common.script.file.FileRestore.ArchiveResult result =
                        com.ddf.boot.common.script.file.FileRestore.computerReadAndMoveFileToMonth(
                                new String[]{srcText}, outText);
                Platform.runLater(() -> {
                    progressBar.setProgress(1);
                    startButton.setDisable(false);

                    Alert info = new Alert(Alert.AlertType.INFORMATION);
                    info.setTitle("归档完成");
                    info.setHeaderText("归档结果统计");
                    TextArea reportArea = new TextArea(result.toReport());
                    reportArea.setEditable(false);
                    reportArea.setWrapText(true);
                    info.getDialogPane().setContent(reportArea);
                    info.showAndWait();

                    statusLabel.setText("归档完成，共 " + result.getTotalFiles() + " 个文件");
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
