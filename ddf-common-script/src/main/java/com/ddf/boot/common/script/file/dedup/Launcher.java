package com.ddf.boot.common.script.file.dedup;

/**
 * jpackage 启动入口。不能直接继承 Application，否则 JavaFX classpath 模式初始化失败。
 */
public class Launcher {
    public static void main(String[] args) {
        DedupApplication.main(args);
    }
}
