package com.ddf.boot.common.core.util;

import cn.hutool.http.HttpGlobalConfig;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>资源链接工具类</p >
 *
 * @author Mitchell
 * @version 1.0
 * @date 2020/10/24 12:08
 */
@Slf4j
public class ResourceUrlUtils {

    private static final String START_PREFIX = "http";

    /**
     * 包装成http开头的绝对路径
     *
     * @param prefix http前缀
     * @param url    资源链接
     * @return
     */
    public static String wrapAbsolutePath(String prefix, String url) {
        if (StringUtils.isEmpty(url) || StringUtils.isEmpty(prefix)) {
            return null;
        }
        if (isHttpStart(url)) {
            return url;
        }
        return prefix + "/" + url;
    }

    /**
     * 截取url路径前缀，获取相对路径
     *
     * @param prefix
     * @param url
     * @return
     */
    public static String wrapRelativePath(String prefix, String url) {
        if (StringUtils.isEmpty(url) || StringUtils.isEmpty(prefix)) {
            return null;
        }
        if (!isHttpStart(url)) {
            return url;
        }
        return url.substring(prefix.length());
    }

    /**
     * 将链接内容转换成base64字符串
     *
     * @param url
     * @return
     */
    public static String base64(String url) {
        try {
            HttpResponse response = HttpRequest.get(url)
                    .timeout(HttpGlobalConfig.getTimeout())
                    .execute();
            byte[] bytes = response.bodyBytes();
            // data:image/jpeg;base64
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            log.error("", e);
            return null;
        }
    }

    /**
     * 是否是http开头的绝对路径
     *
     * @param url
     * @return
     */
    private static boolean isHttpStart(String url) {
        if (StringUtils.isEmpty(url)) {
            return false;
        }
        return url.startsWith(START_PREFIX);
    }

}
