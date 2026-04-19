package com.ddf.boot.common.alarm.util;

import com.ddf.boot.common.alarm.config.LarkProperties;
import com.ddf.boot.common.alarm.model.LarkContentRequest;
import com.ddf.boot.common.api.exception.ServerErrorException;
import com.ddf.boot.common.api.util.DateUtils;
import com.ddf.boot.common.api.util.JsonUtil;
import com.ddf.boot.common.core.helper.SpringContextHolder;
import com.ddf.boot.common.core.util.HttpClientUtil;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

/**
 * <p>Lark机器人工具</p >
 * https://open.larksuite.com/document/client-doc
 * s/bot-v3/add-custom-bot?lang=zh-CN
 *
 * @author snowball
 * @version 1.0
 * @since 2024/06/04 15:01
 */
@Slf4j
public class LarkUtil {

    /**
     * 飞书 @所有人的标签。
     */
    private static final String LARK_AT_ALL_TAG = "<at user_id=\"all\">所有人</at>";

    private static final LarkProperties LARK_PROPERTIES;

    static {
        LARK_PROPERTIES = SpringContextHolder.getBeanWithStatic(LarkProperties.class);
    }


    /**
     * 使用Lark发送msg_type=text类型的文本消息
     *
     * @param webhookUrl Webhook 地址
     * @param secret 签名密钥
     * @param text 文本内容
     * @param isAtAll 是否 @ 所有人
     */
    public static void sendTextMsgType(String webhookUrl, String secret, String text, boolean isAtAll) {
        try {
            final Map<String, Object> params = buildText(secret, text, isAtAll);
            final String result = HttpClientUtil.postJson(webhookUrl, JsonUtil.toJson(params));
            final Map resultMap = JsonUtil.toBean(result, Map.class);
            final Object code = resultMap.get("code");
            if (Objects.isNull(code) || !(code instanceof Integer) || (Integer) code != 0) {
                log.error("Lark机器人发送消息失败,返回结果:{}", result);
            }
        } catch (Exception e) {
            log.error("Lark机器人发送消息失败", e);
        }
    }

    /**
     * 使用Lark发送msg_type=post类型的富文本消息
     *
     * @param webhookUrl Webhook 地址
     * @param secret 签名密钥
     * @param title 标题
     * @param contentRequest 富文本内容请求对象
     */
    public static void sendPostMsgType(String webhookUrl, String secret, String title,
            LarkContentRequest contentRequest) {
        try {
            final Map<String, Object> params = buildPostMsgTypeContent(secret, title, contentRequest);
            final String result = HttpClientUtil.postJson(webhookUrl, JsonUtil.toJson(params));
            final Map resultMap = JsonUtil.toBean(result, Map.class);
            final Object code = resultMap.get("code");
            if (Objects.isNull(code) || !(code instanceof Integer) || (Integer) code != 0) {
                log.error("Lark机器人发送消息失败,返回结果:{}", result);
            }
        } catch (Exception e) {
            log.error("Lark机器人发送消息失败", e);
        }
    }

    /**
     * {
     * "msg_type": "post",
     * "content": {
     * "post": {
     * "zh_cn": {
     * "title": "项目更新通知",
     * "content": [
     * [{
     * "tag": "text",
     * "text": "项目有更新: "
     * },
     * {
     * "tag": "a",
     * "text": "请查看",
     * "href": "http://www.example.com/"
     * },
     * {
     * "tag": "at",
     * "user_id": "ou_18eac8********17ad4f02e8bbbb"
     * }
     * ]
     * ]
     * }
     * }
     * }
     * }
     *
     * @param secret 签名密钥
     * @param title 标题
     * @param contentRequest 富文本内容请求对象
     * @return
     */
    public static Map<String, Object> buildPostMsgTypeContent(String secret, String title,
            LarkContentRequest contentRequest) {
        final Long currentTimeSeconds = DateUtils.currentTimeSeconds();
        Map<String, Object> params = new HashMap<>();
        params.put("timestamp", currentTimeSeconds);
        params.put("sign", genSign(secret, currentTimeSeconds));
        params.put("msg_type", "post");

        Map<String, Object> detailContent = new HashMap<>();
        detailContent.put("title", title);
        detailContent.put("content", contentRequest.getContent());

        Map<String, Object> languageMap = new HashMap<>();
        languageMap.put("zh_cn", detailContent);

        Map<String, Object> postMap = new HashMap<>();
        postMap.put("post", languageMap);
        params.put("content", postMap);
        return params;
    }

    /**
     * 构建text文本消息
     *
     * @param text 文本内容
     * @param isAtAll 是否 @ 所有人
     * @param secret 签名密钥
     * @return
     */
    public static Map<String, Object> buildText(String secret, String text, boolean isAtAll) {
        final Long currentTimeSeconds = DateUtils.currentTimeSeconds();
        Map<String, Object> params = new HashMap<>();
        params.put("timestamp", currentTimeSeconds);
        params.put("sign", genSign(secret, currentTimeSeconds));
        params.put("msg_type", "text");

        Map<String, Object> textParams = new HashMap<>();
        if (isAtAll) {
            textParams.put("text", LARK_AT_ALL_TAG + text);
        } else {
            textParams.put("text", text);
        }
        params.put("content", textParams);
        return params;
    }
    /**
     * @param secret 签名密钥
     * @param timestamp 参数
     */
    private static String genSign(String secret, long timestamp) {
        try {
            //把timestamp+"\n"+密钥当做签名字符串
            String stringToSign = timestamp + "\n" + secret;
            //使用HmacSHA256算法计算签名
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(stringToSign.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signData = mac.doFinal(new byte[] {});
            return new String(Base64.encodeBase64(signData));
        } catch (Exception e) {
            throw new ServerErrorException(e.getMessage());
        }
    }
}