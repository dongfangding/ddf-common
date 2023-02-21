package comm.ddf.common.vps.helper;

import com.ddf.boot.common.core.helper.EnvironmentHelper;
import com.github.tobato.fastdfs.FdfsClientConstants;
import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.github.tobato.fastdfs.domain.fdfs.MetaData;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.domain.fdfs.ThumbImageConfig;
import com.github.tobato.fastdfs.domain.upload.FastImageFile;
import com.github.tobato.fastdfs.domain.upload.ThumbImage;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import comm.ddf.common.vps.config.VpsProperties;
import comm.ddf.common.vps.dto.UploadResponse;
import comm.ddf.common.vps.util.VpsUtil;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>视频、图片处理客户端</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/12/01 17:42
 */
@Component
@RequiredArgsConstructor(onConstructor_={@Autowired})
@Slf4j
public class VpsClient {

    private final FastFileStorageClient fastFileStorageClient;

    private final ThumbImageConfig thumbImageConfig;

    private final VpsProperties vpsProperties;

    private final FdfsWebServer fdfsWebServer;

    private final EnvironmentHelper environmentHelper;

    private static final List<String> SUPPORT_IMAGE_LIST = Arrays.asList(FdfsClientConstants.SUPPORT_IMAGE_TYPE);

    /**
     * 上传本地文件
     *
     * @param filePath
     * @return
     */
    @SneakyThrows
    public UploadResponse uploadFile(String filePath, ThumbImage thumbImage) {
        final File file = new File(filePath);
        thumbImage = ObjectUtils.defaultIfNull(thumbImage, new ThumbImage(thumbImageConfig.getWidth(), thumbImageConfig.getHeight()));
        // 安全考虑， 只有这个临时目录的本地文件允许走这块代码上传
//        PreconditionUtil.checkArgument(filePath.startsWith(vpsProperties.getFfmpegTmpPath()), "不允许上传除ffmpeg临时目录以外的文件");
        String extName = filePath.substring(filePath.lastIndexOf(".") + 1);
        return uploadFile(new FastImageFile(Files.newInputStream(file.toPath()), file.length(), extName,
                new HashSet<>(), thumbImage), false);
    }

    /**
     * 上传文件并生成缩略图
     *
     * @param multipartFile
     * @return
     */
    @SneakyThrows
    public UploadResponse uploadFile(MultipartFile multipartFile) {
        final String fileName = StringUtils.defaultIfBlank(multipartFile.getOriginalFilename(), multipartFile.getName());
        String fileExtName = fileName.substring(fileName.lastIndexOf(".") + 1);
        return uploadFile(new FastImageFile(multipartFile.getInputStream(), multipartFile.getSize(), fileExtName,
                new HashSet<>(), null), false);
    }

    /**
     * 上传文件并生成缩略图
     *
     * @param multipartFile
     * @param cutVideoThumb 如果是视频是否裁剪视频帧获取封面图，支持非常有限，仅提供思路
     * @return
     */
    @SneakyThrows
    public UploadResponse uploadFile(MultipartFile multipartFile, boolean cutVideoThumb) {
        final String fileName = StringUtils.defaultIfBlank(multipartFile.getOriginalFilename(), multipartFile.getName());
        String fileExtName = fileName.substring(fileName.lastIndexOf(".") + 1);
        return uploadFile(new FastImageFile(multipartFile.getInputStream(), multipartFile.getSize(), fileExtName,
                new HashSet<>(), null), cutVideoThumb);
    }

    /**
     * 批量上传文件
     *
     * @param multipartFiles
     * @return
     */
    public List<UploadResponse> batchUploadFile(MultipartFile[] multipartFiles) {
        List<UploadResponse> rtnList = new ArrayList<>();
        for (MultipartFile file : multipartFiles) {
            rtnList.add(uploadFile(file));
        }
        return rtnList;
    }

    /**
     * 上传文件并生成缩略图
     *
     * 如果是视频的话， 视频需要先上传然后调用ffmpeg进行截帧命令， 然后将生成的文件再次调用上传。
     * 因此这个方法能工作的前提必须是有一台专门的服务器用来处理文件上传请求， 然后在这台服务器上要安装ffmpeg，这样才能正常工作
     *
     * @param fastImageFile
     * @return
     */
    public UploadResponse uploadFile(FastImageFile fastImageFile, boolean cutVideoThumb) {
        final String fileExtName = fastImageFile.getFileExtName();
        final InputStream inputStream = fastImageFile.getInputStream();
        final long fileSize = fastImageFile.getFileSize();
        final Set<MetaData> metaDataSet = fastImageFile.getMetaDataSet();
        ThumbImage thumbImage = fastImageFile.getThumbImage();
        String accessDomain = fdfsWebServer.getWebServerUrl();
        // 暂时以这个来判断是上传的图片还是视频
        if (isImage(fileExtName)) {
            thumbImage = ObjectUtils.defaultIfNull(thumbImage, new ThumbImage(thumbImageConfig.getWidth(), thumbImageConfig.getHeight()));
            final StorePath storePath = fastFileStorageClient.uploadImage(fastImageFile);
            return UploadResponse.fromStorePath(storePath, thumbImage, accessDomain);
        }
        // 走到这里也有可能上传的还是图片,但是不管了，当视频处理，然后去截帧。
        final StorePath storePath = fastFileStorageClient.uploadFile(inputStream, fileSize, fileExtName, metaDataSet);
        final UploadResponse response = UploadResponse.fromStorePath(storePath, thumbImage, accessDomain);
        if (thumbImage != null && cutVideoThumb) {
            String storeAccessPath = null;
            // 依赖图片在本机服务，且安装了ffmpeg
            final String basePath = vpsProperties.getFdfsBasePath();
            if (StringUtils.isNotBlank(basePath)) {
                String localFilePath = basePath + File.separator + VpsUtil.getFDfsPhysicalStorePath(storePath.getFullPath());
                File localFile = new File(localFilePath);
                if (localFile.exists()) {
                    storeAccessPath = localFilePath;
                }
            }
            if (storeAccessPath == null) {
                // 在线访问处理
                storeAccessPath = fdfsWebServer.getWebServerUrl() + "/" + storePath.getFullPath();
            }
            final String ffmpegTmpPath = vpsProperties.getFfmpegTmpPath();
            String coverTmpPath = VpsUtil.cutVideoCover(storeAccessPath, ffmpegTmpPath.endsWith(File.separator) ?
                    ffmpegTmpPath : ffmpegTmpPath + File.separator + environmentHelper.getApplicationName());
            // 存在截帧失败的情况，则这个封面图就没有
            response.setThumbPath(null);
            // 依赖于本地要安装ffmpeg， 否则这个文件在本地不会存在，无法上传，因此做近一步文件是否存在的判断
            if (Objects.nonNull(coverTmpPath) && new File(coverTmpPath).exists()) {
                final UploadResponse tmpResponse = uploadFile(coverTmpPath, fastImageFile.getThumbImage());
                // 视频截帧时使用缩略图字段返回视频封面图片地址
                response.setThumbPath(tmpResponse.getFullPath());
            }
        }
        return response;
    }


    /**
     * 简单判断是否是图片
     *
     * @param fileExtName
     * @return
     */
    private boolean isImage(String fileExtName) {
        for (String s : SUPPORT_IMAGE_LIST) {
            if (s.equalsIgnoreCase(fileExtName)) {
                return true;
            }
        }
        return false;
    }
}
