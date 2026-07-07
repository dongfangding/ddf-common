package com.ddf.boot.common.script.file.dedup;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DedupResult {

    private final String fileName;
    private final String hash;
    private final List<Path> files;
    private int keptIndex;

    public DedupResult(String fileName, String hash, List<Path> files) {
        this.fileName = fileName;
        this.hash = hash;
        this.files = List.copyOf(files);
        this.keptIndex = 0;
    }

    public String fileName() { return fileName; }
    public String hash() { return hash; }
    public List<Path> files() { return files; }

    public int getKeptIndex() { return keptIndex; }
    public void setKeptIndex(int keptIndex) { this.keptIndex = keptIndex; }

    public Path kept() { return files.get(keptIndex); }

    public List<Path> duplicates() {
        List<Path> result = new ArrayList<>(files);
        result.remove(keptIndex);
        return result;
    }
}
