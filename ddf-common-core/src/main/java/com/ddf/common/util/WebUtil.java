package com.ddf.common.util;

import com.ddf.common.constant.GlobalConstants;
import com.ddf.common.security.config.UserClaim;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.UnknownHostException;

/**
 * Web层辅助工具类
 */
public class WebUtil {

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
     * 获取请求头中的用户信息
     *
     * @return
     */
    public static UserClaim getUserClaim() {
        HttpServletRequest curRequest;
        UserClaim userClaim = UserClaim.defaultUser();
        try {
            curRequest = getCurRequest();
        } catch (Exception e) {
            // 非web环境返回默认用户信息
            return userClaim;
        }
        UserClaim newUserClaim = new UserClaim();
        boolean isDefault = true;
        String userId = curRequest.getHeader(UserClaim.CLAIM_USER_ID);
        if (userId != null) {
            isDefault = false;
            newUserClaim.setUserId(Long.parseLong(userId));
        }
        String userName = curRequest.getHeader(UserClaim.CLAIM_USER_NAME);
        if (StringUtils.isNotBlank(userName)) {
            try {
                isDefault = false;
                userName = URLDecoder.decode(curRequest.getHeader(UserClaim.CLAIM_USER_NAME), "UTF-8");
                newUserClaim.setUsername(userName);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        if (isDefault) {
            return userClaim;
        }
        return newUserClaim;
    }

    /**
     * 获取客户端IP
     */
    public static final String getHost() {
        HttpServletRequest request = getCurRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (GlobalConstants.LOCALHOST.equals(ip)) {
            InetAddress inet;
            try {
                inet = InetAddress.getLocalHost();
                ip = inet.getHostAddress();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
        if (ip != null && ip.length() > 15) {
            if (ip.indexOf(GlobalConstants.COMMA) > 0) {
                ip = ip.substring(0, ip.indexOf(GlobalConstants.COMMA));
            }
        }
        return ip;
    }

}
