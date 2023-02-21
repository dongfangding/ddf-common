package comm.ddf.common.vps.dto;

import com.github.tobato.fastdfs.domain.upload.ThumbImage;
import java.io.Serializable;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>上传文件请求类</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/05/30 12:52
 */
@Data
public class UploadRequest implements Serializable {

    private static final long serialVersionUID = -8223899801722847573L;

    /**
     * 文件
     */
    private MultipartFile[] multipartFile;

    /**
     * 是否生成缩略图
     */
    private boolean thumb = true;

    /**
     * 缩略图比例，如果不配置的话，走默认全局配置
     */
    private ThumbImage thumbImage;

}
