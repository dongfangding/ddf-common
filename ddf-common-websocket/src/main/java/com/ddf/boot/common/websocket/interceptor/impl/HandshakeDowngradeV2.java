package com.ddf.boot.common.websocket.interceptor.impl;

import com.ddf.boot.common.core.util.SecureUtil;
import com.ddf.boot.common.websocket.constant.WebsocketConst;
import com.ddf.boot.common.websocket.interceptor.HandshakeDowngrade;
import com.ddf.boot.common.websocket.model.ws.AuthPrincipal;
import com.ddf.boot.common.websocket.service.MerchantBaseDeviceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * 兼容的握手处理器$
 * <p>
 * <p>
 * _ooOoo_
 * o8888888o
 * 88" . "88
 * (| -_- |)
 * O\ = /O
 * ___/`---'\____
 * .   ' \\| |// `.
 * / \\||| : |||// \
 * / _||||| -:- |||||- \
 * | | \\\ - /// | |
 * | \_| ''\---/'' | |
 * \ .-\__ `-` ___/-. /
 * ___`. .' /--.--\ `. . __
 * ."" '< `.___\_<|>_/___.' >'"".
 * | | : `- \`.;`\ _ /`;.`/ - ` : | |
 * \ \ `-. \_ __\ /__ _/ .-` / /
 * ======`-.____`-.___\_____/___.-`____.-'======
 * `=---='
 * .............................................
 * 佛曰：bug泛滥，我已瘫痪！
 *
 * @author dongfang.ding
 * @date 2019/12/27 0027 13:47
 */
@Component
@Slf4j
public class HandshakeDowngradeV2 implements HandshakeDowngrade {

    @Autowired
    private MerchantBaseDeviceService merchantBaseDeviceService;

    @Override
    public AuthPrincipal validPrincipal(HttpServletRequest httpServletRequest, ServerHttpResponse response) {
        String token = httpServletRequest.getParameter(WebsocketConst.TOKEN_PARAMETER);
        if (StringUtils.isBlank(token)) {
            return null;
        }
        String encryptToken;
        try {
            encryptToken = SecureUtil.privateDecryptFromBcd(token);
        } catch (Exception e) {
            log.error("{}=>解密出错，不允许认证！", token, e);
            return null;
        }

        String[] encryptInfo = encryptToken.split(WebsocketConst.AUTH_SPLIT);
        // loginType;ime;token;当前时间戳
        if (encryptInfo.length != 4) {
            log.error("解析后格式不符合！=> {} <==", encryptToken);
            return null;
        }

        String loginType = encryptInfo[0];
        if (StringUtils.isBlank(loginType)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return null;
        }
        AuthPrincipal.LoginType loginTypeEnum;
        try {
            loginTypeEnum = AuthPrincipal.LoginType.valueOf(loginType);
        } catch (Exception e) {
            log.error("loginType的值不正确！！【{}】", loginType);
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return null;
        }
        boolean handshakeResult = false;
        String deviceNumber = encryptInfo[1];
        token = encryptInfo[2];
        long timestamp = Long.parseLong(encryptInfo[3]);
        long now = System.currentTimeMillis();
        if (Math.abs(now - timestamp) > WebsocketConst.VALID_AUTH_TIMESTAMP) {
            log.error("{}认证参数已过期", deviceNumber);
            return null;
        }
        if (AuthPrincipal.LoginType.ANDROID.equals(loginTypeEnum)) {
            if (merchantBaseDeviceService.isValid(deviceNumber, token)) {
                handshakeResult = true;
            }
        }

        if (!handshakeResult) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return null;
        }
        return AuthPrincipal.buildAndroidAuthPrincipal(token, deviceNumber, null, timestamp);
    }
}
