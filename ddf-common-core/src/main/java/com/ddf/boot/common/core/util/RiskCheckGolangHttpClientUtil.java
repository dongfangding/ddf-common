package com.ddf.boot.common.core.util;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.util.List;
import javax.net.ssl.SSLException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;


/**
 * http client util
 *
 * @author snowball
 * @date 2023/10/19 23:29
 **/
@Slf4j
public class RiskCheckGolangHttpClientUtil {
    private static final PoolingHttpClientConnectionManager CM;
    private static final RequestConfig REQUEST_CONFIG;
    private static final CloseableHttpClient CLIENT;

    static {
        CM = new PoolingHttpClientConnectionManager();
        CM.setMaxTotal(100);// 多线程调用注意配置，根据线程数设定
        CM.setDefaultMaxPerRoute(50);
        CM.setValidateAfterInactivity(10000);

        REQUEST_CONFIG = RequestConfig.custom().setSocketTimeout(10000)// 数据传输过程中数据包之间间隔的最大时间
                .setConnectTimeout(1000)// 连接建立时间，三次握手完成时间
                .setExpectContinueEnabled(true)// 重点参数
                .setConnectionRequestTimeout(1000).build();

        CLIENT = HttpClientUtil.getHttpClient();
    }

    public static CloseableHttpClient getHttpClient() {
        HttpRequestRetryHandler httpRequestRetryHandler = (e, i, httpContext) -> {
            System.out.println("try httpRequestRetryHandler o: " + i);
            if (i >= 3) {
                // Do not retry if over max retry count
                return false;
            }
            if (e instanceof InterruptedIOException) {
                // Timeout
                return true;
            }
            if (e instanceof UnknownHostException) {
                // Unknown host
                return false;
            }
            if (e instanceof SSLException) {
                // SSL handshake exception
                return false;
            }
            HttpClientContext clientContext = HttpClientContext.adapt(httpContext);
            HttpRequest request = clientContext.getRequest();
            boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
            if (idempotent) {
                // Retry if the request is considered idempotent
                return true;
            }
            return false;

        };
        return HttpClients.custom().setRetryHandler(httpRequestRetryHandler).setConnectionManager(CM).build();
    }


    public static String post(String url,  List<Header> headers) {
        HttpPost httpPost = new HttpPost(url);
        // 得指明使用UTF-8编码，
        Header[] headersArray = headers.toArray(new Header[headers.size()]);
        httpPost.setHeaders(headersArray);
        return executePost(httpPost);
    }



    private static String executePost(HttpPost post) {
        post.setConfig(REQUEST_CONFIG);
        return execute(post);
    }

    private static String execute(HttpRequestBase post) {
        CloseableHttpResponse response = null;
        String result = "";
        try {
            response = CLIENT.execute(post);
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            if (statusCode != HttpStatus.SC_OK) {
                log.error("http工具请求-请求发送失败, url = {},statusCode:{}", post.getRequestLine(),statusCode);
                return result;
            }
            HttpEntity resEntity = response.getEntity();
            if (resEntity == null) {
                return result;
            }
            result = EntityUtils.toString(resEntity, "UTF-8");
        } catch (Exception e) {
            log.error("http工具请求-处理异常, url = {}", post.getRequestLine(), e);
        } finally {
            if (response != null) {
                try {
                    // 此处调优重点，多线程模式下可提高性能。
                    EntityUtils.consume(response.getEntity());// 此处高能，通过源码分析，由EntityUtils是否回收HttpEntity
                    response.close();
                } catch (IOException e) {
                    log.error("http工具请求-关闭response失败:" + e);
                }
            }
        }
        return result;
    }
}

