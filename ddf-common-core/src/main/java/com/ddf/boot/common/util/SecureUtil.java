package com.ddf.boot.common.util;

import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import org.springframework.core.env.Environment;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class SecureUtil {

    private SecureUtil() {}

    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    private final static RSA PRIVATE_RSA;

    private final static RSA PUBLIC_RSA;

    private final static RSA LOCAL_PRIVATE_RSA;

    private final static RSA LOCAL_PUBLIC_RSA;

    /**
     * 本地公钥私钥
     */
    private static final  String LOCAL_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCHTMSHP88BKSwWLlrab7t+yZQmdsVfDLyoJjAisP35C+sfYZVUGlPqntqpuMC6Q2gxAgN1sybVfJ+B/xqx7/1RXjwiS64VMSaScFoGcsTBxptCbt4/TDUcpE4UKrG7pizPL6ID6fYbPLvzhh6s14w1zVz7OL39zJ1l0AhUPDez/QIDAQAB";
    private static final  String LOCAL_PRIVATE_KEY = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAIdMxIc/zwEpLBYuWtpvu37JlCZ2xV8MvKgmMCKw/fkL6x9hlVQaU+qe2qm4wLpDaDECA3WzJtV8n4H/GrHv/VFePCJLrhUxJpJwWgZyxMHGm0Ju3j9MNRykThQqsbumLM8vogPp9hs8u/OGHqzXjDXNXPs4vf3MnWXQCFQ8N7P9AgMBAAECgYAbox/F7M/REeLyiPeABTDMfkqn7Lz2ZHio9FwCyhqm47tchqdlLZeUpmxOHPIpWhmPYTTptvWoyDMg78Y5MKeSyZcFOpzkTKjcJGUwEimgZCjl5Xsnqv/rK5TR3ADggmrAEkJ5+bdf5IWSStBpHDbZhg6Xll45cTRZNuw8V+9GgQJBANbQTqwekzZFJmhxr5m1E+RtsEQpkHAOoCC7vAdHFJdWPZX6wuw9wBWNxlr9Z6GkS6pHwu2ijTQb1S8Aa+w4siECQQChPa+t0vTShyVdzUpVsRryPF8BZik5q3varWA2LmyhqOmpXKtoNahazb4YNC857Co4WHGzlHQ4jP4VhRAHGz5dAkA/kZFWeg3SZ5BAJDR05hMm7BbXdP1bS9izFxtDhBNh3ZGICpcYVgW72yKx1n+OZBJIJ8hVjl7+5qWlrRhC5VxBAkBGNa8euIIkffaWXsLkh2bdXc5ctJh05SfcM6x2S0bAKeX8+j4k9WBmkboZnfeGeEB2IoT4Fkd5LGOjCTrObV19AkEArl8C9M2cQu3qBtBfb721u07bDJpo5LmjyKKM2JzboU38Vjy3O25kL8lLPVi13GeUrenUUp8ZtEVR6Gz+Sjowgw==";

    private static final String PRIMARY_KEY = SpringContextHolder.getBean(Environment.class)
            .getProperty("customs.rsa.primaryKey");

    private static final String PUBLIC_KEY = SpringContextHolder.getBean(Environment.class)
            .getProperty("customs.rsa.publicKey");

    static {
        PRIVATE_RSA = new RSA(PRIMARY_KEY, null);
        PUBLIC_RSA = new RSA(null, PUBLIC_KEY);

        LOCAL_PRIVATE_RSA = new RSA(LOCAL_PRIVATE_KEY, null);
        LOCAL_PUBLIC_RSA = new RSA(null, LOCAL_PUBLIC_KEY);
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

    public static void main(String[] args) {
        System.out.println(localPublicEncryptBcd("ANDROID;huawei-1aac7aa5-d1e9-43f6-a139-4e6fe1e6f15b;13542612549;" + System.currentTimeMillis()));
        System.out.println(localPublicDecryptFromBcd("6A0A2311BC326836F6F3D62056404AE3D0D0593C11B1117A91E1C7714EECCDB30473577B6BB977D07391585B671D0CD8F492B16D691165E557335E102B9AD9F180CDC980C360D86DD9D090CCFEA39FCAD4347C12E685940497B66A12CABAFA7A02647B1734ACFC79728966FECF287795043BBF86DF5EAAD0B77E65B182F37E601CA075514BD8E8F0957616198A6617AAB6199E9428D703730E0B6F7673572E7DD97503A3498FFBE8659F2A92CDEFE1450DB40200703F26891CB59DC4090D77615B177DCC14C5C573B68F495983386D5BAAF7F1214D124A8AE55EED569EF66A20387087283C7308D36B60E40A78758A8413E73DE8E3A222E2BB692706958CE187725ED3B1894E490C4ABF425BE7E6827B30D7ABC8780627C9BD1DF6E8EDF4B1C45410FDEE920332AAF2D28CA0CEBCD35C27DA13DF48AC20CD2C1487A03696BAD6683AEEBE189C68A3AA0A4F4B7552EC5BD0A3895F4924EA4C085D18D13630AF1EC6FFD054063577F0C15A1BA738580701065AB594213D101886B17ADF8AFB66A50A5BF063D68383859BA0DDDD88C82149773CD729EB7A7A1356BBF360D15C029A16A2FE12B60EB2491506FF75EE1579D9265FC3F897486F19F1DED7DBFD92647161C9A5D56F4D5ABDC999B577C39065FCC9D1315CAC47CED33D393DE710F475C559785CB70C66B912CAFA5A6F9F7A452352FF50A94EBC2C1DBF09DF6C6C64DC230FA271CEAC36B494D6080616DF136894AE498EB1CD69DF28448F68BF22FADC7C779B80148483C7351786D1C9751A883D3CB21A7B3A8DB1E52925F2A3D4E34CE9630F020B49D2DE7A53344CFF0265733FA5A040EEDB7A8C666876631246739E6B2FCFF45078CB96C44CF4E897E4C646B8E7D9319EFDFC2685C7CD8FCB6A5B573F"));

        System.out.println(localPrivateEncryptBcd("942436"));

        System.out.println(localPublicDecryptFromBcd("69DFC3F7344C0D99219D253723F2AEED7A89632200D8E6A98C153C76460BD063C6B08108EC73481B70D4BCA0ED77853E6405DCE6280A5393AD209392752BACE387F4C1D598C94849D69A3B12CE580940EC904E438F0DE352357B59D2F72C871DEDCC6B9C060EA434E0486342AB767A4AEEC0C9A7DD4CAF9008FF407FF39F217E"));
    }
}

