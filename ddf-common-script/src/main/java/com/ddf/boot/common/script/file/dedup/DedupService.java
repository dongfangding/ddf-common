package com.ddf.boot.common.script.file.dedup;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DedupService {

    public List<DedupResult> scan(Path scanDir) throws IOException {
        List<Path> allFiles;
        try (Stream<Path> stream = Files.walk(scanDir)) {
            allFiles = stream
                    .filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .collect(Collectors.toList());
        }

        Map<String, List<Path>> hashGroups = new LinkedHashMap<>();
        MessageDigest md = getDigest();
        byte[] buffer = new byte[8192];
        int total = allFiles.size();
        int processed = 0;

        for (Path file : allFiles) {
            try {
                String hash = computeHash(file, md, buffer);
                hashGroups.computeIfAbsent(hash, k -> new ArrayList<>()).add(file);
            } catch (IOException e) {
                System.err.println("跳过无法读取的文件: " + file);
            }
            processed++;
            if (processed % 100 == 0 || processed == total) {
                System.out.printf("\r扫描进度: %d/%d", processed, total);
            }
        }
        System.out.println();

        return hashGroups.entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .map(e -> new DedupResult(
                        e.getValue().get(0).getFileName().toString(),
                        e.getKey(),
                        e.getValue()))
                .sorted(Comparator.comparing(DedupResult::fileName))
                .collect(Collectors.toList());
    }

    public int moveDuplicates(DedupResult result, Path outputDir) throws IOException {
        String keptName = result.kept().getFileName().toString();
        int lastDot = keptName.lastIndexOf('.');
        String nameWithoutExt = lastDot > 0 ? keptName.substring(0, lastDot) : keptName;

        Path targetDir = resolveUniqueDir(outputDir, "重复自_" + nameWithoutExt);
        Files.createDirectories(targetDir);

        int moved = 0;
        for (Path dup : result.duplicates()) {
            try {
                Path dest = resolveUniqueFile(targetDir, dup.getFileName().toString());
                Files.move(dup, dest);
                moved++;
            } catch (IOException e) {
                System.err.println("移动失败: " + dup + " -> " + e.getMessage());
            }
        }
        return moved;
    }

    private Path resolveUniqueDir(Path parent, String name) {
        Path candidate = parent.resolve(name);
        if (!Files.exists(candidate)) {
            return candidate;
        }
        for (int i = 1; ; i++) {
            candidate = parent.resolve(name + "_" + i);
            if (!Files.exists(candidate)) {
                return candidate;
            }
        }
    }

    private Path resolveUniqueFile(Path dir, String fileName) {
        Path candidate = dir.resolve(fileName);
        if (!Files.exists(candidate)) {
            return candidate;
        }
        int lastDot = fileName.lastIndexOf('.');
        String base = lastDot > 0 ? fileName.substring(0, lastDot) : fileName;
        String ext = lastDot > 0 ? fileName.substring(lastDot) : "";
        for (int i = 1; ; i++) {
            candidate = dir.resolve(base + "_" + i + ext);
            if (!Files.exists(candidate)) {
                return candidate;
            }
        }
    }

    private String computeHash(Path file, MessageDigest md, byte[] buffer) throws IOException {
        md.reset();
        try (InputStream in = Files.newInputStream(file)) {
            int n;
            while ((n = in.read(buffer)) != -1) {
                md.update(buffer, 0, n);
            }
        }
        return HexFormat.of().formatHex(md.digest());
    }

    private static MessageDigest getDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
