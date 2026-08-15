package com.ddf.boot.common.websocket.util;

import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import com.ddf.boot.common.api.util.JsonUtil;
import com.ddf.boot.common.core.helper.SpringContextHolder;
import com.ddf.boot.common.websocket.model.AuthPrincipal;
import com.ddf.boot.common.websocket.model.HandshakeParam;
import com.ddf.boot.common.websocket.properties.WebSocketProperties;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 提供一个默认的基于RSA的工具类
 *
 * @author dongfang.ding
 */
public class WsSecureUtil {

    private static final WebSocketProperties WEB_SOCKET_PROPERTIES = SpringContextHolder.getBeanWithStatic(
            WebSocketProperties.class);

    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    private static final RSA RSA;

    /**
     * 密钥必须由接入方配置，禁止内置默认密钥
     */
    static {
        if (WEB_SOCKET_PROPERTIES != null && WEB_SOCKET_PROPERTIES.getRsaPrivateKey() != null) {
            RSA = new RSA(WEB_SOCKET_PROPERTIES.getRsaPrivateKey(), WEB_SOCKET_PROPERTIES.getRsaPublicKey());
        } else {
            throw new IllegalStateException("WebSocket RSA 密钥未配置，必须配置 rsaPrivateKey/rsaPublicKey");
        }
    }

    /**
     * 私钥加密
     *
     * @param data 待处理数据
     * @return java.lang.String
     * @since 2019/11/29 12:02
     **/
    public static String privateEncryptBcd(String data) {
        return RSA.encryptBcd(data, KeyType.PrivateKey, UTF_8);
    }


    /**
     * 私钥解密
     *
     * @param data 待处理数据
     * @return java.lang.String
     * @since 2019/11/29 0029 12:03
     **/
    public static String privateDecryptFromBcd(String data) {
        return RSA.decryptStrFromBcd(data, KeyType.PrivateKey, UTF_8);
    }



    /**
     * 公钥加密
     *
     * @param data 待处理数据
     * @return java.lang.String
     * @since 2019/11/29 12:02
     **/
    public static String publicEncryptBcd(String data) {
        return RSA.encryptBcd(data, KeyType.PublicKey, UTF_8);
    }


    /**
     * 公钥解密
     *
     * @param data 待处理数据
     * @return java.lang.String
     * @since 2019/11/29 12:03
     **/
    public static String publicDecryptFromBcd(String data) {
        return RSA.decryptStrFromBcd(data, KeyType.PublicKey, UTF_8);
    }

    /**
     * 生成摘要
     *
     * @param data 待处理数据
     * @param key 目标键
     * @return java.lang.String
     * @since 2019/11/29 12:06
     **/
    public static String signWithHMac(String data, String key) {
        HMac mac = new HMac(HmacAlgorithm.HmacMD5, key.getBytes(UTF_8));
        return mac.digestHex(data);
    }

    /**
     * @param args 参数
     */
    public static void main(String[] args) throws UnsupportedEncodingException {
        String handShakeParamToken;
        for (int i = 0; i < 15; i++) {
            HandshakeParam param = new HandshakeParam();
            param.setAccessKeyId(i + "");
            param.setAccessKeyName("ddf" + i);
            param.setLoginType(AuthPrincipal.LoginType.USER);
            handShakeParamToken = JsonUtil.asString(param);
            handShakeParamToken = WsSecureUtil.publicEncryptBcd(handShakeParamToken);
            handShakeParamToken = URLEncoder.encode(handShakeParamToken, "utf-8");
            System.out.println(handShakeParamToken);
        }
    }

}
