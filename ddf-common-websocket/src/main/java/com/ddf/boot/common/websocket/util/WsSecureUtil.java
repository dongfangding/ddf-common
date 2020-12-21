package com.ddf.boot.common.websocket.util;

import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import com.ddf.boot.common.core.util.JsonUtil;
import com.ddf.boot.common.core.util.SpringContextHolder;
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
     * 本地提供一个默认的用于测试的RSA密钥对
     */
    private static final String LOCAL_PUBLIC_KEY =
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCHTMSHP88BKSwWLlrab7t+yZQmdsVfDLyoJjAisP35C+sfYZVUGlPqntqpuMC6Q2gxAgN1sybVfJ+B/xqx7/1RXjwiS64VMSaScFoGcsTBxptCbt4/TDUcpE4UKrG7pizPL6ID6fYbPLvzhh6s14w1zVz7OL39zJ1l0AhUPDez/QIDAQAB";
    private static final String LOCAL_PRIVATE_KEY =
            "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAIdMxIc/zwEpLBYuWtpvu37JlCZ2xV8MvKgmMCKw/fkL6x9hlVQaU+qe2qm4wLpDaDECA3WzJtV8n4H/GrHv/VFePCJLrhUxJpJwWgZyxMHGm0Ju3j9MNRykThQqsbumLM8vogPp9hs8u/OGHqzXjDXNXPs4vf3MnWXQCFQ8N7P9AgMBAAECgYAbox/F7M/REeLyiPeABTDMfkqn7Lz2ZHio9FwCyhqm47tchqdlLZeUpmxOHPIpWhmPYTTptvWoyDMg78Y5MKeSyZcFOpzkTKjcJGUwEimgZCjl5Xsnqv/rK5TR3ADggmrAEkJ5+bdf5IWSStBpHDbZhg6Xll45cTRZNuw8V+9GgQJBANbQTqwekzZFJmhxr5m1E+RtsEQpkHAOoCC7vAdHFJdWPZX6wuw9wBWNxlr9Z6GkS6pHwu2ijTQb1S8Aa+w4siECQQChPa+t0vTShyVdzUpVsRryPF8BZik5q3varWA2LmyhqOmpXKtoNahazb4YNC857Co4WHGzlHQ4jP4VhRAHGz5dAkA/kZFWeg3SZ5BAJDR05hMm7BbXdP1bS9izFxtDhBNh3ZGICpcYVgW72yKx1n+OZBJIJ8hVjl7+5qWlrRhC5VxBAkBGNa8euIIkffaWXsLkh2bdXc5ctJh05SfcM6x2S0bAKeX8+j4k9WBmkboZnfeGeEB2IoT4Fkd5LGOjCTrObV19AkEArl8C9M2cQu3qBtBfb721u07bDJpo5LmjyKKM2JzboU38Vjy3O25kL8lLPVi13GeUrenUUp8ZtEVR6Gz+Sjowgw==";

    /**
     * 为了方便可以直接main方法调试，这里用了静态代码块, 但是需要SpringContextHolder这个类配置必须进行非空判断
     */
    static {
        if (WEB_SOCKET_PROPERTIES != null && WEB_SOCKET_PROPERTIES.getRsaPrivateKey() != null) {
            RSA = new RSA(WEB_SOCKET_PROPERTIES.getRsaPrivateKey(), WEB_SOCKET_PROPERTIES.getRsaPublicKey());
        } else {
            RSA = new RSA(LOCAL_PRIVATE_KEY, LOCAL_PUBLIC_KEY);
        }
    }

    /**
     * 私钥加密
     *
     * @param data
     * @return java.lang.String
     * @date 2019/11/29 12:02
     **/
    public static String privateEncryptBcd(String data) {
        return RSA.encryptBcd(data, KeyType.PrivateKey, UTF_8);
    }


    /**
     * 私钥解密
     *
     * @param data
     * @return java.lang.String
     * @date 2019/11/29 0029 12:03
     **/
    public static String privateDecryptFromBcd(String data) {
        return RSA.decryptStrFromBcd(data, KeyType.PrivateKey, UTF_8);
    }



    /**
     * 公钥加密
     *
     * @param data
     * @return java.lang.String
     * @date 2019/11/29 12:02
     **/
    public static String publicEncryptBcd(String data) {
        return RSA.encryptBcd(data, KeyType.PublicKey, UTF_8);
    }


    /**
     * 公钥解密
     *
     * @param data
     * @return java.lang.String
     * @date 2019/11/29 12:03
     **/
    public static String publicDecryptFromBcd(String data) {
        return RSA.decryptStrFromBcd(data, KeyType.PublicKey, UTF_8);
    }

    /**
     * 生成摘要
     *
     * @param data
     * @param key
     * @return java.lang.String
     * @date 2019/11/29 12:06
     **/
    public static String signWithHMac(String data, String key) {
        HMac mac = new HMac(HmacAlgorithm.HmacMD5, key.getBytes(UTF_8));
        return mac.digestHex(data);
    }

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

