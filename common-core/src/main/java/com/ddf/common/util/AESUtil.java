package com.ddf.common.util;


import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;

/**
 * AES加解密工具
 */
public class AESUtil {

    private static final String CIPHER_MODE = "AES/CBC/PKCS5Padding";

    private static final String SECRET_KEY = "yaoke-pay-provider-message-ws";

    private static final Integer IV_SIZE = 16;

    private static final String ENCRYPT_ALG = "AES";

    private static final String ENCODE = "UTF-8";

    private static final int SECRET_KEY_SIZE = 32;

    /**
     * 创建密钥
     *
     * @return
     */
    private static SecretKeySpec createKey() {
        StringBuilder sb = new StringBuilder(SECRET_KEY_SIZE);
        sb.append(SECRET_KEY);
        if (sb.length() > SECRET_KEY_SIZE) {
            sb.setLength(SECRET_KEY_SIZE);
        }
        if (sb.length() < SECRET_KEY_SIZE) {
            while (sb.length() < SECRET_KEY_SIZE) {
                sb.append(" ");
            }
        }
        try {
            byte[] data = sb.toString().getBytes(ENCODE);
            return new SecretKeySpec(data, ENCRYPT_ALG);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 创建16位向量: 不够则用0填充
     *
     * @return
     */
    private static IvParameterSpec createIV() {
        StringBuffer sb = new StringBuffer(IV_SIZE);
        sb.append(SECRET_KEY);
        if (sb.length() > IV_SIZE) {
            sb.setLength(IV_SIZE);
        }
        if (sb.length() < IV_SIZE) {
            while (sb.length() < IV_SIZE) {
                sb.append("0");
            }
        }
        byte[] data = null;
        try {
            data = sb.toString().getBytes(ENCODE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return new IvParameterSpec(data);
    }

    /**
     * 加密：有向量16位，结果转base64
     *
     * @param context
     * @return
     */
    public static String encrypt(String context) {
        try {
            byte[] content = context.getBytes(ENCODE);
            SecretKeySpec key = createKey();
            Cipher cipher = Cipher.getInstance(CIPHER_MODE);
            cipher.init(Cipher.ENCRYPT_MODE, key, createIV());
            byte[] data = cipher.doFinal(content);
            return Base64.encodeBase64String(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 解密
     *
     * @param context
     * @return
     */
    public static String decrypt(String context) {
        try {
            byte[] data = Base64.decodeBase64(context);
            SecretKeySpec key = createKey();
            Cipher cipher = Cipher.getInstance(CIPHER_MODE);
            cipher.init(Cipher.DECRYPT_MODE, key, createIV());
            byte[] content = cipher.doFinal(data);
            return new String(content, ENCODE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}