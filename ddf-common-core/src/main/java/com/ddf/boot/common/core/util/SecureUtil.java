package com.ddf.boot.common.core.util;

import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import cn.hutool.crypto.symmetric.AES;
import com.ddf.boot.common.core.config.GlobalProperties;
import com.ddf.boot.common.core.encode.BCryptPasswordEncoder;
import com.ddf.boot.common.core.exception.SecureException;
import com.ddf.boot.common.core.helper.SpringContextHolder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
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

    private static volatile RSA PRIVATE_RSA;

    private static volatile RSA PUBLIC_RSA;

    /**
     * AES IV 长度（字节），AES 块大小固定为 16 字节
     */
    private static final int AES_IV_LENGTH = 16;

    /**
     * AES 密钥 - 延迟初始化，在首次使用时检查配置
     */
    private static volatile byte[] AES_KEY;

    /**
     * 用于生成随机 IV 的安全随机源
     */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * 密码随机散列工具类
     */
    private static final BCryptPasswordEncoder B_CRYPT_PASSWORD_ENCODER = new BCryptPasswordEncoder();

    /**
     * 安全初始化RSA密钥
     * 如果配置缺失，抛出异常而非使用硬编码密钥
     *
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
    private static byte[] initAesKey() {
        if (GLOBAL_PROPERTIES == null || StringUtils.isBlank(GLOBAL_PROPERTIES.getAesSecret())) {
            throw new SecureException("AES secret 未配置，请通过 global.aes-secret 配置 AES 密钥，"
                    + "建议使用32位随机字符串并通过配置中心管理");
        }
        return GLOBAL_PROPERTIES.getAesSecret().getBytes(UTF_8);
    }

    /**
     * 懒加载并缓存 RSA 密钥对，避免类加载时因 RSA 未配置导致无关方法（md5/bCrypt/HMac）也抛
     * {@link ExceptionInInitializerError}。
     */
    private static void initRsaKeyPair() {
        String configuredPrivateKey = null;
        String configuredPublicKey = null;

        if (GLOBAL_PROPERTIES != null) {
            configuredPrivateKey = GLOBAL_PROPERTIES.getRsaPrivateKey();
            configuredPublicKey = GLOBAL_PROPERTIES.getRsaPublicKey();
        }

        // 主密钥对 - 同一个 RSA 实例同时包含公钥和私钥
        RSA rsaKeyPair = initRsa(configuredPrivateKey, configuredPublicKey, "RSA密钥对");
        PRIVATE_RSA = rsaKeyPair;
        PUBLIC_RSA = rsaKeyPair;
    }

    private static RSA getPrivateRsa() {
        if (PRIVATE_RSA == null) {
            synchronized (SecureUtil.class) {
                if (PRIVATE_RSA == null) {
                    initRsaKeyPair();
                }
            }
        }
        return PRIVATE_RSA;
    }

    private static RSA getPublicRsa() {
        if (PUBLIC_RSA == null) {
            synchronized (SecureUtil.class) {
                if (PUBLIC_RSA == null) {
                    initRsaKeyPair();
                }
            }
        }
        return PUBLIC_RSA;
    }

    /**
     * 获取AES密钥（线程安全，双重检查锁定）
     */
    private static byte[] getAesKey() {
        if (AES_KEY == null) {
            synchronized (SecureUtil.class) {
                if (AES_KEY == null) {
                    AES_KEY = initAesKey();
                }
            }
        }
        return AES_KEY;
    }

    /**
     * RSA私钥加密
     *
     * @param data 待处理数据
     * @return java.lang.String
     * @since 2019/11/29 12:02
     **/
    public static String rsaPrivateEncryptHex(String data) {
        return getPrivateRsa().encryptHex(data, UTF_8, KeyType.PrivateKey);
    }


    /**
     * RSA私钥解密, 密文可以为十六进制或者Base64
     *
     * @param data 待处理数据
     * @return java.lang.String
     * @since 2019/11/29 0029 12:03
     **/
    public static String rsaPrivateDecryptStr(String data) {
        return getPrivateRsa().decryptStr(data, KeyType.PrivateKey, UTF_8);
    }


    /**
     * RSA 公钥加密
     *
     * @param data 待处理数据
     * @return java.lang.String
     * @since 2019/11/29 12:02
     **/
    public static String rsaPublicEncryptHex(String data) {
        return getPublicRsa().encryptHex(data, UTF_8, KeyType.PublicKey);
    }


    /**
     * RSA公钥解密, 密文可以为十六进制或者Base64
     *
     * @param data 待处理数据
     * @return java.lang.String
     * @since 2019/11/29 12:03
     **/
    public static String rsaPublicDecryptStr(String data) {
        return getPublicRsa().decryptStr(data, KeyType.PublicKey, UTF_8);
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
        HMac mac = new HMac(HmacAlgorithm.HmacSHA256, key.getBytes(UTF_8));
        return mac.digestHex(data);
    }

    /**
     * 使用系统配置的AES加密成十六进制（CBC 模式 + 随机 IV，IV 前缀进密文）
     *
     * @param str STR参数
     */
    public static String aesEncryptHex(String str) {
        return aesCbcEncryptHex(getAesKey(), str);
    }

    /**
     * 使用系统配置的AES解密十六进制密文（CBC 模式，从密文中分离前缀 IV）
     *
     * @param str STR参数
     */
    public static String aesDecryptStr(String str) {
        return aesCbcDecryptStr(getAesKey(), str);
    }

    /**
     * 使用指定秘钥的AES加密成十六进制（CBC 模式 + 随机 IV，IV 前缀进密文）
     *
     * @param str STR参数
     * @param secret 签名密钥
     */
    public static String aesEncryptHexWithKey(String str, String secret) {
        return aesCbcEncryptHex(secret.getBytes(UTF_8), str);
    }

    /**
     * 使用指定秘钥的AES解密加密后的十六进制数据（CBC 模式，从密文中分离前缀 IV）
     *
     * @param str STR参数
     * @param secret 签名密钥
     */
    public static String aesDecryptStrWithKey(String str, String secret) {
        return aesCbcDecryptStr(secret.getBytes(UTF_8), str);
    }

    /**
     * AES-CBC 加密，随机 IV 前缀进密文，避免 ECB 模式的可预测性。
     *
     * @param key AES 密钥
     * @param str 明文
     * @return IV + 密文的十六进制拼接
     */
    private static String aesCbcEncryptHex(byte[] key, String str) {
        byte[] iv = new byte[AES_IV_LENGTH];
        SECURE_RANDOM.nextBytes(iv);
        AES aes = new AES(Mode.CBC, Padding.PKCS5Padding, key, iv);
        byte[] cipher = aes.encrypt(str, UTF_8);
        return HexUtil.encodeHexStr(iv) + HexUtil.encodeHexStr(cipher);
    }

    /**
     * AES-CBC 解密，从 hex 密文中分离前缀 IV。
     *
     * @param key AES 密钥
     * @param str IV + 密文的十六进制拼接
     * @return 明文
     */
    private static String aesCbcDecryptStr(byte[] key, String str) {
        byte[] data = HexUtil.decodeHex(str);
        if (data.length <= AES_IV_LENGTH) {
            throw new SecureException("AES 密文非法");
        }
        byte[] iv = Arrays.copyOfRange(data, 0, AES_IV_LENGTH);
        byte[] cipher = Arrays.copyOfRange(data, AES_IV_LENGTH, data.length);
        AES aes = new AES(Mode.CBC, Padding.PKCS5Padding, key, iv);
        return aes.decryptStr(cipher, UTF_8);
    }

    /**
     * 随机散列函数摘要
     *
     * @param originStr originSTR参数
     */
    public static String bCryptEncoder(String originStr) {
        return B_CRYPT_PASSWORD_ENCODER.encode(originStr);
    }

    /**
     * 验证随机散列函数摘要是否匹配
     *
     * @param originStr originSTR参数
     * @param encodeStr encodeSTR参数
     */
    public static boolean bCryptMatch(String originStr, String encodeStr) {
        return B_CRYPT_PASSWORD_ENCODER.matches(originStr, encodeStr);
    }

    /**
     * Calculates the MD5 digest and returns the value as a 32 character hex string.
     *
     * @param data 待处理数据
     * @return MD5 digest as a hex string
     */
    public static String md5Hex(final String data) {
        return DigestUtils.md5Hex(data);
    }
}
