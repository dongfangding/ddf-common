package com.ddf.boot.common.core.util;

import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import com.ddf.boot.common.core.config.GlobalProperties;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class SecureUtil {

    private SecureUtil() {}


    private static final GlobalProperties GLOBAL_PROPERTIES = SpringContextHolder.getBeanWithStatic(GlobalProperties.class);

    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    private final static RSA PRIVATE_RSA;

    private final static RSA PUBLIC_RSA;

    private final static RSA LOCAL_PRIVATE_RSA;

    private final static RSA LOCAL_PUBLIC_RSA;

    private final static SymmetricCrypto AES;

    /**
     * 本地公钥私钥
     */
    private static final  String LOCAL_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCHTMSHP88BKSwWLlrab7t+yZQmdsVfDLyoJjAisP35C+sfYZVUGlPqntqpuMC6Q2gxAgN1sybVfJ+B/xqx7/1RXjwiS64VMSaScFoGcsTBxptCbt4/TDUcpE4UKrG7pizPL6ID6fYbPLvzhh6s14w1zVz7OL39zJ1l0AhUPDez/QIDAQAB";
    private static final  String LOCAL_PRIVATE_KEY = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAIdMxIc/zwEpLBYuWtpvu37JlCZ2xV8MvKgmMCKw/fkL6x9hlVQaU+qe2qm4wLpDaDECA3WzJtV8n4H/GrHv/VFePCJLrhUxJpJwWgZyxMHGm0Ju3j9MNRykThQqsbumLM8vogPp9hs8u/OGHqzXjDXNXPs4vf3MnWXQCFQ8N7P9AgMBAAECgYAbox/F7M/REeLyiPeABTDMfkqn7Lz2ZHio9FwCyhqm47tchqdlLZeUpmxOHPIpWhmPYTTptvWoyDMg78Y5MKeSyZcFOpzkTKjcJGUwEimgZCjl5Xsnqv/rK5TR3ADggmrAEkJ5+bdf5IWSStBpHDbZhg6Xll45cTRZNuw8V+9GgQJBANbQTqwekzZFJmhxr5m1E+RtsEQpkHAOoCC7vAdHFJdWPZX6wuw9wBWNxlr9Z6GkS6pHwu2ijTQb1S8Aa+w4siECQQChPa+t0vTShyVdzUpVsRryPF8BZik5q3varWA2LmyhqOmpXKtoNahazb4YNC857Co4WHGzlHQ4jP4VhRAHGz5dAkA/kZFWeg3SZ5BAJDR05hMm7BbXdP1bS9izFxtDhBNh3ZGICpcYVgW72yKx1n+OZBJIJ8hVjl7+5qWlrRhC5VxBAkBGNa8euIIkffaWXsLkh2bdXc5ctJh05SfcM6x2S0bAKeX8+j4k9WBmkboZnfeGeEB2IoT4Fkd5LGOjCTrObV19AkEArl8C9M2cQu3qBtBfb721u07bDJpo5LmjyKKM2JzboU38Vjy3O25kL8lLPVi13GeUrenUUp8ZtEVR6Gz+Sjowgw==";

    private static final String PRIMARY_KEY = LOCAL_PRIVATE_KEY;

    private static final String PUBLIC_KEY = LOCAL_PUBLIC_KEY;

    static {
        PRIVATE_RSA = new RSA(PRIMARY_KEY, null);
        PUBLIC_RSA = new RSA(null, PUBLIC_KEY);

        LOCAL_PRIVATE_RSA = new RSA(LOCAL_PRIVATE_KEY, null);
        LOCAL_PUBLIC_RSA = new RSA(null, LOCAL_PUBLIC_KEY);

        if (GLOBAL_PROPERTIES != null && StringUtils.isNotBlank(GLOBAL_PROPERTIES.getAesSecret())) {
            AES = new SymmetricCrypto(SymmetricAlgorithm.AES, GLOBAL_PROPERTIES.getAesSecret().getBytes(UTF_8));
        } else {
            AES = new SymmetricCrypto(SymmetricAlgorithm.AES, "Java is the best language.......".getBytes(UTF_8));
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
        return PRIVATE_RSA.encryptBcd(data, KeyType.PrivateKey, UTF_8);
    }


    /**
     * 私钥解密
     * @param data
     * @return java.lang.String

     * @date 2019/11/29 0029 12:03
     **/
    public static String privateDecryptFromBcd(String data) {
        return PRIVATE_RSA.decryptStrFromBcd(data, KeyType.PrivateKey, UTF_8);
    }



    /**
     * 公钥加密
     *
     * @param data
     * @return java.lang.String

     * @date 2019/11/29 12:02
     **/
    public static String publicEncryptBcd(String data) {
        return PUBLIC_RSA.encryptBcd(data, KeyType.PublicKey, UTF_8);
    }


    /**
     * 公钥解密
     * @param data
     * @return java.lang.String

     * @date 2019/11/29 12:03
     **/
    public static String publicDecryptFromBcd(String data) {
        return PUBLIC_RSA.decryptStrFromBcd(data, KeyType.PublicKey, UTF_8);
    }

    /**
     * 生成摘要
     * @param data
     * @param key
     * @return java.lang.String

     * @date 2019/11/29 12:06
     **/
    public static String signWithHMac(String data, String key) {
        HMac mac = new HMac(HmacAlgorithm.HmacMD5, key.getBytes(UTF_8));
        return mac.digestHex(data);
    }


    /**
     * 私钥加密
     *
     * @param data
     * @return java.lang.String

     * @date 2019/11/29 12:02
     **/
    public static String localPrivateEncryptBcd(String data) {
        return LOCAL_PRIVATE_RSA.encryptBcd(data, KeyType.PrivateKey, UTF_8);
    }


    /**
     * 私钥解密
     * @param data
     * @return java.lang.String

     * @date 2019/11/29 0029 12:03
     **/
    public static String localPrivateDecryptFromBcd(String data) {
        return LOCAL_PRIVATE_RSA.decryptStrFromBcd(data, KeyType.PrivateKey, UTF_8);
    }


    /**
     * 公钥加密
     *
     * @param data
     * @return java.lang.String
     * @date 2019/11/29 12:02
     **/
    public static String localPublicEncryptBcd(String data) {
        return LOCAL_PUBLIC_RSA.encryptBcd(data, KeyType.PublicKey, UTF_8);
    }


    /**
     * 公钥解密
     * @param data
     * @return java.lang.String

     * @date 2019/11/29 12:03
     **/
    public static String localPublicDecryptFromBcd(String data) {
        return LOCAL_PUBLIC_RSA.decryptStrFromBcd(data, KeyType.PublicKey, UTF_8);
    }

    /**
     * 获取AES实例
     * @return
     */
    public static SymmetricCrypto getAES() {
        return AES;
    }

    /**
     * 使用AES加密成十六进制
     * @param str
     * @return
     */
    public static String encryptHexByAES(String str) {
        return AES.encryptHex(str, StandardCharsets.UTF_8);
    }

    public static String decryptFromHexByAES(String str) {
        return AES.decryptStr(str);
    }

    public static void main(String[] args) {

        String str = "{\"accessKeyId\":\"1\",\"accessKeyName\":\"ddf\",\"loginType\":\"USER\",\"currentTimeStamp\":1600229897375}";

        String s = SecureUtil.localPublicEncryptBcd(str);
        System.out.println("s = " + s);

        String aesStr = "哈哈我非叫我二纺机我饿";

    }
}

