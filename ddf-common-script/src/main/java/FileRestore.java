import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2024/07/09 10:54
 */
public class FileRestore {

    public static void main(String[] args) {
        String baseTargetDirectory = "D:/迅雷下载/restore";
        computerReadAndMoveFileToMonth(new String[] {"D:/迅雷下载/视频"}, baseTargetDirectory);
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
     * 适用于原手机文件，直接读取文件的创建时间，将文件按创建时间的月份和天进行归档整理
     *
     * @param directories
     * @param baseTargetDirectory
     */
    public static void computerReadAndMoveFileToDay(String[] directories, String baseTargetDirectory) {
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
                            String day = fileName.substring(3, 11);
                            // 创建月目录
                            Path monthDir = Paths.get(baseTargetDirectory, month);
                            if (!Files.exists(monthDir)) {
                                Files.createDirectories(monthDir);
                            }

                            // 创建日目录
                            Path dayDir = monthDir.resolve(day);
                            if (!Files.exists(dayDir)) {
                                Files.createDirectories(dayDir);
                            }

                            // 移动文件到目标日期目录
                            Path targetPath = dayDir.resolve(file.getFileName());
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
     * 从电脑中读取文件，适用于监控录像，按照文件格式截取创建时间，因录像一天内小文件较多，所以按照整理天汇总
     *
     * @param directories
     * @param baseTargetDirectory
     */
    public static void computerMonitorReadAndMoveFileToDay(String[] directories, String baseTargetDirectory) {
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
                            String day = fileName.substring(3, 11);
                            // 创建月目录
                            Path monthDir = Paths.get(baseTargetDirectory, month);
                            if (!Files.exists(monthDir)) {
                                Files.createDirectories(monthDir);
                            }

                            // 创建日目录
                            Path dayDir = monthDir.resolve(day);
                            if (!Files.exists(dayDir)) {
                                Files.createDirectories(dayDir);
                            }

                            // 移动文件到目标日期目录
                            Path targetPath = dayDir.resolve(file.getFileName());
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
     *
     * 解决问题， 由于上面的问题，导致文件夹特别多，处理成如下
     * 1. 一级目录到月202401
     * 2. 再创建二级目录到天20240112
     * 3. 将源文件夹转移到二级目录下
     * 4. 总结就是没有移动以前任何文件，只是嵌套了一下造了两层级目录进行目录层级缩减
     *
     * @param directories
     */
    public void packageMonitorVideo(String[] directories, String targetDirector) {
        for (String directory : directories) {
            File sourceDir = new File(directory);
            File[] folders = sourceDir.listFiles(File::isDirectory);

            if (folders != null) {
                try {
                    for (File folder : folders) {
                        String sourceFolderName = folder.getName();

                        // 截取一级目录、二级目录、三级目录
                        String firstLevelDir = sourceFolderName.substring(0, Math.min(sourceFolderName.length(), 6)); // 前6位作为一级目录
                        String secondLevelDir = sourceFolderName.substring(0, Math.min(sourceFolderName.length(), 8)); // 前8位作为二级目录

                        // 构建目标路径
                        Path targetPath = Paths.get(targetDirector, firstLevelDir, secondLevelDir, sourceFolderName);

                        // 创建目标路径的父目录（如果不存在）
                        Files.createDirectories(targetPath.getParent());

                        // 移动文件夹
                        Path sourcePath = folder.toPath();
                        Files.move(sourcePath, targetPath);

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
}
