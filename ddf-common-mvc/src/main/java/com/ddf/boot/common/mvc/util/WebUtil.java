package com.ddf.boot.common.mvc.util;

import cn.hutool.core.io.IoUtil;
import com.ddf.boot.common.core.constant.GlobalConstants;
import com.ddf.boot.common.mvc.filter.CachingRequestBodyFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.WebUtils;

/**
 * Web层辅助工具类
 *
 * @author snowball
 */
@Slf4j
public class WebUtil {

    public static final String UNKNOWN = "unknown";

    /**
     * 是否信任代理转发头（X-Forwarded-For / X-Real-IP 等）。
     * <p>
     * 默认关闭：客户端可伪造这些头，无条件信任会被用于伪造 IP 绕过限流/风控。
     * 只有当应用确认部署在可信代理之后（且代理会覆盖这些头）时，才应开启。
     */
    private static volatile boolean trustProxyHeaders = false;

    /**
     * 设置是否信任代理转发头，应由接入方在确认部署拓扑后显式开启。
     */
    public static void setTrustProxyHeaders(boolean trust) {
        trustProxyHeaders = trust;
    }

    /**
     * 获取当前ServletRequestAttributes
     */
    public static ServletRequestAttributes getCurServletRequestAttributes() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
            return servletRequestAttributes;
        }
        return null;
    }

    /**
     * 获取当前HttpServletRequest
     */
    public static HttpServletRequest getCurRequest() {
        final ServletRequestAttributes attributes = getCurServletRequestAttributes();
        if (Objects.isNull(attributes)) {
            return null;
        }
        return attributes.getRequest();
    }

    /**
     * 获取当前HttpServletResponse
     */
    public static HttpServletResponse getCurResponse() {
        final ServletRequestAttributes attributes = getCurServletRequestAttributes();
        if (Objects.isNull(attributes)) {
            return null;
        }
        return attributes.getResponse();
    }


    /**
     * 获取客户端IP。
     * <p>
     * 默认优先返回 {@code RemoteAddr}（socket 对端地址，客户端无法直接伪造）。
     * 仅当通过 {@link #setTrustProxyHeaders(boolean)} 显式开启信任代理头时，才读取
     * {@code X-Forwarded-For} 等可被伪造的转发头，避免伪造 IP 绕过限流/风控。
     */
    public static String getHost() {
        HttpServletRequest request = getCurRequest();
        if (Objects.isNull(request)) {
            return "";
        }
        if (trustProxyHeaders) {
            String forwardedIp = resolveFromForwardHeaders(request);
            if (StringUtils.isNotBlank(forwardedIp) && !UNKNOWN.equalsIgnoreCase(forwardedIp)) {
                return forwardedIp;
            }
        }
        return StringUtils.defaultIfBlank(request.getRemoteAddr(), "");
    }

    /**
     * 依次从各类代理转发头中解析客户端 IP（仅在开启信任代理头时使用）。
     */
    private static String resolveFromForwardHeaders(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotBlank(ip) && !UNKNOWN.equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            if (ip.contains(GlobalConstants.COMMA)) {
                ip = ip.split(",")[0].trim();
            }
            return ip;
        }
        String[] headers =
                {"X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"};
        for (String header : headers) {
            ip = request.getHeader(header);
            if (StringUtils.isNotBlank(ip) && !UNKNOWN.equalsIgnoreCase(ip)) {
                return ip;
            }
        }
        return "";
    }


    /**
     * 处理http 响应成功
     *
     * @param response 响应对象
     * @param message 消息内容
     */
    public static void responseSuccess(HttpServletResponse response, String message) {
        response.setStatus(200);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=utf-8");
        try {
            response.getWriter().print(message);
        } catch (IOException e) {
            log.error("处理响应失败，{}", message, e);
        }
    }

    /**
     * 处理http 响应失败
     *
     * @param response 响应对象
     * @param errorMessage 错误消息参数
     * @param status 参数
     */
    public static void responseError(HttpServletResponse response, int status, String errorMessage) {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=utf-8");
        try {
            response.getWriter().print(errorMessage);
        } catch (IOException e) {
            log.error("处理响应失败{}", errorMessage, e);
        }
    }

    /**
     * 返回附件
     *
     * @param response 响应对象
     * @param filename 文件名
     * @param content 内容
     */
    public static void writeAttachment(HttpServletResponse response, String filename, byte[] content)
            throws IOException {
        // 设置 header 和 contentType
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, "UTF-8"));
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        // 输出附件
        IoUtil.write(response.getOutputStream(), false, content);
    }

    /**
     * 获取User-Agent
     *
     * @param request 请求对象
     */
    public static String getUserAgent(HttpServletRequest request) {
        return StringUtils.defaultIfBlank(request.getHeader("User-Agent"), "");
    }

    /**
     * 获取User-Agent
     */
    public static String getUserAgent() {
        return getCurrentRequestHeaderIfPresent("User-Agent");
    }

    /**
     * 安全获取请求头
     *
     * @param headerName 请求头名称
     */
    public static String getCurrentRequestHeaderIfPresent(String headerName) {
        HttpServletRequest request = getCurRequest();
        if (Objects.isNull(request)) {
            return "";
        }
        return StringUtils.defaultIfBlank(request.getHeader(headerName), "");
    }


    /**
     * 读取body, 配合{@link CachingRequestBodyFilter}
     *
     * @param httpServletRequest 请求参数
     */
    public static String readBody(HttpServletRequest httpServletRequest) {
        ContentCachingRequestWrapper wrapper = WebUtils.getNativeRequest(httpServletRequest,
                ContentCachingRequestWrapper.class);
        if (wrapper == null) {
            return "";
        }
        byte[] buf = wrapper.getContentAsByteArray();
        if (buf.length == 0) {
            return "";
        }
        String encoding = Optional.ofNullable(wrapper.getCharacterEncoding()).orElse(StandardCharsets.UTF_8.name());
        return new String(buf, Charset.forName(encoding));
    }


    /**
     * 处理http 响应失败
     *
     * @param response 响应对象
     * @param json JSON 字符串
     */
    public static void writerJson(HttpServletResponse response, String json) {
        response.setStatus(200);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=utf-8");
        try {
            response.getWriter().print(json);
        } catch (IOException e) {
            log.error("处理响应失败{}", json, e);
        }
    }
}
