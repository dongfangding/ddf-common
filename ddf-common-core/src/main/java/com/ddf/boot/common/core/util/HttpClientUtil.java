package com.ddf.boot.common.core.util;

import java.io.InterruptedIOException;
import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import org.apache.http.*;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RuntimeUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

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
        CM = new PoolingHttpClientConnectionManager();
        // 最多缓存连接池数量
        CM.setMaxTotal(300);
        // 每个主机支持的最大并发连接数，共享最大缓存连接数量上限
        CM.setDefaultMaxPerRoute(50);
        // 超过这个时间的连接使用前要校验一次连接是否还能用，所以如果对方服务设置了最大的keep-alive小于这个值，可能会存在问题，要单独处理,目前增加了NoHttpResponseException的重试
        CM.setValidateAfterInactivity(60000);

        int defaultTimeoutMillis = 10000;
        DEFAULT_REQUEST_CONFIG = buildRequestConfig(defaultTimeoutMillis);
        REQUEST_CONFIG_MAP.put(defaultTimeoutMillis, DEFAULT_REQUEST_CONFIG);

        CLIENT = createHttpClient();

        printState();
    }

    private static CloseableHttpClient createHttpClient() {
        HttpRequestRetryHandler retryHandler = (exception, executionCount, context) -> {
            if (executionCount >= 3) {
                return false;
            }
            if (exception instanceof NoHttpResponseException || exception instanceof InterruptedIOException) {
                return true;
            }
            if (exception instanceof UnknownHostException || exception instanceof SSLException) {
                return false;
            }
            HttpClientContext clientContext = HttpClientContext.adapt(context);
            HttpRequest request = clientContext.getRequest();
            return !(request instanceof HttpEntityEnclosingRequest);
        };

        return HttpClients
                .custom()
                .setRetryHandler(retryHandler)
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
                .setSocketTimeout(timeoutMillis)
                // 建立tcp连接的超时时间
                .setConnectTimeout(timeoutMillis)
                // 从连接池中获取连接的超时时间
                .setConnectionRequestTimeout(1000)
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
                .scheduleAtFixedRate(() -> {
                    final PoolingHttpClientConnectionManager cm = HttpClientUtil.CM;
                    final Set<HttpRoute> routes = cm.getRoutes();
                    for (HttpRoute route : routes) {
                        log.info(
                                "连接池状态: total={}, defaultRoute={}, route = {}, available={}, leased={}, pending={}", cm
                                        .getTotalStats()
                                        .getMax(), cm.getDefaultMaxPerRoute(), route.toString(), cm
                                        .getStats(route)
                                        .getAvailable(), cm
                                        .getStats(route)
                                        .getLeased(), cm
                                        .getStats(route)
                                        .getPending());
                    }
                }, 30, 30, TimeUnit.SECONDS);
    }

    private static void applyHeaders(HttpRequestBase request, Map<String, String> headers) {
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
        httpPost.setEntity(new StringEntity(postData, Consts.UTF_8));
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
        httpPost.setEntity(new StringEntity(postData, Consts.UTF_8));
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
    private static String execute(HttpRequestBase request) {
        String result = "";
        // try-with-resources 自动 close()
        try (CloseableHttpResponse response = CLIENT.execute(request)) {
            final StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                log.error("HTTP请求失败 - url: {}, 状态码: {}", request.getURI(), statusCode);
                return result;
            }
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                result = EntityUtils.toString(resEntity, Consts.UTF_8);
                // 主动释放连接回连接池
                EntityUtils.consume(resEntity);
            }
        } catch (Exception e) {
            log.error("HTTP请求异常 - url: {}", request.getURI(), e);
            String host = request
                    .getURI()
                    .getHost();
            String pingResult = RuntimeUtil.execForStr("ping -c 3 " + host);
            log.error("HTTP请求异常 - url: {}, ping: {}", request.getURI(), pingResult, e);
        }
        return result;
    }
}
