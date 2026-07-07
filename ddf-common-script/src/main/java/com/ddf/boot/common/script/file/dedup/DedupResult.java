package com.ddf.boot.common.script.file.dedup;

import java.nio.file.Path;
import java.util.List;

public record DedupResult(
        String fileName,
        String hash,
        List<Path> files
) {
    public boolean hasDuplicates() {
        return files.size() > 1;
    }

    public Path kept() {
        return files.get(0);
    }

    public List<Path> duplicates() {
        return files.subList(1, files.size());
    }
}
