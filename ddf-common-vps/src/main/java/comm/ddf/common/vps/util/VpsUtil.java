package comm.ddf.common.vps.util;

import java.io.File;
import java.text.MessageFormat;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>视频图片处理工具类</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/12/01 16:49
 */
@Slf4j
public class VpsUtil {

    /**
     * 视频截帧图片
     *
     * {0} 原视频文件地址
     * {1} 从那一秒开始截帧
     * {2} 图片截取后保存路径，包含文件名、后缀等都要指定
     * -i 指定源文件
     * -y 文件存在则覆盖
     * -f 指定图片编码格式
     * -ss 指定从那一秒开始截帧
     * -vframes 1 图片的话是截1帧
     *
     */
    public static final String FFMPEG_SCREENSHOT_COMMAND = "ffmpeg -i {0} -y -f image2 -ss {1} -vframes 1 {2}";


    /**
     * 视频截取指定时间段
     *
     * {0} 原视频文件地址
     * {1} 从哪个时间段开始截取，格式时:分:秒
     * {2} 截取到哪个时间段结束，格式时:分:秒
     * {3} 截取后保存路径，包含文件名、后缀等都要指定
     * -i 指定源文件
     * -y 文件存在则覆盖
     * -vcodec copy 使用跟原视频一样的视频编解码器
     * -acodec copy 使用跟原视频一样的音频编解码器
     * -ss 从哪个时间段开始截取，格式时:分:秒
     * -to 截取到哪个时间段结束，格式时:分:秒
     */
    public static final String FFMPEG_VIDEO_CUT_COMMAND = "ffmpeg -i {0} -y -vcodec copy -acodec copy -ss {1} -to {2} {3}";

    /**
     * 对视频进行封面截图
     *
     * @param filePath 可以是本地文件，也可以是在线文件
     * @return 截取后文件本地路径
     */
    public static String cutVideoCover(String filePath, String tmpPath) {
        return cutVideoCover(filePath, "1", tmpPath);
    }

    /**
     * 对视频进行封面截图
     *
     * @param filePath        可以是本地文件，也可以是在线文件
     * @param beforeCutSecond
     * @return 截取后文件本地路径
     */
    public static String cutVideoCover(String filePath, String beforeCutSecond, String tmpPath) {
        tmpPath = tmpPath.endsWith(File.separator) ? tmpPath : tmpPath + File.separator;
        String finalFilePath = tmpPath + filePath.replaceAll("//*", "_") + "_" + System.currentTimeMillis() + ".jpg";
        String command = MessageFormat.format(FFMPEG_SCREENSHOT_COMMAND, filePath, beforeCutSecond, finalFilePath);
        try {
            log.info("视频截图命令， command = {}", command);
            ProcessBuilder builder = new ProcessBuilder("sh", "-c", command);
            final Process start = builder.start();
            start.waitFor();
        } catch (Exception e) {
            // 如果失败的话，会损失数据，暂时不处理
            log.error("视频截图失败， command = {}", command, e);
            return null;
        }
        return finalFilePath;
    }


    /**
     * 根据FastDFS上传后返回的路径获取本机实际存储路径
     * 没在官网找到相关api,，只能按照规则解析，不确定一定没有问题
     * group1  groupName
     * M00    这个好像是磁盘序号
     *
     * 后面的路径是在storage.如果未配置store_path[n]路径下的一个data目录下
     * 如果未配置store_path[n]， 则使用的是base_path
     * /data是固定的
     *
     * group1/M00/00/00/ag8Kh2GnPTWASVlZAM7twHqR7-Y487.mp4
     * 物理的
     * @param path
     * @return
     */
    public static String getFDfsPhysicalStorePath(String path) {
        path = path.replaceAll("group[0-9]*|/M[0-9]*", "");
        return path;
    }
}
