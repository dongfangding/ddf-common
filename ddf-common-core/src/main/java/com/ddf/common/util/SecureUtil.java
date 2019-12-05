package com.ddf.common.util;

import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;

import java.nio.charset.Charset;

public class SecureUtil {

    private SecureUtil() {}

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private static RSA privateRsa;

    private static RSA publicRsa;

/*    private static final String PRIMARY_KEY = SpringContextHolder.getBean(Environment.class)
            .getProperty("customs.rsa.primaryKey");*/

    private static final String PRIMARY_KEY = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAIdMxIc/zwEpLBYuWtpvu37JlCZ2xV8MvKgmMCKw/fkL6x9hlVQaU+qe2qm4wLpDaDECA3WzJtV8n4H/GrHv/VFePCJLrhUxJpJwWgZyxMHGm0Ju3j9MNRykThQqsbumLM8vogPp9hs8u/OGHqzXjDXNXPs4vf3MnWXQCFQ8N7P9AgMBAAECgYAbox/F7M/REeLyiPeABTDMfkqn7Lz2ZHio9FwCyhqm47tchqdlLZeUpmxOHPIpWhmPYTTptvWoyDMg78Y5MKeSyZcFOpzkTKjcJGUwEimgZCjl5Xsnqv/rK5TR3ADggmrAEkJ5+bdf5IWSStBpHDbZhg6Xll45cTRZNuw8V+9GgQJBANbQTqwekzZFJmhxr5m1E+RtsEQpkHAOoCC7vAdHFJdWPZX6wuw9wBWNxlr9Z6GkS6pHwu2ijTQb1S8Aa+w4siECQQChPa+t0vTShyVdzUpVsRryPF8BZik5q3varWA2LmyhqOmpXKtoNahazb4YNC857Co4WHGzlHQ4jP4VhRAHGz5dAkA/kZFWeg3SZ5BAJDR05hMm7BbXdP1bS9izFxtDhBNh3ZGICpcYVgW72yKx1n+OZBJIJ8hVjl7+5qWlrRhC5VxBAkBGNa8euIIkffaWXsLkh2bdXc5ctJh05SfcM6x2S0bAKeX8+j4k9WBmkboZnfeGeEB2IoT4Fkd5LGOjCTrObV19AkEArl8C9M2cQu3qBtBfb721u07bDJpo5LmjyKKM2JzboU38Vjy3O25kL8lLPVi13GeUrenUUp8ZtEVR6Gz+Sjowgw==";


/*    private static final String PUBLIC_KEY = SpringContextHolder.getBean(Environment.class)
        .getProperty("customs.rsa.publicKey");*/

    private static final String PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCHTMSHP88BKSwWLlrab7t+yZQmdsVfDLyoJjAisP35C+sfYZVUGlPqntqpuMC6Q2gxAgN1sybVfJ+B/xqx7/1RXjwiS64VMSaScFoGcsTBxptCbt4/TDUcpE4UKrG7pizPL6ID6fYbPLvzhh6s14w1zVz7OL39zJ1l0AhUPDez/QIDAQAB";


    static {
        privateRsa = new RSA(PRIMARY_KEY, null);
        publicRsa = new RSA(null, PUBLIC_KEY);
    }


    public static RSA getPublicRsa() {
        return publicRsa;
    }

    public static RSA getPrivateRsa() {
        return privateRsa;
    }

    /**
     * 私钥加密
     *
     * @param data
     * @return java.lang.String


     **/
    public static String privateEncryptBcd(String data) {
        return privateRsa.encryptBcd(data, KeyType.PrivateKey, UTF_8);
    }


    /**
     * 私钥解密
     * @param data
     * @return java.lang.String


     **/
    public static String privateDecryptFromBcd(String data) {
        return privateRsa.decryptStrFromBcd(data, KeyType.PrivateKey, UTF_8);
    }



    /**
     * 公钥加密
     *
     * @param data
     * @return java.lang.String


     **/
    public static String publicEncryptBcd(String data) {
        return publicRsa.encryptBcd(data, KeyType.PublicKey, UTF_8);
    }


    /**
     * 公钥解密
     * @param data
     * @return java.lang.String


     **/
    public static String publicDecryptFromBcd(String data) {
        return publicRsa.decryptStrFromBcd(data, KeyType.PublicKey, UTF_8);
    }


    /**
     * 生成摘要
     * @param data
     * @param key
     * @return java.lang.String
     **/
    public static String signWithHMac(String data, String key) {
        HMac mac = new HMac(HmacAlgorithm.HmacMD5, key.getBytes(UTF_8));
        return mac.digestHex(data);
    }

    /**
     * 生成摘要
     * @param data
     * @return java.lang.String
     * @author dongfang.ding
     * @date 2019/12/5 0005 19:12
     **/
    public static String signWithHMac(String data) {
        HMac mac = new HMac(HmacAlgorithm.HmacMD5);
        return mac.digestHex(data);
    }


    public static void main(String[] args) {
        /*String str = "ANDROID;huawei-1aac7aa5-d1e9-43f6-a139-4e6fe1e6f15b;13542612549;" + (System.currentTimeMillis());

        String s = SecureUtil.publicEncryptBcd(str);
        System.out.println("s = " + s);

        String aaa = SecureUtil.signWithHMac(str, "aaa");
        System.out.println("aaa = " + aaa);

        String s1 = SecureUtil.privateDecryptFromBcd(s);

        System.out.println("s1 = " + s1);

        String aaa1 = SecureUtil.signWithHMac(s1, "aaa");

        System.out.println("aaa1 = " + aaa1);

        System.out.println(aaa.equals(aaa1));*/


        System.out.println(SecureUtil.privateDecryptFromBcd("03668026D99F564181B4EF9D275AD045B4398CC111B0D355C7781EBC4D8F800B02181837D6BA02E7C0267CDBBDC3D92924C8456D277D7A0035CD167034624854F1380407DACCEF07BE65A7924346801C34984B3237ED6A68C1C1E7DD7BD51550103706962C986BC611D21651435F9D74AEF3547220207288EFE0226365529DD7"));


    }

}
