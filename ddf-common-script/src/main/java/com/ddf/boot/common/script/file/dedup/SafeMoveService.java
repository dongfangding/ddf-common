package com.ddf.boot.common.script.file.dedup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class SafeMoveService {

    public static void move(Path source, Path dest) throws IOException {
        Files.move(source, dest);
        verifyMove(source, dest);
    }

    public static void move(Path source, Path dest, StandardCopyOption... options) throws IOException {
        Files.move(source, dest, options);
        verifyMove(source, dest);
    }

    private static void verifyMove(Path source, Path dest) {
        if (!Files.exists(dest)) {
            System.err.println("FATAL: 文件移动后目标不存在! source=" + source + " dest=" + dest);
            System.exit(1);
        }
    }
}
