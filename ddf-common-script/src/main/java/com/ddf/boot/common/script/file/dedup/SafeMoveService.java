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
