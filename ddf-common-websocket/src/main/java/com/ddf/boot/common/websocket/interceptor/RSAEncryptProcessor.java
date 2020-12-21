package com.ddf.boot.common.websocket.interceptor;

import com.ddf.boot.common.core.util.JsonUtil;
import com.ddf.boot.common.websocket.model.Message;
import com.ddf.boot.common.websocket.util.WsSecureUtil;
import org.springframework.stereotype.Component;

/**
 * 基于RSA对消息进行加解密的实现$
 *
 * @author dongfang.ding
 * @date 2020/9/16 0016 22:43
 */
@Component
public class RSAEncryptProcessor implements EncryptProcessor {

    /**
     * 解密握手时需要的token参数
     * 如果客户端加密了的话
     *
     * @param token
     * @return
     */
    @Override
    public String decryptHandshakeToken(String token) {
        return WsSecureUtil.privateDecryptFromBcd(token);
    }

    /**
     * 加密要发送的消息对象
     *
     * @param message
     * @return
     */
    @Override
    public <T> String encryptMessage(Message<T> message) {
        message = Message.wrapperWithSign(message);
        return WsSecureUtil.privateEncryptBcd(JsonUtil.asString(message));
    }
}
