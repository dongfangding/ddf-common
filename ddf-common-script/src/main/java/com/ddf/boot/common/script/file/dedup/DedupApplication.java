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
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.awt.Desktop;
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


    /**
     * cd /mnt/d/IdeaWorkspaces/ddf-common && mvn javafx:run -pl ddf-common-script -Dmaven.repo.local=/mnt/d/maven_repository
     *
     *   如果需要在 IDE 里直接运行，在 IntelliJ IDEA 的 Run Configuration 里添加 VM options：
     *
     *   --module-path /mnt/d/maven_repository/org/openjfx/javafx-controls/17.0.14/javafx-controls-17.0.14-linux.jar:/mnt/d/maven_repository/org/openjfx/javafx-graphics/17.0.14/javafx-graphics-17.0.14-linux.jar:/mnt/d/maven_repository/org/openj
     *   fx/javafx-base/17.0.14/javafx-base-17.0.14-linux.jar
     *   --add-modules javafx.controls
     *
     * @param args
     */
    public static void main(String[] args) {
        launch(args);
    }

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
                    }
                });
                fileListBox.getChildren().add(rb);
            }
        }
    }
}
