package com.ddf.boot.common.core.util;

import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import cn.hutool.crypto.symmetric.AES;
import com.ddf.boot.common.core.config.GlobalProperties;
import com.ddf.boot.common.core.encode.BCryptPasswordEncoder;
import com.ddf.boot.common.core.helper.SpringContextHolder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * 加密工具类
 *
 * @author dongfang.ding on 2018/5/31
 * @date 2022/5/24 22:57
 **/
@Slf4j
public class SecureUtils {

    private SecureUtils() {
    }

    private static final GlobalProperties GLOBAL_PROPERTIES = SpringContextHolder.getBeanWithStatic(
            GlobalProperties.class);

    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    /**
     * 动态的AES对象缓存, 这个是外部传入加密字符串，然后缓存AES对象来复用的
     */
    private final static Map<String, AES> DYNAMIC_AES_CACHE = new ConcurrentHashMap<>();

    /**
     * 动态的RSA对象缓存, 这个是外部传入加密字符串，然后缓存RSA对象来复用的
     */
    private final static Map<String, RSA> DYNAMIC_RSA_CACHE = new ConcurrentHashMap<>();

    /**
     * 密码随机散列工具类
     */
    private final static BCryptPasswordEncoder B_CRYPT_PASSWORD_ENCODER = new BCryptPasswordEncoder();

    /**
     * 全局默认AES加密密钥
     */
    private static String GLOBAL_DEFAULT_AES_SECRET = "Java is the best language.......";

    /**
     * 全局默认RSA公钥
     */
    private static String GLOBAL_DEFAULT_PUBLIC_KEY =
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCHTMSHP88BKSwWLlrab7t+yZQmdsVfDLyoJjAisP35C+sfYZVUGlPqntqpuMC6Q2gxAgN1sybVfJ+B/xqx7/1RXjwiS64VMSaScFoGcsTBxptCbt4/TDUcpE4UKrG7pizPL6ID6fYbPLvzhh6s14w1zVz7OL39zJ1l0AhUPDez/QIDAQAB";

    /**
     * 全局默认RSA私钥
     */
    private static String GLOBAL_DEFAULT_PRIVATE_KEY =
            "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAIdMxIc/zwEpLBYuWtpvu37JlCZ2xV8MvKgmMCKw/fkL6x9hlVQaU+qe2qm4wLpDaDECA3WzJtV8n4H/GrHv/VFePCJLrhUxJpJwWgZyxMHGm0Ju3j9MNRykThQqsbumLM8vogPp9hs8u/OGHqzXjDXNXPs4vf3MnWXQCFQ8N7P9AgMBAAECgYAbox/F7M/REeLyiPeABTDMfkqn7Lz2ZHio9FwCyhqm47tchqdlLZeUpmxOHPIpWhmPYTTptvWoyDMg78Y5MKeSyZcFOpzkTKjcJGUwEimgZCjl5Xsnqv/rK5TR3ADggmrAEkJ5+bdf5IWSStBpHDbZhg6Xll45cTRZNuw8V+9GgQJBANbQTqwekzZFJmhxr5m1E+RtsEQpkHAOoCC7vAdHFJdWPZX6wuw9wBWNxlr9Z6GkS6pHwu2ijTQb1S8Aa+w4siECQQChPa+t0vTShyVdzUpVsRryPF8BZik5q3varWA2LmyhqOmpXKtoNahazb4YNC857Co4WHGzlHQ4jP4VhRAHGz5dAkA/kZFWeg3SZ5BAJDR05hMm7BbXdP1bS9izFxtDhBNh3ZGICpcYVgW72yKx1n+OZBJIJ8hVjl7+5qWlrRhC5VxBAkBGNa8euIIkffaWXsLkh2bdXc5ctJh05SfcM6x2S0bAKeX8+j4k9WBmkboZnfeGeEB2IoT4Fkd5LGOjCTrObV19AkEArl8C9M2cQu3qBtBfb721u07bDJpo5LmjyKKM2JzboU38Vjy3O25kL8lLPVi13GeUrenUUp8ZtEVR6Gz+Sjowgw==";


    static {
        if (GLOBAL_PROPERTIES != null && StringUtils.isNotBlank(GLOBAL_PROPERTIES.getAesSecret())) {
            GLOBAL_DEFAULT_AES_SECRET = GLOBAL_PROPERTIES.getAesSecret();
        }

        if (GLOBAL_PROPERTIES != null && !StringUtils.isAnyBlank(
                GLOBAL_PROPERTIES.getRsaPrivateKey(), GLOBAL_PROPERTIES.getRsaPublicKey())) {
            GLOBAL_DEFAULT_PUBLIC_KEY = GLOBAL_PROPERTIES.getRsaPublicKey();
            GLOBAL_DEFAULT_PRIVATE_KEY = GLOBAL_PROPERTIES.getRsaPrivateKey();
        }
    }


    /**
     * 使用全局rsa私钥将数据编码为Hex十六进制字符， 私钥加密公钥解密
     *
     * @param data
     * @return
     */
    public static String rsaPrivateEncryptHex(String data) {
        return rsaPrivateEncryptHex(data, GLOBAL_DEFAULT_PRIVATE_KEY);
    }

    /**
     * 使用全局rsa私钥将数据编码为base64字符, 私钥加密公钥解密
     *
     * @param data
     * @return
     */
    public static String rsaPrivateEncryptBase64(String data) {
        return rsaPrivateEncryptBase64(data, GLOBAL_DEFAULT_PRIVATE_KEY);
    }


    /**
     * 全局RSA对象使用私钥将密文解码，密文需为Hex（16进制）或Base64字符串, 公钥加密私钥解密
     *
     * @param data
     * @return
     */
    public static String rsaPrivateDecryptStr(String data) {
        return rsaPrivateDecryptStr(data, GLOBAL_DEFAULT_PRIVATE_KEY);
    }

    /**
     * 使用全局rsa对象，公钥将数据编码为Hex十六进制字符, 公钥加密私钥解密
     *
     * @param data
     * @return
     */
    public static String rsaPublicEncryptHex(String data) {
        return rsaPublicEncryptHex(data, GLOBAL_DEFAULT_PUBLIC_KEY);
    }

    /**
     * 使用全局rsa对象，公钥将数据编码为Base64字符, 公钥加密私钥解密
     *
     * @param data
     * @return
     */
    public static String rsaPublicEncryptBase64(String data) {
        return rsaPublicEncryptBase64(data, GLOBAL_DEFAULT_PUBLIC_KEY);
    }

    /**
     * RSA全局对象公钥解密，密文需为Hex（16进制）或Base64字符串, 私钥加密公钥解密
     *
     * @param data
     * @return java.lang.String
     * @date 2019/11/29 12:03
     **/
    public static String rsaPublicDecryptStr(String data) {
        return rsaPublicDecryptStr(data, GLOBAL_DEFAULT_PUBLIC_KEY);
    }

    /**
     * RSA指定私钥编码为Hex字符串, 私钥加密公钥解密
     *
     * @param data
     * @return java.lang.String
     * @date 2019/11/29 12:02
     **/
    public static String rsaPrivateEncryptHex(String data, String privateKey) {
        RSA rsa = DYNAMIC_RSA_CACHE.get(privateKey);
        if (Objects.isNull(rsa)) {
            rsa = new RSA(privateKey, null);
            DYNAMIC_RSA_CACHE.put(privateKey, rsa);
        }
        return rsa.encryptHex(data, UTF_8, KeyType.PrivateKey);
    }

    /**
     * RSA指定私钥编码为BASE64字符串, 私钥加密公钥解密
     *
     * @param data
     * @return java.lang.String
     * @date 2019/11/29 12:02
     **/
    public static String rsaPrivateEncryptBase64(String data, String privateKey) {
        RSA rsa = DYNAMIC_RSA_CACHE.get(privateKey);
        if (Objects.isNull(rsa)) {
            rsa = new RSA(privateKey, null);
            DYNAMIC_RSA_CACHE.put(privateKey, rsa);
        }
        return rsa.encryptBase64(data, UTF_8, KeyType.PrivateKey);
    }

    /**
     * RSA指定私钥解密为字符串，密文需为Hex（16进制）或Base64字符串
     *
     * @param data
     * @return java.lang.String
     * @date 2019/11/29 0029 12:03
     **/
    public static String rsaPrivateDecryptStr(String data, String privateKey) {
        RSA rsa = DYNAMIC_RSA_CACHE.get(privateKey);
        if (Objects.isNull(rsa)) {
            rsa = new RSA(privateKey, null);
            DYNAMIC_RSA_CACHE.put(privateKey, rsa);
        }
        return rsa.decryptStr(data, KeyType.PrivateKey);
    }

    /**
     * RSA指定公钥加密,编码为Hex字符串
     *
     * @param data
     * @return java.lang.String
     * @date 2019/11/29 12:02
     **/
    public static String rsaPublicEncryptHex(String data, String publicKey) {
        RSA rsa = DYNAMIC_RSA_CACHE.get(publicKey);
        if (Objects.isNull(rsa)) {
            rsa = new RSA(null, publicKey);
            DYNAMIC_RSA_CACHE.put(publicKey, rsa);
        }
        return rsa.encryptHex(data, UTF_8, KeyType.PublicKey);
    }

    /**
     * RSA指定公钥加密,编码为Base64字符串
     *
     * @param data
     * @return java.lang.String
     * @date 2019/11/29 12:02
     **/
    public static String rsaPublicEncryptBase64(String data, String publicKey) {
        RSA rsa = DYNAMIC_RSA_CACHE.get(publicKey);
        if (Objects.isNull(rsa)) {
            rsa = new RSA(null, publicKey);
            DYNAMIC_RSA_CACHE.put(publicKey, rsa);
        }
        return rsa.encryptBase64(data, UTF_8, KeyType.PublicKey);
    }

    /**
     * RSA指定公钥解密，密文需为Hex（16进制）或Base64字符串
     *
     * @param data
     * @return java.lang.String
     * @date 2019/11/29 12:03
     **/
    public static String rsaPublicDecryptStr(String data, String publicKey) {
        RSA rsa = DYNAMIC_RSA_CACHE.get(publicKey);
        if (Objects.isNull(rsa)) {
            rsa = new RSA(null, publicKey);
            DYNAMIC_RSA_CACHE.put(publicKey, rsa);
        }
        return rsa.decryptStr(data, KeyType.PublicKey);
    }

    /**
     * 使用HmacSHA256生成摘要，32个字符秘钥
     *
     * @param data
     * @param key
     * @return java.lang.String
     * @date 2019/11/29 12:06
     **/
    public static String signWithHMac(String data, String key) {
        HMac mac = new HMac(HmacAlgorithm.HmacSHA256, key.getBytes(UTF_8));
        return mac.digestHex(data);
    }

    /**
     * 使用系统配置的AES加密成十六进制
     *
     * @param data
     * @return
     */
    public static String aesEncryptHex(String data) {
        return aesEncryptHex(data, GLOBAL_DEFAULT_AES_SECRET);
    }

    /**
     * 使用系统配置的AES加密成Base64
     *
     * @param data
     * @return
     */
    public static String aesEncryptBase64(String data) {
        return aesEncryptBase64(data, GLOBAL_DEFAULT_AES_SECRET);
    }

    /**
     * 使用系统配置的AES解密Hex（16进制）或Base64表示的字符串，默认UTF-8编码
     *
     * @param data
     * @return
     */
    public static String aesDecryptStr(String data) {
        return aesDecryptStr(data, GLOBAL_DEFAULT_AES_SECRET);
    }

    /**
     * 使用指定秘钥的AES加密成十六进制字符, 秘钥长度只有 128，192 和 256 比特三种
     *
     * @param data
     * @param secret
     * @return
     */
    public static String aesEncryptHex(String data, String secret) {
        AES aes = DYNAMIC_AES_CACHE.get(secret);
        if (Objects.isNull(aes)) {
            aes = new AES(Mode.CBC, Padding.PKCS5Padding, secret.getBytes(UTF_8), "0102030405060708".getBytes());
            DYNAMIC_AES_CACHE.put(secret, aes);
        }
        return aes.encryptHex(data, StandardCharsets.UTF_8);
    }

    /**
     * 使用指定秘钥的AES加密成Base64字符, 秘钥长度只有 128，192 和 256 比特三种
     *
     * @param data
     * @return
     */
    public static String aesEncryptBase64(String data, String secret) {
        AES aes = DYNAMIC_AES_CACHE.get(secret);
        if (Objects.isNull(aes)) {
            aes = new AES(Mode.CBC, Padding.PKCS5Padding, secret.getBytes(UTF_8), "0102030405060708".getBytes());
            DYNAMIC_AES_CACHE.put(secret, aes);
        }
        return aes.encryptBase64(data, StandardCharsets.UTF_8);
    }

    /**
     * 使用指定秘钥的AES解密加密后的十六进制数据, 密文为Hex（16进制）或Base64表示的字符串, 秘钥长度只有 128，192 和 256 比特三种
     *
     * @param data
     * @param secret
     * @return
     */
    public static String aesDecryptStr(String data, String secret) {
        AES aes = DYNAMIC_AES_CACHE.get(secret);
        if (Objects.isNull(aes)) {
            aes = new AES(Mode.CBC, Padding.PKCS5Padding, secret.getBytes(UTF_8), "0102030405060708".getBytes());
            DYNAMIC_AES_CACHE.put(secret, aes);
        }
        return aes.decryptStr(data);
    }

    /**
     * 随机散列函数摘要
     *
     * @param data
     * @return
     */
    public static String bCryptEncoder(String data) {
        return B_CRYPT_PASSWORD_ENCODER.encode(data);
    }

    /**
     * 验证随机散列函数摘要是否匹配, 注意这个方法不能再高频的全局接口中使用，因为很消耗cpu
     *
     * @param originStr
     * @param encodeStr
     * @return
     */
    public static boolean bCryptMatch(String originStr, String encodeStr) {
        return B_CRYPT_PASSWORD_ENCODER.matches(originStr, encodeStr);
    }
}

