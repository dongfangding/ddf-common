package com.ddf.boot.common.script.file.dedup;

import com.ddf.boot.common.script.file.dedup.ui.DedupPanel;
import com.ddf.boot.common.script.file.dedup.ui.FolderCompressPanel;
import com.ddf.boot.common.script.file.dedup.ui.PhotoArchivePanel;
import com.ddf.boot.common.script.file.dedup.ui.VideoArchivePanel;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
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
        panels.put(FUNCTION_NAMES[1], new PhotoArchivePanel());
        panels.put(FUNCTION_NAMES[2], new VideoArchivePanel());
        panels.put(FUNCTION_NAMES[3], new FolderCompressPanel());

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
