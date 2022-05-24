package com.ddf.boot.common.core.util;

import cn.hutool.core.io.IoUtil;
import com.ddf.boot.common.core.constant.GlobalConstants;
import java.io.IOException;
import java.net.URLEncoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Web层辅助工具类
 */
@Slf4j
public class WebUtil {

    public static final String UNKNOWN = "unknown";

    /**
     * 获取当前ServletRequestAttributes
     */
    public static ServletRequestAttributes getCurServletRequestAttributes() {
        return (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    }

    /**
     * 获取当前HttpServletRequest
     */
    public static HttpServletRequest getCurRequest() {
        return getCurServletRequestAttributes().getRequest();
    }

    /**
     * 获取当前HttpServletResponse
     */
    public static HttpServletResponse getCurResponse() {
        return getCurServletRequestAttributes().getResponse();
    }


    /**
     * 获取客户端IP
     */
    public static String getHost() {
        HttpServletRequest request = getCurRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && ip.length() != 0 && !UNKNOWN.equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            if (ip.contains(GlobalConstants.COMMA)) {
                ip = ip.split(",")[0];
            }
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }


    /**
     * 处理http 响应成功
     *
     * @param response
     * @param message
     */
    public static void responseSuccess(HttpServletResponse response, String message) {
        response.setStatus(200);
        response.setCharacterEncoding("UTF-8");
        try {
            response.getWriter().print(message);
        } catch (IOException e) {
            log.error("处理响应失败，{}", message, e);
        }
    }

    /**
     * 处理http 响应失败
     *
     * @param response
     * @param errorMessage
     */
    public static void responseError(HttpServletResponse response, int status, String errorMessage) {
        log.debug("------------------失败响应------------------");
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        try {
            response.getWriter().print(errorMessage);
        } catch (IOException e) {
            log.error("处理响应失败{}", errorMessage, e);
        }
    }

    /**
     * 返回附件
     *
     * @param response 响应
     * @param filename 文件名
     * @param content 附件内容
     * @throws IOException
     */
    public static void writeAttachment(HttpServletResponse response, String filename, byte[] content) throws IOException {
        // 设置 header 和 contentType
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, "UTF-8"));
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        // 输出附件
        IoUtil.write(response.getOutputStream(), false, content);
    }

    /**
     * 获取User-Agent
     *
     * @param request
     * @return
     */
    public static String getUserAgent(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");
        return ua != null ? ua : "";
    }
}
