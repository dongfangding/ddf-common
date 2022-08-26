package comm.ddf.common.vps.dto;

import com.github.tobato.fastdfs.domain.fdfs.DefaultThumbImageConfig;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.domain.upload.ThumbImage;
import java.io.Serializable;
import lombok.Data;

/**
 * <p>上传后响应</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/11/30 20:04
 */
@Data
public class UploadResponse implements Serializable {

    private static final long serialVersionUID = -4365909723660880167L;

    /**
     * 组name
     */
    private String group;

    /**
     * 上传后路径，取出group name
     */
    private String path;

    /**
     * 上传后路径，带group name
     */
    private String fullPath;

    /**
     * 缩略图路径， 带group name
     * @see com.github.tobato.fastdfs.domain.upload.ThumbImage#getThumbImagePath(String)
     * @see DefaultThumbImageConfig#getThumbImagePath(String)
     */
    private String thumbPath;

    /**
     * 访问域名
     */
    private String accessDomain;

    public String getAccessFullPath() {
        return String.join("/", accessDomain, fullPath);
    }


    /**
     * 只适用于使用默认的缩略图配置
     *
     * @param storePath
     * @return
     */
    public static UploadResponse fromStorePath(StorePath storePath, ThumbImage thumbImage, String accessDomain) {
        final UploadResponse response = new UploadResponse();
        response.setGroup(storePath.getGroup());
        response.setPath(storePath.getPath());
        response.setFullPath(storePath.getFullPath());
        response.setAccessDomain(accessDomain);
        if (thumbImage != null) {
            response.setThumbPath(thumbImage.getThumbImagePath(storePath.getFullPath()));
        }
        return response;
    }
}
