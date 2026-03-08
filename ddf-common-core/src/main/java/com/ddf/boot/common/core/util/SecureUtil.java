package com.ddf.boot.common.core.util;

import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import com.ddf.boot.common.core.config.GlobalProperties;
import com.ddf.boot.common.core.encode.BCryptPasswordEncoder;
import com.ddf.boot.common.core.exception.SecureException;
import com.ddf.boot.common.core.helper.SpringContextHolder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 加密工具类
 * <p>
 * 安全说明：
 * 1. 所有密钥必须通过 GlobalProperties 配置，禁止硬编码
 * 2. 如果配置缺失，将抛出 IllegalStateException 而非使用默认密钥
 * </p>
 *
 * @author dongfang.ding on 2018/5/31
 * @since 2022/5/24 22:57
 **/
public class SecureUtil {

    private SecureUtil() {
    }

    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    private static final GlobalProperties GLOBAL_PROPERTIES;

    static {
        GlobalProperties props = null;
        try {
            props = SpringContextHolder.getBeanWithStatic(GlobalProperties.class);
        } catch (Exception e) {
            // Spring context may not be ready during static initialization
        }
        GLOBAL_PROPERTIES = props;
    }

    private static final RSA PRIVATE_RSA;

    private static final RSA PUBLIC_RSA;

    private static volatile SymmetricCrypto AES;

    /**
     * 动态的AES对象缓存
     */
    private static final Map<String, SymmetricCrypto> DYNAMIC_AES_CACHE = new ConcurrentHashMap<>();

    /**
     * 密码随机散列工具类
     */
    private static final BCryptPasswordEncoder B_CRYPT_PASSWORD_ENCODER = new BCryptPasswordEncoder();

    /**
     * 安全初始化RSA密钥
     * 如果配置缺失，抛出异常而非使用硬编码密钥
     * @param privateKey 参数
     * @param publicKey 参数
     * @param configName 参数
     */
    private static RSA initRsa(String privateKey, String publicKey, String configName) {
        if (StringUtils.isBlank(privateKey) || StringUtils.isBlank(publicKey)) {
            throw new SecureException(configName + " 未配置，请通过 GlobalProperties 配置 RSA 密钥对");
        }
        return new RSA(privateKey, publicKey);
    }

    /**
     * 安全获取AES密钥
     * 如果配置缺失，抛出异常而非使用默认密钥
     */
    private static SymmetricCrypto initAes() {
        if (GLOBAL_PROPERTIES == null || StringUtils.isBlank(GLOBAL_PROPERTIES.getAesSecret())) {
            throw new SecureException("AES secret 未配置，请通过 global.aes-secret 配置 AES 密钥，"
                    + "建议使用32位随机字符串并通过配置中心管理");
        }
        return new SymmetricCrypto(
                SymmetricAlgorithm.AES, GLOBAL_PROPERTIES
                .getAesSecret()
                .getBytes(UTF_8)
        );
    }

    static {
        // 从配置获取密钥，配置缺失时抛出异常
        String configuredPrivateKey = null;
        String configuredPublicKey = null;

        if (GLOBAL_PROPERTIES != null) {
            configuredPrivateKey = GLOBAL_PROPERTIES.getRsaPrivateKey();
            configuredPublicKey = GLOBAL_PROPERTIES.getRsaPublicKey();
        }

        // 主密钥对
        PRIVATE_RSA = initRsa(configuredPrivateKey, configuredPublicKey, "RSA密钥对");
        PUBLIC_RSA = initRsa(configuredPrivateKey, configuredPublicKey, "RSA密钥对");
        // AES 密钥 - 延迟初始化，在首次使用时检查配置
        AES = null;
    }

    /**
     * 获取AES实例（线程安全，双重检查锁定）
     */
    public static SymmetricCrypto getAES() {
        if (AES == null) {
            synchronized (SecureUtil.class) {
                if (AES == null) {
                    AES = initAes();
                }
            }
        }
        return AES;
    }

    /**
     * RSA私钥加密
     *
     * @param data
     * @return java.lang.String
     * @since 2019/11/29 12:02
     **/
    public static String rsaPrivateEncryptHex(String data) {
        return PRIVATE_RSA.encryptHex(data, UTF_8, KeyType.PrivateKey);
    }


    /**
     * RSA私钥解密, 密文可以为十六进制或者Base64
     *
     * @param data
     * @return java.lang.String
     * @since 2019/11/29 0029 12:03
     **/
    public static String rsaPrivateDecryptStr(String data) {
        return PRIVATE_RSA.decryptStr(data, KeyType.PrivateKey, UTF_8);
    }


    /**
     * RSA 公钥加密
     *
     * @param data
     * @return java.lang.String
     * @since 2019/11/29 12:02
     **/
    public static String rsaPublicEncryptHex(String data) {
        return PUBLIC_RSA.encryptHex(data, UTF_8, KeyType.PublicKey);
    }


    /**
     * RSA公钥解密, 密文可以为十六进制或者Base64
     *
     * @param data
     * @return java.lang.String
     * @since 2019/11/29 12:03
     **/
    public static String rsaPublicDecryptStr(String data) {
        return PUBLIC_RSA.decryptStr(data, KeyType.PublicKey, UTF_8);
    }

    /**
     * 生成摘要
     *
     * @param data
     * @param key
     * @return java.lang.String
     * @since 2019/11/29 12:06
     **/
    public static String signWithHMac(String data, String key) {
        HMac mac = new HMac(HmacAlgorithm.HmacSHA256, key.getBytes(UTF_8));
        return mac.digestHex(data);
    }

    /**
     * 使用系统配置的AES加密成十六进制
     *
     * @param str
     * @return
     */
    public static String aesEncryptHex(String str) {
        return getAES().encryptHex(str, StandardCharsets.UTF_8);
    }

    /**
     * 使用系统配置的AES解密解密Hex（16进制）或Base64表示的字符串，默认UTF-8编码
     *
     * @param str
     * @return
     */
    public static String aesDecryptStr(String str) {
        return getAES().decryptStr(str);
    }

    /**
     * 使用指定秘钥的AES加密成十六进制
     *
     * @param str
     * @param secret 参数
     * @return
     */
    public static String aesEncryptHexWithKey(String str, String secret) {
        SymmetricCrypto aes = DYNAMIC_AES_CACHE.get(secret);
        if (Objects.isNull(aes)) {
            aes = new SymmetricCrypto(SymmetricAlgorithm.AES, secret.getBytes(UTF_8));
            DYNAMIC_AES_CACHE.put(secret, aes);
        }
        return aes.encryptHex(str, StandardCharsets.UTF_8);
    }

    /**
     * 使用指定秘钥的AES解密加密后的十六进制数据
     *
     * @param str
     * @param secret
     * @return
     */
    public static String aesDecryptStrWithKey(String str, String secret) {
        SymmetricCrypto aes = DYNAMIC_AES_CACHE.get(secret);
        if (Objects.isNull(aes)) {
            aes = new SymmetricCrypto(SymmetricAlgorithm.AES, secret.getBytes(UTF_8));
            DYNAMIC_AES_CACHE.put(secret, aes);
        }
        return aes.decryptStr(str);
    }

    /**
     * 随机散列函数摘要
     *
     * @param originStr
     * @return
     */
    public static String bCryptEncoder(String originStr) {
        return B_CRYPT_PASSWORD_ENCODER.encode(originStr);
    }

    /**
     * 验证随机散列函数摘要是否匹配
     *
     * @param originStr
     * @param encodeStr
     * @return
     */
    public static boolean bCryptMatch(String originStr, String encodeStr) {
        return B_CRYPT_PASSWORD_ENCODER.matches(originStr, encodeStr);
    }

    /**
     * Calculates the MD5 digest and returns the value as a 32 character hex string.
     *
     * @param data Data to digest
     * @return MD5 digest as a hex string
     */
    public static String md5Hex(final String data) {
        return DigestUtils.md5Hex(data);
    }
    /**
     * @param args 参数
     */
    public static void main(String[] args) {

    }
}