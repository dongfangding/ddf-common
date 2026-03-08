package com.ddf.boot.netty.broker.ssl;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 生成服务器SslContext
 * <p>
 * 安全说明：
 * 1. SSL证书密码必须通过配置文件管理，禁止使用默认值
 * 2. 生产环境应使用复杂的随机密码
 * </p>
 *
 * <p>
 * 1. 生成服务端密钥对,证书密码是123456
 * keytool -genkey -alias server_jks -keysize 2048 -validity 365 -keyalg RSA -dname "CN=localhost" -keypass server_123456 -storepass server_123456 -keystore server.jks
 * <p>
 * 2. 将生成的文件导入到证书中存储
 * keytool -export -alias server_jks -keystore server.jks -storepass server_123456 -file server.cer
 * <p>
 * 3. 客户端生成密钥对文件
 * keytool -genkey -alias client_jks -keysize 2048 -validity 365 -keyalg RSA -dname "CN=localhost" -keypass client_123456 -storepass client_123456 -keystore client.jks
 * <p>
 * 4. 将服务端的公钥导入到客户端的授信库中，这一步应该是客户端来做，服务端将证书发送给客户端
 * keytool -import -trustcacerts -alias server_jks -file server.cer -storepass client_123456 -keystore client.jks
 * <p>
 * 补充:
 * 1. 从jks中获取公钥 keytool -list -rfc -keystore client.jks -storepass client_123456
 * 2. 私钥不能直接获取
 *
 * @author dongfang.ding
 * @since 2019/7/12 15:13
 */
public class KeyManagerFactoryHelper {

    private static final Logger log = LoggerFactory.getLogger(KeyManagerFactoryHelper.class);

    /**
     * @param password 参数
     * @param type 参数
     */
    private static KeyStore keyStore;
    private static KeyManagerFactory keyManagerFactory;
    private static TrustManagerFactory trustManagerFactory;

    /**
     * 验证密码是否为默认密码，默认密码不安全
     */
    private static void validatePassword(String password, String type) {
        if ("server_123456".equals(password) || "client_123456".equals(password)) {
            throw new IllegalStateException(
                    type + " SSL证书密码使用了默认密码，这是严重的安全漏洞！请使用复杂的随机密码并通过配置文件管理");
        }
    }

    static {
        try {
            // 密钥仓库类型
            keyStore = KeyStore.getInstance("JKS");
            // X.509 标准规定了证书可以包含什么信息，并说明了记录信息的方法（数据格式）。除了签名外，所有 X.509证书还包含以
            keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
        } catch (KeyStoreException | NoSuchAlgorithmException e) {
            log.error("Failed to initialize KeyStore or KeyManagerFactory", e);
            throw new RuntimeException("Failed to initialize SSL key stores", e);
        }
    }

    /**
     * 创建默认的服务端SslContext
     *
     * @return
     * @throws Exception
     */
    public static SslContext defaultServerContext() throws Exception {
        return createServerContext(
                System.getProperty("user.dir") + "/src/main/resources/cer/server.jks",
                "server_123456");
    }

    /**
     * 创建默认的客户端SslContext
     *
     * @return
     * @throws Exception
     */
    public static SslContext defaultClientContext() throws Exception {
        return createClientContext(
                System.getProperty("user.dir") + "/src/main/resources/cer/client.jks",
                "client_123456");
    }

    /**
     * 生成服务端SslContext
     *
     * @param caPath     证书路径
     * @param caPassword 证书密码
     * @return SslContext
     * @throws Exception
     */
    public static SslContext createServerContext(String caPath, String caPassword) throws Exception {
        validatePassword(caPassword, "Server");
        keyStore.load(new FileInputStream(caPath), caPassword.toCharArray());
        keyManagerFactory.init(keyStore, caPassword.toCharArray());
        return SslContextBuilder.forServer(keyManagerFactory).build();
    }


    /**
     * 生成客户端SslContext
     *
     * @param caPath     证书路径
     * @param caPassword 证书密码
     * @return SslContext
     * @throws Exception
     */
    public static SslContext createClientContext(String caPath, String caPassword) throws Exception {
        validatePassword(caPassword, "Client");
        keyStore.load(new FileInputStream(caPath), caPassword.toCharArray());
        trustManagerFactory.init(keyStore);
        return SslContextBuilder.forClient().trustManager(trustManagerFactory).build();
    }
}