package com.ddf.boot.common.script.file;

import com.ddf.boot.common.script.file.dedup.SafeMoveService;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2024/07/09 10:54
 */
public class FileRestore {
    /**
     * @param args [0]功能标识 [1]源目录(逗号分隔多个) [2]输出目录
     *             功能标识: month(按月份归档), video(监控视频归档), compress(目录压缩)
     */
    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("用法: java FileRestore <month|video|compress> <源目录(多个用逗号分隔)> <输出目录>");
            System.exit(1);
        }
        String mode = args[0];
        String[] srcDirs = args[1].split(",");
        String outDir = args[2];
        switch (mode) {
            case "month" -> {
                ArchiveResult result = computerReadAndMoveFileToMonth(srcDirs, outDir);
                System.out.println(result.toReport());
            }
            case "video" -> packageMonitorVideo2(srcDirs, outDir);
            case "compress" -> packageMonitorVideo(srcDirs, outDir);
            default -> {
                System.err.println("未知功能: " + mode + ", 可选: month/video/compress");
                System.exit(1);
            }
        }
    }

    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyyMM")
            .withZone(ZoneId.systemDefault());

    /**
     * 按拍摄时间将文件归档到月份目录。
     * <p>支持的文件名格式（从文件名解析日期）：</p>
     * <ul>
     *   <li>VID20250101... — 从文件名第4-9位解析月份</li>
     *   <li>VID_20250101... — 从文件名第5-10位解析月份</li>
     *   <li>PRO_VID_20250101_120000_00_001.mp4 — 从文件名第9-14位解析月份</li>
     *   <li>VID_20250101_120000_00_001.mp4 — 从文件名第5-10位解析月份</li>
     *   <li>VID_20250101_120000.mp4 — 从文件名第5-10位解析月份</li>
     *   <li>VID20250101120000.mp4 — 从文件名第4-9位解析月份</li>
     *   <li>yyyyMMddHHmmss_xxxxxx.MP4（如 20240824165007_000205.MP4）— 从第1-8位解析月份</li>
     *   <li>Record_yyyy-MM-dd-HH-mm-ss.mp4（如 Record_2024-08-28-19-08-36.mp4）— 归入对应月份下的"录屏"子目录</li>
     *   <li>DJI_* — 读取文件实际创建时间，按创建月份归档</li>
     *   <li>lv_0_YYYYMMDDHHMMSS.mp4 / TG-2024-05-02-142222410.mp4 — 归入"剪辑"子目录</li>
     *   <li>share_1cd17aed...mp4 — 归入"网络分享"子目录</li>
     *   <li>图片文件（jpg/jpeg/png/gif/bmp） — 归入"图片"子目录</li>
     * </ul>
     * <p>无法识别的文件归入 not_vid 目录。</p>
     *
     * @param directories 源目录
     * @param baseTargetDirectory 输出目录
     * @return 归档结果统计
     */
    public static ArchiveResult computerReadAndMoveFileToMonth(String[] directories, String baseTargetDirectory) {
        ArchiveResult result = new ArchiveResult();
        result.sourceDirs = directories;
        result.targetDir = baseTargetDirectory;
        String notVidVideoPath = baseTargetDirectory + "/not_vid";
        Path targetRoot = Path.of(baseTargetDirectory).toAbsolutePath().normalize();
        for (String directory : directories) {
            try {
                Files.walkFileTree(Path.of(directory), new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        // 输出目录可能落在源目录内部，跳过其子树避免重跑时自处理已归档文件
                        if (dir.toAbsolutePath().normalize().equals(targetRoot)) {
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        String fileName = file.getFileName().toString();

                        Path targetDir;
                        if (isImageByExtension(fileName)) {
                            targetDir = Path.of(baseTargetDirectory, "图片");
                            result.imageFiles++;
                        } else if (fileName.startsWith("lv_") || fileName.startsWith("TG-")) {
                            targetDir = Path.of(baseTargetDirectory, "剪辑");
                            result.clipFiles++;
                        } else if (fileName.startsWith("share_")) {
                            targetDir = Path.of(baseTargetDirectory, "网络分享");
                            result.shareFiles++;
                        } else if (fileName.startsWith("Record_")) {
                            String dateStr = extractRecordDateString(fileName);
                            if (dateStr == null) {
                                targetDir = Path.of(notVidVideoPath);
                                result.unmatchedFiles++;
                            } else if (!isValidReasonableDate(dateStr)) {
                                result.invalidDateFiles++;
                                result.invalidDateNames.add(file.toString());
                                System.err.println("INVALID DATE: " + file + " 解析日期=" + dateStr);
                                return FileVisitResult.CONTINUE;
                            } else {
                                String month = dateStr.substring(0, 6);
                                targetDir = Path.of(baseTargetDirectory, month, "录屏");
                                result.recordFiles++;
                                result.matchedMonths.add(month);
                                result.matchedFiles++;
                            }
                        } else if (startsWithLeadingTimestamp(fileName)) {
                            String dateStr = fileName.substring(0, 8);
                            if (!isValidReasonableDate(dateStr)) {
                                result.invalidDateFiles++;
                                result.invalidDateNames.add(file.toString());
                                System.err.println("INVALID DATE: " + file + " 解析日期=" + dateStr);
                                return FileVisitResult.CONTINUE;
                            }
                            String month = dateStr.substring(0, 6);
                            targetDir = Path.of(baseTargetDirectory, month);
                            result.matchedMonths.add(month);
                            result.matchedFiles++;
                        } else if (fileName.startsWith("DJI_")) {
                            String month = MONTH_FMT.format(attrs.creationTime().toInstant());
                            targetDir = Path.of(baseTargetDirectory, month);
                            result.matchedMonths.add(month);
                            result.matchedFiles++;
                        } else {
                            String dateStr = extractDateString(fileName);
                            if (dateStr == null) {
                                targetDir = Path.of(notVidVideoPath);
                                result.unmatchedFiles++;
                            } else if (!isValidReasonableDate(dateStr)) {
                                result.invalidDateFiles++;
                                result.invalidDateNames.add(file.toString());
                                System.err.println("INVALID DATE: " + file + " 解析日期=" + dateStr);
                                return FileVisitResult.CONTINUE;
                            } else {
                                String month = dateStr.substring(0, 6);
                                targetDir = Path.of(baseTargetDirectory, month);
                                result.matchedMonths.add(month);
                                result.matchedFiles++;
                            }
                        }
                        if (!Files.exists(targetDir)) {
                            Files.createDirectories(targetDir);
                        }
                        Path targetPath = targetDir.resolve(file.getFileName());
                        SafeMoveService.move(file, targetPath, StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Moved " + file.getFileName() + " to " + targetPath);
                        result.totalFiles++;
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!result.invalidDateNames.isEmpty()) {
            Path invalidManifest = Path.of(baseTargetDirectory, "日期不合理文件.txt");
            try {
                Files.createDirectories(invalidManifest.getParent());
                Files.write(invalidManifest, result.invalidDateNames);
                System.err.println("已记录 " + result.invalidDateNames.size() + " 个日期不合理文件到: " + invalidManifest);
            } catch (IOException e) {
                System.err.println("WARN: 写入日期不合理文件清单失败: " + e.getMessage());
            }
        }
        return result;
    }

    /**
     * 归档结果统计。
     */
    public static class ArchiveResult {
        String[] sourceDirs;
        String targetDir;
        int totalFiles;
        int matchedFiles;
        int unmatchedFiles;
        int clipFiles;
        int shareFiles;
        int recordFiles;
        int imageFiles;
        int invalidDateFiles;
        final java.util.LinkedHashSet<String> matchedMonths = new java.util.LinkedHashSet<>();
        final java.util.List<String> invalidDateNames = new java.util.ArrayList<>();

        public int getTotalFiles() { return totalFiles; }
        public int getMatchedFiles() { return matchedFiles; }
        public int getUnmatchedFiles() { return unmatchedFiles; }
        public int getClipFiles() { return clipFiles; }
        public int getShareFiles() { return shareFiles; }
        public int getRecordFiles() { return recordFiles; }
        public int getImageFiles() { return imageFiles; }
        public int getInvalidDateFiles() { return invalidDateFiles; }

        public String toReport() {
            StringBuilder sb = new StringBuilder();
            sb.append("共迁移 ").append(totalFiles).append(" 个文件\n");
            sb.append("按月份匹配: ").append(matchedFiles).append(" 个");
            if (!matchedMonths.isEmpty()) {
                sb.append("，涉及月份: ").append(String.join("、", matchedMonths));
            }
            sb.append("\n");
            if (imageFiles > 0) {
                sb.append("图片文件: ").append(imageFiles).append(" 个，归入 图片 目录\n");
            }
            if (clipFiles > 0) {
                sb.append("剪辑文件: ").append(clipFiles).append(" 个，归入 剪辑 目录\n");
            }
            if (shareFiles > 0) {
                sb.append("网络分享: ").append(shareFiles).append(" 个，归入 网络分享 目录\n");
            }
            if (recordFiles > 0) {
                sb.append("录屏文件: ").append(recordFiles).append(" 个，归入 月份/录屏 目录\n");
            }
            sb.append("匹配失败: ").append(unmatchedFiles).append(" 个，归入 not_vid 目录\n");
            sb.append("日期不合理: ").append(invalidDateFiles).append(" 个，未处理");
            if (invalidDateFiles > 0) {
                sb.append("，清单见 日期不合理文件.txt");
            }
            sb.append("\n");
            sb.append("输出目录: ").append(targetDir);
            return sb.toString();
        }
    }

    /**
     * 从文件名提取 8 位日期串（yyyyMMdd），无法识别格式返回 null。
     * <ul>
     * <li>PRO_VID_YYYYMMDD_... → 取第9-16位</li>
     * <li>VID_YYYYMMDD...      → 取第5-12位</li>
     * <li>VIDYYYYMMDD...       → 取第4-11位</li>
     * </ul>
     */
    private static String extractDateString(String fileName) {
        String candidate = null;
        if (fileName.startsWith("PRO_VID_") && fileName.length() >= 16) {
            candidate = fileName.substring(8, 16);
        } else if (fileName.startsWith("VID_") && fileName.length() >= 12) {
            candidate = fileName.substring(4, 12);
        } else if (fileName.startsWith("VID") && fileName.length() >= 11) {
            candidate = fileName.substring(3, 11);
        }
        if (candidate == null || !candidate.chars().allMatch(Character::isDigit)) {
            return null;
        }
        return candidate;
    }

    /**
     * 从 Record_yyyy-MM-dd-HH-mm-ss.<ext> 格式的文件名提取 8 位日期串（yyyyMMdd）。
     * 例如 Record_2024-08-28-19-08-36.mp4 → "20240828"。无法识别返回 null。
     */
    private static String extractRecordDateString(String fileName) {
        if (!fileName.startsWith("Record_") || fileName.length() < 17) {
            return null;
        }
        String datePart = fileName.substring(7, 17);
        if (datePart.charAt(4) != '-' || datePart.charAt(7) != '-') {
            return null;
        }
        String digits = datePart.substring(0, 4) + datePart.substring(5, 7) + datePart.substring(8, 10);
        if (!digits.chars().allMatch(Character::isDigit)) {
            return null;
        }
        return digits;
    }

    /**
     * 判断文件名是否以 14 位数字开头（形如 yyyyMMddHHmmss_xxxxxx.MP4）。
     * 例如 20240824165007_000205.MP4。
     */
    private static boolean startsWithLeadingTimestamp(String fileName) {
        if (fileName.length() < 14) {
            return false;
        }
        return fileName.substring(0, 14).chars().allMatch(Character::isDigit);
    }

    /**
     * 校验日期是否合法且合理：完整日历校验（闰年、大小月）+ 年份范围 [2000, 当前年份]。
     */
    private static boolean isValidReasonableDate(String dateStr) {
        if (dateStr == null || dateStr.length() != 8) {
            return false;
        }
        try {
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.BASIC_ISO_DATE);
            int year = date.getYear();
            return year >= 2000 && year <= Year.now().getValue();
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private static final java.util.Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "bmp");

    private static boolean isImageByExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0) return false;
        return IMAGE_EXTENSIONS.contains(fileName.substring(dot + 1).toLowerCase());
    }


    /**
     * 适用于所有监控视频在一起的文件， 文件格式为video_0005_0_10_20240629145104_20240629145641.mp4
     * 解决问题， 按照文件名解析时间进行归档
     * 1. 一级目录到月202401
     * 2. 再创建二级目录到天20240112
     * 3. 再创建三级目录到小时2024011223
     * 3. 将源文件夹转移到三级目录下
     *
     * @param directories directories参数
     * @param baseTargetDirectory basetargetdirectory参数
     */
    public static void packageMonitorVideo2(String[] directories, String baseTargetDirectory) {
        for (String directory : directories) {
            try {
                Files.walkFileTree(Path.of(directory), new SimpleFileVisitor<>() {
                    /**
                     * @param file 参数
                     * @param attrs 参数
                     */
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        // video_0881_0_10_20240628194426_20240628195010.mp4
                        String fileName = file.getFileName().toString();
                        if (!fileName.startsWith("video")) {
                            return FileVisitResult.CONTINUE;
                        }
                        final String[] split = fileName.split("_");
                        if (split.length < 5) {
                            System.err.println("WARN: 无法解析文件名格式: " + fileName);
                            return FileVisitResult.CONTINUE;
                        }
                        String dateStr = split[4];
                        String month = dateStr.substring(0, 6);
                        String day = dateStr.substring(0, 8);

                        int year = Integer.parseInt(month.substring(0, 4));
                        int mon = Integer.parseInt(month.substring(4, 6));
                        if (year < 2000 || mon < 1 || mon > 12) {
                            System.err.println("FATAL: 日期解析异常, 文件名=" + fileName + " dateStr=" + dateStr);
                            System.exit(1);
                        }
                        // 创建层级目录
                        Path targetPath = Path.of(baseTargetDirectory, month, day);
                        if (!Files.exists(targetPath)) {
                            Files.createDirectories(targetPath);
                        }

                        // 移动文件到目标日期目录
                        Path targetPathFile = targetPath.resolve(file.getFileName());
                        SafeMoveService.move(file, targetPathFile, StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Moved " + file.getFileName() + " to " + targetPath);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 监控录像使用的，录像文件夹的特点
     * 1. 一级目录 2024011220，然后下面就全部是这个时间点的文件
     * <p>
     * 解决问题， 由于上面的问题，导致文件夹特别多，处理成如下
     * 1. 一级目录到月202401
     * 2. 再创建二级目录到天20240112
     * 3. 将源文件夹转移到二级目录下
     * 4. 总结就是没有移动以前任何文件，只是嵌套了一下造了两层级目录进行目录层级缩减
     *
     * @param directories directories参数
     * @param targetDirector 参数
     */
    public static void packageMonitorVideo(String[] directories, String targetDirector) {
        for (String directory : directories) {
            File sourceDir = new File(directory);
            File[] folders = sourceDir.listFiles(File::isDirectory);

            if (folders != null) {
                try {
                    for (File folder : folders) {
                        String sourceFolderName = folder.getName();

                        // 截取一级目录、二级目录、三级目录
                        if (sourceFolderName.length() < 8) {
                            System.err.println("WARN: 文件夹名过短无法解析: " + sourceFolderName);
                            continue;
                        }
                        String firstLevelDir = sourceFolderName.substring(0, 6);
                        String secondLevelDir = sourceFolderName.substring(0, 8);

                        int year = Integer.parseInt(firstLevelDir.substring(0, 4));
                        int mon = Integer.parseInt(firstLevelDir.substring(4, 6));
                        if (year < 2000 || mon < 1 || mon > 12) {
                            System.err.println("FATAL: 日期解析异常, folderName=" + sourceFolderName);
                            System.exit(1);
                        }

                        Path targetPath = Path.of(targetDirector, firstLevelDir, secondLevelDir, sourceFolderName);

                        // 创建目标路径的父目录（如果不存在）
                        Files.createDirectories(targetPath.getParent());


                        // 深层遍历并删除图片文件
                        deleteImageFilesRecursively(folder);

                        // 移动文件夹
                        Path sourcePath = folder.toPath();
                        SafeMoveService.move(sourcePath, targetPath);

                        // 删除空源文件夹
                        //                        Files.delete(sourcePath);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            System.out.println("All folders moved successfully.");
        }
    }

    /**
     * @param directories 参数
     * @param backupDeleteDirector 参数
     */
    public static void deleteRepeatFileByMd5(String[] directories, String backupDeleteDirector) {
        Set<String> md5Set = new HashSet<>();
        for (String directory : directories) {
            try {
                Files.walkFileTree(Path.of(directory), new SimpleFileVisitor<>() {
                    /**
                     * @param file 参数
                     * @param attrs 参数
                     */
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        final File realFile = file.toFile();
                        final String md5 = getMD5(realFile);
                        if (!md5Set.contains(md5)) {
                            md5Set.add(md5);
                            return FileVisitResult.CONTINUE;
                        }
                        Path deleteDir = Path.of(backupDeleteDirector);
                        if (!Files.exists(deleteDir)) {
                            Files.createDirectories(deleteDir);
                        }

                        Path targetPath = deleteDir.resolve(realFile.getName());
                        SafeMoveService.move(realFile.toPath(), targetPath);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    // --------------------- 辅助方法


    /**
     * 获取文件md5
     *
     * @param file 文件参数
     */
    public static String getMD5(File file) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            try (InputStream is = new FileInputStream(file); DigestInputStream dis = new DigestInputStream(is, md)) {
                byte[] buffer = new byte[8192];
                while (dis.read(buffer) != -1) {
                    ; // 读取文件内容以计算哈希值
                }
            }

            byte[] md5Bytes = md.digest();

            // 转换为十六进制字符串
            StringBuilder md5 = new StringBuilder();
            for (byte b : md5Bytes) {
                md5.append("%02x".formatted(b));
            }
            return md5.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 判断文件是否是图片文件的方法
     *
     * @param file 文件参数
     */
    private static boolean isImageFile(File file) {
        String[] imageExtensions = new String[] {"jpg", "jpeg", "png", "gif", "bmp"};
        String fileName = file.getName().toLowerCase();
        for (String extension : imageExtensions) {
            if (fileName.endsWith("." + extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 递归遍历目录并删除图片文件
     *
     * @param directory directory参数
     */
    private static void deleteImageFilesRecursively(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteImageFilesRecursively(file);
                } else if (isImageFile(file)) {
                    file.delete();
                }
            }
        }
    }
}
