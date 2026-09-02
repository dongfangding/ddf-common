package com.ddf.boot.common.script.file.dedup;

import com.ddf.boot.common.script.file.dedup.ui.DedupPanel;
import com.ddf.boot.common.script.file.dedup.ui.FolderCompressPanel;
import com.ddf.boot.common.script.file.dedup.ui.PhotoArchivePanel;
import com.ddf.boot.common.script.file.dedup.ui.VideoArchivePanel;
import com.ddf.boot.common.script.file.dedup.ui.VideoDetectPanel;
import java.util.LinkedHashMap;
import java.util.Map;
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

public class DedupApplication extends Application {

    private static final String[] FUNCTION_NAMES =
            {"文件去重", "按拍摄时间归档", "监控视频文件归档", "监控录像目录压缩", "视频目录检测"};

    private final Map<String, VBox> panels = new LinkedHashMap<>();
    private final StackPane contentArea = new StackPane();


    /**
     * 开发运行（WSL/Linux）:
     * mvn javafx:run -pl ddf-common-script -Dmaven.repo.local=/mnt/d/maven_repository
     * 打包成可执行文件，注意在wsl下则生成bin，windows下生成.exe，要在对应平台下执行
     * # 1. Maven 打包（产出 ddf-common-script-dist/ 目录）
     * mvn clean package -pl ddf-common-script -DskipTests -Dmaven.repo.local=D:/maven_repository
     * # 2. jpackage 生成 .exe
     * jpackage --input ddf-common-script/target/ddf-common-script-dist --main-jar ddf-common-script.jar --main-class com.ddf.boot.common.script.file.dedup.Launcher --name DupTool --type app-image --dest out
     * 生成的 out/DupTool-1.0.exe 双击安装，安装后即可运行
     * # jpackage 可选参数:
     * #   --type msi         生成 .msi 安装包
     * #   --type app-image   生成免安装目录（含 .exe 启动器）
     * #   --win-console      显示控制台窗口（调试用，去掉则不显示黑窗口）
     * CLI 模式（不启动 GUI）:
     * java -cp ddf-common-script/target/classes com.ddf.boot.common.script.file.FileRestore month <源目录> <输出目录>
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
        panels.put(FUNCTION_NAMES[4], new VideoDetectPanel());

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
