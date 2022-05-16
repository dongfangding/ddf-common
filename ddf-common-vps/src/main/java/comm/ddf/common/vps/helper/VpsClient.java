package comm.ddf.common.vps.helper;

import com.ddf.boot.common.core.helper.EnvironmentHelper;
import com.github.tobato.fastdfs.FdfsClientConstants;
import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.github.tobato.fastdfs.domain.fdfs.MetaData;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import comm.ddf.common.vps.config.VpsProperties;
import comm.ddf.common.vps.dto.UploadResponse;
import comm.ddf.common.vps.util.VpsUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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
    public UploadResponse uploadFile(String filePath) {
        final File file = new File(filePath);
        // 安全考虑， 只有这个临时目录的本地文件允许走这块代码上传
//        PreconditionUtil.checkArgument(filePath.startsWith(vpsProperties.getFfmpegTmpPath()), "不允许上传除ffmpeg临时目录以外的文件");
        String extName = filePath.substring(filePath.lastIndexOf(".") + 1);
        return uploadFile(new FileInputStream(file), file.length(), extName, null);
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
        return uploadFile(multipartFile.getInputStream(), multipartFile.getSize(), fileExtName, new HashSet<>());
    }

    /**
     * 上传文件并生成缩略图
     *
     * 如果是视频的话， 视频需要先上传然后调用ffmpeg进行截帧命令， 然后将生成的文件再次调用上传。
     * 因此这个方法能工作的前提必须是有一台专门的服务器用来处理文件上传请求， 然后在这台服务器上要安装ffmpeg，这样才能正常工作
     *
     * @param inputStream
     * @param fileSize
     * @param fileExtName
     * @param metaDataSet
     * @return
     */
    public UploadResponse uploadFile(InputStream inputStream, long fileSize, String fileExtName, Set<MetaData> metaDataSet) {
        // 暂时以这个来判断是上传的图片还是视频
        if (isImage(fileExtName)) {
            final StorePath storePath = fastFileStorageClient.uploadImageAndCrtThumbImage(inputStream, fileSize, fileExtName, metaDataSet);
            return UploadResponse.fromStorePath(storePath);
        }
        // 走到这里也有可能上传的还是图片,但是不管了，当视频处理，然后去截帧。
        final StorePath storePath = fastFileStorageClient.uploadFile(inputStream, fileSize, fileExtName, metaDataSet);
        final UploadResponse response = UploadResponse.fromStorePath(storePath);
        String storeAccessPath = null;

        // 如果文件存储在本机
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
        if (Objects.nonNull(coverTmpPath)) {
            final UploadResponse tmpResponse = uploadFile(coverTmpPath);
            // 视频截帧时使用缩略图字段返回视频封面图片地址
            response.setThumbPath(tmpResponse.getFullPath());
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
