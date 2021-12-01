package comm.ddf.common.vps.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/12/01 19:36
 */
@Component
@ConfigurationProperties(prefix = "customs.vps")
@Data
public class VpsProperties {

    /**
     * 存放ffmpeg截帧，截取视频时的临时文件存放目录
     */
    private String ffmpegTmpPath = "/opt/ffmpeg/tmp/";

    /**
     * fdfs的storage.conf配置的存储目录,这个是为了在这个目录下找到文件真实存储路径的前缀
     * 这个只能处理fastdfs的文件和服务器在同一个服务器的问题， 所以只是一个优先级，如果本机存在，则不需要使用在线视频截取
     * ，所以也可以不配置
     *
     * 如果未配置store_path[n]， 从0开始，如store_path[0]， 则使用base_path
     */
    private String fdfsBasePath;
}
