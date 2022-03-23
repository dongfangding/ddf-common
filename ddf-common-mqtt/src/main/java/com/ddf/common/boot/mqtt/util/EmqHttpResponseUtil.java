package com.ddf.common.boot.mqtt.util;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * EMQ X 在设备连接事件中使用当前客户端相关信息作为参数，向用户自定义的认证服务发起请求查询权限
 * <p>
 * 通过返回的 HTTP 响应状态码 (HTTP statusCode) 来处理认证请求。
 *
 * @author Mitchell
 * @version 1.0
 * @date 2021/1/25 20:24
 */
@Slf4j
public class EmqHttpResponseUtil {
 
    /**
     * 认证成功：API 返回 200 状态码
     *
     * @param response
     * @param message
     */
    public static void success(HttpServletResponse response, String message) {
        response.setStatus(200);
        response.setCharacterEncoding("UTF-8");
        try {
            response.getWriter().print(message);
        } catch (IOException e) {
            log.error("{} = {}", message, e);
        }
    }
 
    /**
     * 认证失败：API 返回 4xx 状态码
     *
     * @param response
     * @param errorMessage
     */
    public static void error(HttpServletResponse response, String errorMessage) {
        log.debug("------------------失败响应------------------");
        response.setStatus(401);
        response.setCharacterEncoding("UTF-8");
        try {
            response.getWriter().print(errorMessage);
        } catch (IOException e) {
            log.error("{} = {}", errorMessage, e);
        }
    }
}
