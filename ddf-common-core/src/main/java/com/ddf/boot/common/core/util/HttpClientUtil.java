package com.ddf.boot.common.core.util;

import cn.hutool.core.collection.CollUtil;
import com.ddf.boot.common.api.exception.BaseErrorCallbackCode;
import com.ddf.boot.common.api.exception.BusinessException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.HttpRequestRetryStrategy;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.NoHttpResponseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

/**
 * http client 工具类
 *
 * @author snowball
 * @date 2025/8/13 16:31
 **/
@Slf4j
public class HttpClientUtil {
    /**
     * 连接池管理对象
     */
    private static final PoolingHttpClientConnectionManager CM;
    /**
     * 请求配置对象缓存
     */
    private static final Map<Integer, RequestConfig> REQUEST_CONFIG_MAP = new ConcurrentHashMap<>();
    /**
     * 默认请求配置
     */
    private static final RequestConfig DEFAULT_REQUEST_CONFIG;
    /**
     * 默认请求客户端
     */
    private static final CloseableHttpClient CLIENT;

    static {
        int defaultTimeoutMillis = 10000;
        // 1. 创建连接配置
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                // 建立连接超时
                .setConnectTimeout(Timeout.ofSeconds(10))
                // 超过这个时间的连接使用前要校验一次连接是否还能用，所以如果对方服务设置了最大的keep-alive小于这个值，可能会存在问题，要单独处理,目前增加了NoHttpResponseException的重试
                .setValidateAfterInactivity(TimeValue.ofSeconds(30))
                .build();
        CM = new PoolingHttpClientConnectionManager();
        // 最多缓存连接池数量
        CM.setMaxTotal(300);
        // 每个主机支持的最大并发连接数，共享最大缓存连接数量上限
        CM.setDefaultMaxPerRoute(50);
        CM.setDefaultConnectionConfig(connectionConfig);

        DEFAULT_REQUEST_CONFIG = buildRequestConfig(defaultTimeoutMillis);
        REQUEST_CONFIG_MAP.put(defaultTimeoutMillis, DEFAULT_REQUEST_CONFIG);

        CLIENT = createHttpClient();

        printState();
    }

    private static CloseableHttpClient createHttpClient() {
        // 显式实现 HttpRequestRetryStrategy 接口
        HttpRequestRetryStrategy retryStrategy = new HttpRequestRetryStrategy() {
            @Override
            public boolean retryRequest(HttpRequest request, IOException exception, int execCount,
                    HttpContext context) {
                // 1. 超过重试次数则停止
                if (execCount >= 3) {
                    return false;
                }

                // 2. 掉线或网络中断重试
                if (exception instanceof NoHttpResponseException || exception instanceof InterruptedIOException) {
                    return true;
                }

                // 3. 域名解析失败或 SSL 握手失败不重试
                if (exception instanceof UnknownHostException || exception instanceof SSLException) {
                    return false;
                }

                // 4. 幂等性判定
                // 如果请求不包含实体（如 GET），通常认为重试是安全的
                // HC5 中通过是否实现 HttpEntityContainer 接口来判断
                if (!(request instanceof HttpEntityContainer)) {
                    return true;
                }

                return false;
            }

            @Override
            public boolean retryRequest(HttpResponse response, int execCount, HttpContext context) {
                if (execCount >= 3) {
                    return false;
                }
                int status = response.getCode();
                // 常见重试场景：503 (服务不可用) 或 429 (请求过多/限流)
                if (status == HttpStatus.SC_SERVICE_UNAVAILABLE || status == 429) {
                    return true;
                }
                return false;
            }

            /**
             * 决定重试之间的等待时间
             */
            @Override
            public TimeValue getRetryInterval(HttpResponse response, int execCount, HttpContext context) {
                return TimeValue.ofSeconds(execCount * 1L);
            }
        };

        return HttpClients
                .custom()
                .setRetryStrategy(retryStrategy)
                .setConnectionManager(CM)
                .setDefaultRequestConfig(DEFAULT_REQUEST_CONFIG)
                .build();
    }

    private static RequestConfig buildRequestConfig(int timeoutMillis) {
        if (REQUEST_CONFIG_MAP.containsKey(timeoutMillis)) {
            return REQUEST_CONFIG_MAP.get(timeoutMillis);
        }
        final RequestConfig requestConfig = RequestConfig.custom()
                // 连接建立后的数据读取超时时间
                .setResponseTimeout(timeoutMillis, TimeUnit.MILLISECONDS)
                // 从连接池中获取连接的超时时间
                .setConnectionRequestTimeout(1000, TimeUnit.MILLISECONDS)
                // 是否启用 HTTP 的 Expect: 100-Continue 机制，用于在发送POST/PUT请求体之前检查服务器是否可以处理请求
                .setExpectContinueEnabled(true)
                .build();
        REQUEST_CONFIG_MAP.put(timeoutMillis, requestConfig);
        return requestConfig;
    }

    /**
     * 定时打印连接池信息
     */
    private static void printState() {
        Executors
                .newSingleThreadScheduledExecutor()
                .scheduleAtFixedRate(
                        () -> {
                            final PoolingHttpClientConnectionManager cm = HttpClientUtil.CM;
                            final Set<HttpRoute> routes = cm.getRoutes();
                            for (HttpRoute route : routes) {
                                log.info(
                                        "连接池状态: total={}, defaultRoute={}, route = {}, available={}, leased={}, pending={}",
                                        cm
                                                .getTotalStats()
                                                .getMax(), cm.getDefaultMaxPerRoute(), route.toString(), cm
                                                .getStats(route)
                                                .getAvailable(), cm
                                                .getStats(route)
                                                .getLeased(), cm
                                                .getStats(route)
                                                .getPending()
                                );
                            }
                        }, 30, 30, TimeUnit.SECONDS
                );
    }

    private static void applyHeaders(HttpUriRequestBase request, Map<String, String> headers) {
        if (CollUtil.isNotEmpty(headers)) {
            headers.forEach(request::addHeader);
        }
    }

    // ----------------- POST JSON -----------------
    public static String postJson(String url, String postData) {
        return postJson(url, postData, null, DEFAULT_REQUEST_CONFIG);
    }

    public static String postJson(String url, String postData, int timeoutMillis) {
        return postJson(url, postData, null, buildRequestConfig(timeoutMillis));
    }

    public static String postJson(String url, String postData, Map<String, String> headers) {
        return postJson(url, postData, headers, DEFAULT_REQUEST_CONFIG);
    }

    public static String postJson(String url, String postData, Map<String, String> headers, int timeoutMillis) {
        return postJson(url, postData, headers, buildRequestConfig(timeoutMillis));
    }

    private static String postJson(String url, String postData, Map<String, String> headers, RequestConfig config) {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(config);
        httpPost.addHeader("Content-Type", "application/json");
        httpPost.setEntity(new StringEntity(postData, StandardCharsets.UTF_8));
        applyHeaders(httpPost, headers);
        return execute(httpPost);
    }

    // ----------------- POST QUERY STRING -----------------
    public static String postQueryString(String url, String postData, Map<String, String> headers) {
        return postQueryString(url, postData, headers, DEFAULT_REQUEST_CONFIG);
    }

    public static String postQueryString(String url, String postData, Map<String, String> headers, int timeoutMillis) {
        return postQueryString(url, postData, headers, buildRequestConfig(timeoutMillis));
    }

    private static String postQueryString(String url, String postData, Map<String, String> headers,
            RequestConfig config) {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(config);
        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        httpPost.setEntity(new StringEntity(postData, StandardCharsets.UTF_8));
        applyHeaders(httpPost, headers);
        return execute(httpPost);
    }

    // ----------------- POST WITHOUT BODY -----------------
    public static String post(String url, List<Header> headers) {
        return post(url, headers, DEFAULT_REQUEST_CONFIG);
    }

    public static String post(String url, List<Header> headers, int timeoutMillis) {
        return post(url, headers, buildRequestConfig(timeoutMillis));
    }

    private static String post(String url, List<Header> headers, RequestConfig config) {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(config);
        if (CollUtil.isNotEmpty(headers)) {
            httpPost.setHeaders(headers.toArray(new Header[0]));
        }
        return execute(httpPost);
    }

    // ----------------- GET -----------------
    public static String get(String url) {
        return get(url, DEFAULT_REQUEST_CONFIG);
    }

    public static String get(String url, int timeoutMillis) {
        return get(url, buildRequestConfig(timeoutMillis));
    }

    private static String get(String url, RequestConfig config) {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(config);
        httpGet.addHeader("Content-Type", "application/x-www-form-urlencoded");
        return execute(httpGet);
    }

    // ----------------- EXECUTE -----------------
    private static String execute(HttpUriRequestBase request) {
        final URI uri;
        try {
            uri = request.getUri();
        } catch (URISyntaxException e) {
            log.error("HTTP请求失败, 解析路径错误 - request: {}", request, e);
            throw new BusinessException(BaseErrorCallbackCode.RESOURCE_REQUEST_ERROR);
        }
        try {
            // 使用 ResponseHandler 自动管理资源释放
            return CLIENT.execute(
                    request, response -> {
                        int statusCode = response.getCode();
                        if (statusCode != HttpStatus.SC_OK) {
                            log.error("HTTP请求失败 - url: {}, 状态码: {}", uri, statusCode);
                            // 必须消费掉 Entity 以便释放连接
                            EntityUtils.consume(response.getEntity());
                            return "";
                        }

                        HttpEntity resEntity = response.getEntity();
                        return resEntity != null ? EntityUtils.toString(resEntity, StandardCharsets.UTF_8) : "";
                    }
            );
        } catch (Exception e) {
            log.error("HTTP请求异常 - url: {}", uri, e);
            throw new BusinessException(BaseErrorCallbackCode.RESOURCE_REQUEST_ERROR);
        }
    }

    @Data
    public static class ShuMeiCaptchaCheckRequest implements Serializable {

        private String accessKey;

        private Data data;


        @lombok.Data
        public static class Data {

            private String rid;

            private String ip;

            private String tokenId;

            private String deviceId;

            private String expectedMode;

            private String expectedAppId;
        }
    }
}
