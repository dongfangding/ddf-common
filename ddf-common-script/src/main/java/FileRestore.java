import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2024/07/09 10:54
 */
public class FileRestore {

    public static void main(String[] args) {
        String baseTargetDirectory = "D:/迅雷下载/backup";
        packageMonitorVideo(new String[] {"D:/文件/整理/监控"}, baseTargetDirectory);
    }

    /**
     * 适用于原手机文件，直接读取文件的创建时间，将文件按创建时间的月份进行归档整理
     *
     * @param directories
     * @param baseTargetDirectory
     */
    public static void computerReadAndMoveFileToMonth(String[] directories, String baseTargetDirectory) {
        String notVidVideoPath = baseTargetDirectory + "/not_vid";
        for (String directory : directories) {
            try {
                Files.walkFileTree(Paths.get(directory), new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        // 获取文件创建时间
                        String fileName = file.getFileName().toString();
                        if (!fileName.startsWith("VID")) {
                            // 如果文件名不是以"VID"开头，移动到not_vid目录
                            Path notVidPath = Paths.get(notVidVideoPath);
                            if (!Files.exists(notVidPath)) {
                                Files.createDirectories(notVidPath);
                            }
                            Path targetPath = notVidPath.resolve(file.getFileName());
                            Files.move(file, targetPath, StandardCopyOption.REPLACE_EXISTING);
                            System.out.println("Moved " + file.getFileName() + " to " + targetPath);
                        } else {
                            String month = fileName.substring(3, 9);
                            // 创建月目录
                            Path monthDir = Paths.get(baseTargetDirectory, month);
                            if (!Files.exists(monthDir)) {
                                Files.createDirectories(monthDir);
                            }

                            // 移动文件到目标日期目录
                            Path targetPath = monthDir.resolve(file.getFileName());
                            Files.move(file, targetPath, StandardCopyOption.REPLACE_EXISTING);
                            System.out.println("Moved " + file.getFileName() + " to " + targetPath);
                        }
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
     * @param directories
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
                        String firstLevelDir = sourceFolderName.substring(
                                0, Math.min(sourceFolderName.length(), 6)); // 前6位作为一级目录
                        String secondLevelDir = sourceFolderName.substring(
                                0, Math.min(sourceFolderName.length(), 8)); // 前8位作为二级目录

                        // 构建目标路径
                        Path targetPath = Paths.get(targetDirector, firstLevelDir, secondLevelDir, sourceFolderName);

                        // 创建目标路径的父目录（如果不存在）
                        Files.createDirectories(targetPath.getParent());


                        // 深层遍历并删除图片文件
                        deleteImageFilesRecursively(folder);

                        // 移动文件夹
                        Path sourcePath = folder.toPath();
                        Files.move(sourcePath, targetPath);

                        // 删除空源文件夹
                        Files.delete(sourcePath);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            System.out.println("All folders moved successfully.");
        }
    }


    public static void deleteRepeatFileByMd5(String[] directories, String backupDeleteDirector) {
        Set<String> md5Set = new HashSet<>();
        for (String directory : directories) {
            try {
                Files.walkFileTree(Paths.get(directory), new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        final File realFile = file.toFile();
                        final String md5 = getMD5(realFile);
                        if (!md5Set.contains(md5)) {
                            md5Set.add(md5);
                            return FileVisitResult.CONTINUE;
                        }
                        Path deleteDir = Paths.get(backupDeleteDirector);
                        if (!Files.exists(deleteDir)) {
                            Files.createDirectories(deleteDir);
                        }

                        Path targetPath = deleteDir.resolve(realFile.getName());
                        Files.move(realFile.toPath(), targetPath);
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
     * @param file
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
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
                md5.append(String.format("%02x", b));
            }
            return md5.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 判断文件是否是图片文件的方法
     *
     * @param file
     * @return
     */
    private static boolean isImageFile(File file) {
        String[] imageExtensions = new String[] { "jpg", "jpeg", "png", "gif", "bmp" };
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
     * @param directory
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
