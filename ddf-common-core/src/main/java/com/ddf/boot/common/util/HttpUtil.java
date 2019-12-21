package com.ddf.boot.common.util;

import com.ddf.boot.common.exception.GlobalCustomizeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * http客户端工具
 */
@Slf4j
public class HttpUtil {
    
    
    /**
     * post请求
     * @param url
     * @param body
     * @param callbackResult
     * @return T
     * @author dongfang.ding
     * @date 2019/12/7 0007 19:51
     **/
    public static <T> T doPost(String url, String body, Class<T> callbackResult) {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        HttpPost httpPost = new HttpPost(url);

        StringEntity entity = new StringEntity(body, "UTF-8");
        log.debug("发送数据内容: {}", body);
        httpPost.setEntity(entity);

        httpPost.setConfig(RequestConfig.custom().setSocketTimeout(6000).setConnectTimeout(6000).build());

        httpPost.setHeader("Content-Type", "application/json;charset=utf8");

        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();
            log.debug("响应状态为: {}", response.getStatusLine());
            if (responseEntity != null) {
                String returnStr = EntityUtils.toString(responseEntity);
                log.debug("响应内容长度为: {}", responseEntity.getContentLength());
                log.debug("响应内容为: {}", returnStr);
                return JsonUtil.toBean(returnStr, callbackResult);
            }
            return null;
        } catch (ParseException | IOException e) {
            log.error("{}接口发送失败！", url, e);
            throw new GlobalCustomizeException("处理失败！");
        } finally {
            try {
                if (httpClient != null) {
                    httpClient.close();
                }
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
