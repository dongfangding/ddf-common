package com.ddf.boot.common.util;

import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import org.springframework.core.env.Environment;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


/**
 *
 * 加密工具类
 *
 * _ooOoo_
 * o8888888o
 * 88" . "88
 * (| -_- |)
 * O\ = /O
 * ___/`---'\____
 * .   ' \\| |// `.
 * / \\||| : |||// \
 * / _||||| -:- |||||- \
 * | | \\\ - /// | |
 * | \_| ''\---/'' | |
 * \ .-\__ `-` ___/-. /
 * ___`. .' /--.--\ `. . __
 * ."" '< `.___\_<|>_/___.' >'"".
 * | | : `- \`.;`\ _ /`;.`/ - ` : | |
 * \ \ `-. \_ __\ /__ _/ .-` / /
 * ======`-.____`-.___\_____/___.-`____.-'======
 * `=---='
 * .............................................
 * 佛曰：bug泛滥，我已瘫痪！
 *
 */
public class SecureUtil {

    private SecureUtil() {}

    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    private static RSA privateRsa;

    private static RSA publicRsa;

    private static final String PRIMARY_KEY = SpringContextHolder.getBean(Environment.class)
            .getProperty("customs.rsa.primaryKey");

//    private static final String PRIMARY_KEY = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAIPT0VbUrdYMIX//tgHgaMh9PUtqG6R/lNOvIl9LR6Mbc5J03qjvLlu2IifHFbFonm6XguZG4LZ4H3hRkX9fC4qSaQR1vZBOvS1aNLRyvMZudISJygiag1qJOQMEwrb8JUZkP7NMpBNjYpmF344+25Il3axRnd1oVR4Hhp4tqEe1AgMBAAECgYBYv0i3BAbjitcirKuDJ+hi0K1rD8v8OkefGtAxByT7EYgEmNktMZgr9bmYvdZE0QGXwjhFfoHZVZUaEw+4h+vkJrXxv2FQHqTSL8fDeL4jggNO3YYpLFoq9bvwWeqDJ3vvVkJEbNycfoE6iXv91mUM3hb7ie1NlvWYOJtiAxf0AQJBAOzyLcea/8Au8nVnJrVstpVaItkib7pT/9w2BDAe821JTXsmUCGw47LpYHZos6G8pR9Hz8VRaj99z8Z5xa2fe2ECQQCObaUe0n67ddr3OExVF9LyFVEdGzvuOwJjQi6/URvhrQDV31kld2z9lqV6p2l6JpDpmQIwPsJTS7qbLLhjGqDVAkEAz+Gh3I/Wdiw6OFqpkV6xydLs5AfccmMkBXW2sulUtLstKTBx+T0SaHNsWDZ/8xRo4krEtN87Ej01P3KyxiM3wQJAaJirF5ycR40AtmeY3zD00KXJAOgcNhMN6NkUvZmSMUS9BVPWAwbWetEkS5QgiP1DlNmyWr3sNgG6U/UeoGFQ1QJAZief8zE0C5jCmWf7VwfAJCSd+r+BVcSCpUxskc3gnhfG6j+ImAirhXIY+ld8rArmGl4UPkQfapBphUJslWLpLg==\n";


    private static final String PUBLIC_KEY = SpringContextHolder.getBean(Environment.class)
        .getProperty("customs.rsa.publicKey");

//    private static final String PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCD09FW1K3WDCF//7YB4GjIfT1Lahukf5TTryJfS0ejG3OSdN6o7y5btiInxxWxaJ5ul4LmRuC2eB94UZF/XwuKkmkEdb2QTr0tWjS0crzGbnSEicoImoNaiTkDBMK2/CVGZD+zTKQTY2KZhd+OPtuSJd2sUZ3daFUeB4aeLahHtQIDAQAB\n";


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
}
