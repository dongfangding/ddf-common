package com.ddf.boot.common.script.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileNamePrefixReplacer {

    /**
     * 入口方法：递归处理所有文件
     *
     * @param folderPath  根文件夹路径
     * @param matchPrefix 要匹配的文件名前缀
     * @param newPrefix   要替换的新前缀
     */
    public static void replaceFileNamePrefix(String folderPath, String matchPrefix, String newPrefix) {
        File rootFolder = new File(folderPath);

        if (!rootFolder.exists() || !rootFolder.isDirectory()) {
            System.err.println("❌ 目标路径不存在或不是文件夹: " + folderPath);
            return;
        }

        processFolderRecursively(rootFolder, matchPrefix, newPrefix);
    }

    /**
     * 递归处理文件夹
     */
    private static void processFolderRecursively(File folder, String matchPrefix, String newPrefix) {
        File[] files = folder.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                // 递归处理子目录
                processFolderRecursively(file, matchPrefix, newPrefix);
            } else if (file.isFile()) {
                String name = file.getName();

                if (name.startsWith(matchPrefix)) {
                    String newName = newPrefix + name.substring(matchPrefix.length());
                    File newFile = new File(file.getParentFile(), newName);

                    boolean renamed = file.renameTo(newFile);
                    if (renamed) {
                        System.out.println("✅ 文件重命名成功: " + name + " → " + newName);
                        replaceClassNameInsideFile(newFile, matchPrefix, newPrefix);
                    } else {
                        System.err.println("❌ 文件重命名失败: " + name);
                    }
                }
            }
        }
    }

    /**
     * 替换文件内部的 public class/interface/enum 后跟的前缀
     */
    public static void replaceClassNameInsideFile(File file, String matchPrefix, String newPrefix) {
        File tempFile = new File(file.getAbsolutePath() + ".tmp");

        try (
            BufferedReader reader = new BufferedReader(new FileReader(file));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();

                // 检查是否以 public (static)? (class|interface|enum) 开头
                if (matchesClassDeclaration(trimmed, matchPrefix)) {
                    // 动态找出需要保留的部分（public ... class/interface/enum）
                    String leadingPart = getLeadingDeclarationPart(trimmed);
                    if (leadingPart != null) {
                        // 替换掉类名的前缀
                        String oldClassNamePart = matchPrefix;
                        String newClassNamePart = newPrefix;
                        // 找到 leadingPart 后，剩下的就是类名，进行替换
                        line = line.replaceFirst(leadingPart + "\\s+" + oldClassNamePart,
                                                 leadingPart + " " + newClassNamePart);
                        System.out.println("🔧 替换类名: " + line.trim());
                    }
                }

                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("❌ 替换内容失败: " + file.getName() + " - " + e.getMessage());
            return;
        }

        // 替换原文件
        if (!file.delete() || !tempFile.renameTo(file)) {
            System.err.println("❌ 替换文件失败: " + file.getName());
        }
    }

    /**
     * 判断一行是否匹配 class/interface/enum 且后面带有匹配前缀
     */
    private static boolean matchesClassDeclaration(String line, String matchPrefix) {
        // 正则匹配:
        // public [static] (class|interface|enum) 空格 + matchPrefix
        return line.matches("^public(\\s+static)?\\s+(class|interface|enum)\\s+" + matchPrefix + ".*");
    }

    /**
     * 获取声明部分（例如 "public static class"、"public interface"）
     */
    private static String getLeadingDeclarationPart(String line) {
        String[] types = {"class", "interface", "enum"};

        for (String type : types) {
            int index = line.indexOf(" " + type + " ");
            if (index != -1) {
                // 找到 "class"、"interface" 或 "enum" 之前的部分，返回完整的 public static class
                return line.substring(0, index + type.length() + 1).trim();
            }
        }
        return null;
    }

    public static void main(String[] args) {
        replaceFileNamePrefix("D:\\IdeaWorkspaces\\seaway\\game\\game-core\\src\\main\\java\\com\\kewta\\biz\\game\\core\\domain\\jinhua", "TigerLoong", "JinHua");
    }
}
