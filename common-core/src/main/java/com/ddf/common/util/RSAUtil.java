package com.ddf.common.util;


import org.springframework.core.io.ClassPathResource;

import javax.crypto.Cipher;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * RSA工具类
 * <p>
 * RSA加密对明文的长度有所限制，规定需加密的明文最大长度=密钥长度-11（单位是字节，即byte），所以在加密和解密的过程中需要分块进行。
 * 而密钥默认是1024位，即1024位/8位-11=128-11=117字节。所以默认加密前的明文最大长度117字节，解密密文最大长度为128字。
 * 那么为啥两者相差11字节呢？是因为RSA加密使用到了填充模式（padding），即内容不足117字节时会自动填满，用到填充模式自然会占用一定的字节，
 * 而且这部分字节也是参与加密的。
 * <p>
 * https://www.cnblogs.com/pcheng/p/9629621.html
 *
 * @author admin
 */
public class RSAUtil {

    /**
     * RSA秘钥大小
     */
    private static final int KEY_SIZE = 2048;

    /**
     * RSA最大加密明文大小
     */
    private static final int MAX_ENCRYPT_BLOCK = 117 * (KEY_SIZE / 1024);

    /**
     * RSA最大解密密文大小
     */
    private static final int MAX_DECRYPT_BLOCK = 128 * (KEY_SIZE / 1024);

    /**
     * 服务端公钥
     */
    private static final PublicKey SERVER_PUBLIC_KEY;
    /**
     * 服务端私钥
     */
    private static final PrivateKey SERVER_PRIVATE_KEY;
    /**
     * 客户端公钥
     */
    private static final PublicKey CLIENT_PUBLIC_KEY;
    /**
     * 客户端私钥
     */
    private static final PrivateKey CLIENT_PRIVATE_KEY;

    private static final String BASE_DIR = "/RSA";

    static {
        SERVER_PUBLIC_KEY = getPublicKey(readKeyByClassPathFile(BASE_DIR + "/服务端公钥2048.txt"));
        SERVER_PRIVATE_KEY = getPrivateKey(readKeyByClassPathFile(BASE_DIR + "/服务端私钥2048.txt"));
        CLIENT_PUBLIC_KEY = getPublicKey(readKeyByClassPathFile(BASE_DIR + "/应用公钥2048.txt"));
        CLIENT_PRIVATE_KEY = getPrivateKey(readKeyByClassPathFile(BASE_DIR + "/应用私钥2048.txt"));
    }

    /**
     * 获取密钥对
     *
     * @return 密钥对
     */
    public static KeyPair getKeyPair() {
        KeyPairGenerator generator;
        try {
            generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(KEY_SIZE);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String readKeyByFile(String keyFilePath) {
        try {
            return getContext(new FileReader(keyFilePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }


    private static String getContext(InputStreamReader inputStreamReader) {
        try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            String str;
            StringBuilder sbl = new StringBuilder();
            while ((str = bufferedReader.readLine()) != null) {
                if (str.contains("---")) {
                    continue;
                }
                sbl.append(str);
            }
            return sbl.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 从文件中读取秘钥字符串
     *
     * @param keyFilePath
     * @return
     */
    public static String readKeyByClassPathFile(String keyFilePath) {
        ClassPathResource classPathResource = new ClassPathResource(keyFilePath);
        System.out.println("======================================================");
        try {
            InputStream inputStream = classPathResource.getInputStream();
            return getContext(new InputStreamReader(inputStream));
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }




    /**
     * 获取私钥
     *
     * @param privateKey 私钥字符串
     * @return
     */
    public static PrivateKey getPrivateKey(String privateKey) {
        KeyFactory keyFactory;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
            byte[] decodedKey = Base64.getDecoder().decode(privateKey.getBytes());
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
            return keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取公钥
     *
     * @param publicKey 公钥字符串
     * @return
     */
    public static PublicKey getPublicKey(String publicKey) {
        KeyFactory keyFactory;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
            byte[] decodedKey = Base64.getDecoder().decode(publicKey.getBytes());
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
            return keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 服务端默认加密
     * @param data
     * @return
     */
    public static String encryptByServerPrivateKey(String data) {
        return encrypt(data, SERVER_PRIVATE_KEY);
    }

    /**
     * 客户端默认加密
     * @param data
     * @return
     */
    public static String encryptByClientPrivateKey(String data) {
        return encrypt(data, CLIENT_PRIVATE_KEY);
    }

    /**
     * RSA加密，要知道私钥和公钥是相对的概念，所以可以使用私钥加密也可以使用公钥加密，
     * 加密之后只有它对应的公钥或私钥才能解密
     *
     * @param data 待加密数据
     * @param key  秘钥
     * @return
     */
    public static String encrypt(String data, Key key) {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            int inputLen = data.getBytes().length;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int offset = 0;
            byte[] cache;
            int i = 0;
            // 对数据分段加密
            while (inputLen - offset > 0) {
                if (inputLen - offset > MAX_ENCRYPT_BLOCK) {
                    cache = cipher.doFinal(data.getBytes(), offset, MAX_ENCRYPT_BLOCK);
                } else {
                    cache = cipher.doFinal(data.getBytes(), offset, inputLen - offset);
                }
                out.write(cache, 0, cache.length);
                i++;
                offset = i * MAX_ENCRYPT_BLOCK;
            }
            byte[] encryptedData = out.toByteArray();
            out.close();
            // 获取加密内容使用base64进行编码,并以UTF-8为标准转化成字符串
            // 加密后的字符串
            return new String(Base64.getEncoder().encode(encryptedData), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 使用服务端的私钥解密
     * @param data
     * @return
     */
    public static String decryptByServerPrivateKey(String data) {
        return decrypt(data, SERVER_PRIVATE_KEY);
    }

    /**
     * 使用服务端的公钥解密
     * @param data
     * @return
     */
    public static String decryptByServerPublicKey(String data) {
        return decrypt(data, SERVER_PUBLIC_KEY);
    }

    /**
     * 使用客户端的私钥解密
     * @param data
     * @return
     */
    public static String decryptByClientPrivateKey(String data) {
        return decrypt(data, CLIENT_PRIVATE_KEY);
    }

    /**
     * 使用客户端的公钥解密
     * @param data
     * @return
     */
    public static String decryptByClientPublicKey(String data) {
        return decrypt(data, CLIENT_PUBLIC_KEY);
    }


    /**
     * RSA解密，要知道私钥和公钥是相对的概念，所以可以使用私钥加密也可以使用公钥加密，
     * 加密之后只有它对应的公钥或私钥才能解密
     *
     * @param data 待解密数据
     * @param key  秘钥
     * @return
     */
    public static String decrypt(String data, Key key) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] dataBytes = Base64.getDecoder().decode(data);
            int inputLen = dataBytes.length;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int offset = 0;
            byte[] cache;
            int i = 0;
            // 对数据分段解密
            while (inputLen - offset > 0) {
                if (inputLen - offset > MAX_DECRYPT_BLOCK) {
                    cache = cipher.doFinal(dataBytes, offset, MAX_DECRYPT_BLOCK);
                } else {
                    cache = cipher.doFinal(dataBytes, offset, inputLen - offset);
                }
                out.write(cache, 0, cache.length);
                i++;
                offset = i * MAX_DECRYPT_BLOCK;
            }
            byte[] decryptedData = out.toByteArray();
            out.close();
            // 解密后的内容
            return new String(decryptedData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 使用服务端的私钥加签
     * @param data
     * @return
     */
    public static String signByServerPrivateKey(String data) {
        return sign(data, SERVER_PRIVATE_KEY);
    }


    /**
     * 使用客户端的私钥加签
     * @param data
     * @return
     */
    public static String signByClientPrivateKey(String data) {
        return sign(data, CLIENT_PRIVATE_KEY);
    }

    /**
     * 签名
     *
     * @param data       待签名数据
     * @param privateKey 私钥
     * @return 签名
     */
    public static String sign(String data, PrivateKey privateKey) {
        try {
            byte[] keyBytes = privateKey.getEncoded();
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey key = keyFactory.generatePrivate(keySpec);
            Signature signature = Signature.getInstance("MD5withRSA");
            signature.initSign(key);
            signature.update(data.getBytes());
            return new String(Base64.getEncoder().encode(signature.sign()), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 服务端使用客户端的公钥验签
     * @return
     */
    public static boolean verifyByClientPublicKey(String srcData, String sign) {
        return verify(srcData, CLIENT_PUBLIC_KEY, sign);
    }

    /**
     * 客户端使用服务端的公钥验签
     */
    public static boolean verifyByServerPublicKey(String srcData, String sign) {
        return verify(srcData, SERVER_PUBLIC_KEY, sign);
    }

    /**
     * 验签
     *
     * @param srcData   原始字符串
     * @param publicKey 公钥
     * @param sign      签名
     * @return 是否验签通过
     */
    public static boolean verify(String srcData, Key publicKey, String sign) {
        try {
            byte[] keyBytes = publicKey.getEncoded();
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey key = keyFactory.generatePublic(keySpec);
            Signature signature = Signature.getInstance("MD5withRSA");
            signature.initVerify(key);
            signature.update(srcData.getBytes());
            return signature.verify(Base64.getDecoder().decode(sign.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        // 生成密钥对
        KeyPair keyPair = getKeyPair();
        String privateKey = new String(Base64.getEncoder().encode(keyPair.getPrivate().getEncoded()));
        String publicKey = new String(Base64.getEncoder().encode(keyPair.getPublic().getEncoded()));
        System.out.println("私钥:" + privateKey);
        System.out.println("公钥:" + publicKey);
        // RSA加密
        String data = "待加密的文字内容";
        String encryptData = encrypt(data, getPublicKey(publicKey));
        System.out.println("加密后内容:" + encryptData);
        // RSA解密
        String decryptData = decrypt(encryptData, getPrivateKey(privateKey));
        System.out.println("解密后内容:" + decryptData);

        // RSA签名
        String sign = sign(data, getPrivateKey(privateKey));
        // RSA验签
        boolean result = verify(decryptData, getPublicKey(publicKey), sign);
        System.out.println("验签结果:" + result);


        String baseDir = System.getProperty("bootUser.dir") + "/src/main/resources/RSA";
        PublicKey serverPublicKey = getPublicKey(readKeyByFile(baseDir + "/服务端公钥2048.txt"));
        PrivateKey serverPrivateKey = getPrivateKey(readKeyByFile(baseDir + "/服务端私钥2048.txt"));
        PublicKey clientPublicKey = getPublicKey(readKeyByFile(baseDir + "/应用公钥2048.txt"));
        PrivateKey clientPrivateKey = getPrivateKey(readKeyByFile(baseDir + "/应用私钥2048.txt"));

        data = "我是一个粉刷匠";
        // 客户端使用服务端的公钥加密要发送的数据
        encryptData = encrypt(data, serverPublicKey);
        // 客户端使用使用自己的私钥对数据进行加签
        sign = sign(encryptData, clientPrivateKey);


        // 使用自己的私钥解密
        System.out.println("===========================================");
        System.out.println("服务端接收到加密内容: " + encryptData);
        System.out.println("服务端收到加签内容: " + sign);
        decryptData = decrypt(encryptData, serverPrivateKey);
        System.out.println("decryptData = " + decryptData);

        // 根据客户端的公钥验签，根据解密的值来判断是否和加签的值相匹配
        boolean verify = verify(encryptData, clientPublicKey, sign);
        System.out.println("验签结果: " + verify);


        // =====如果只有一对秘钥，那么服务端使用私钥加密，客户端通过公钥解密
        encryptData = encrypt(data, serverPrivateKey);
        System.out.println("解密后的数据: " + decrypt(encryptData, serverPublicKey));


        // ===========================服务端向客户端推送数据加签加密流程==========================
        data = "我是一个粉刷匠";
        // 服务端使用自己的私钥对数据进行加密
        encryptData = encrypt(data, serverPrivateKey);
        // 服务端使用自己的私钥对原始数据进行加签
        sign = sign(data, serverPrivateKey);

        // 客户端收到数据使用服务端的公钥进行解密
        decryptData = decrypt(encryptData, serverPublicKey);
        System.out.println("客户端解密结果: " + decryptData);
        // 客户端对解密后的数据使用服务端的公钥进行加签和收到的签名sign比对是否一致
        verify = verify(decryptData, serverPublicKey, sign);
        System.out.println("客户端验签结果: " + verify);
    }
}