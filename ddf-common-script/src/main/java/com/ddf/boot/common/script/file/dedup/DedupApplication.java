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


    /**
     *
     * # 开发运行（WSL/Linux）
     *   cd /mnt/d/IdeaWorkspaces/ddf-common && mvn javafx:run -pl ddf-common-script -Dmaven.repo.local=/mnt/d/maven_repository
     * <p>
     *   # CLI 模式
     *   java -cp ddf-common-script/target/classes com.ddf.boot.common.script.file.FileRestore <month|video|compress> <源目录> <输出目录>
     *   java -cp ddf-common-script/target/classes com.ddf.boot.common.script.file.FileNamePrefixReplacer <文件夹> <匹配前缀> <新前缀>
     * <p>
     *   打包 Windows exe
     * <p>
     *   在 Windows 上执行：
     *   # 1. 打 uber jar（包含所有依赖）
     *   cd d:/IdeaWorkspaces/ddf-common
     *   mvn clean package -pl ddf-common-script -DskipTests
     * <p>
     *   # 2. 生成 exe（需 JDK 17+，自带 jpackage）
     *   jpackage --input ddf-common-script/target/ddf-common-script-dist --main-jar ddf-common-script.jar --main-class com.ddf.boot.common.script.file.dedup.Launcher --name DupTool --type app-image --dest out
     * <p>
     *   生成的 out/文件工具箱.exe 双击即用，无需装 Java
     *
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            launch(args);
        } catch (Throwable t) {
            try {
                java.nio.file.Path logFile = java.nio.file.Path.of("").toAbsolutePath().resolve("DupTool_error.log");
                java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(logFile.toFile()));
                t.printStackTrace(pw);
                pw.flush();
                pw.close();
            } catch (Exception ignored) {
            }
            throw t;
        }
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
