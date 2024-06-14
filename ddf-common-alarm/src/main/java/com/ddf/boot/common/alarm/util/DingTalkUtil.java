package com.ddf.boot.common.alarm.util;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.dingtalk.api.response.OapiRobotSendResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2024/06/04 15:01
 */
@Slf4j
public class DingTalkUtil {


    /**
     * 使用钉钉发送markdown机器人， 并且@所有人
     *
     * @param secret      加签
     * @param accessToken url中带的访问token
     * @param title       markdown标题
     * @param content     markdown内容
     */
    public static void sendMarkdownMsgToAll(String secret, String accessToken, String title, String content) {
        sendMarkdownMsg(secret, accessToken, title, content, true, new ArrayList<>());
    }


    /**
     * 使用钉钉发送markdown机器人
     *
     * @param secret      加签
     * @param accessToken url中带的访问token
     * @param title       markdown标题
     * @param content     markdown内容
     * @param isAtAll     是否@所有人
     * @param atUserIds   @用户id列表
     */
    public static void sendMarkdownMsg(String secret, String accessToken, String title, String content, boolean isAtAll,
            List<String> atUserIds) {
        try {
            Long timestamp = System.currentTimeMillis();
            String stringToSign = timestamp + "\n" + secret;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            String sign = URLEncoder.encode(new String(Base64.encodeBase64(signData)),"UTF-8");
            System.out.println(sign);
            //sign字段和timestamp字段必须拼接到请求URL上，否则会出现 310000 的错误信息
            String url = String.format("https://oapi.dingtalk.com/robot/send?sign=%s&timestamp=%s&access_token=%s",
                    sign, timestamp, accessToken
            );
            DingTalkClient client = new DefaultDingTalkClient(url);
            OapiRobotSendRequest req = buildMarkdownRequest(title, content, isAtAll, atUserIds);
            OapiRobotSendResponse rsp = client.execute(req);
            if (!rsp.isSuccess()) {
                log.error("钉钉机器人消息发送失败， rsp = {}", rsp.getBody());
            }
        } catch (Exception e) {
            log.error("钉钉机器人发送消息失败", e);
        }
    }

    /**
     * 构建markdown请求类
     *
     * @param title
     * @param content
     * @param isAtAll
     * @param atUserIds
     * @return
     */
    public static OapiRobotSendRequest buildMarkdownRequest(String title, String content, boolean isAtAll,
            List<String> atUserIds) {
        OapiRobotSendRequest req = new OapiRobotSendRequest();
        //定义文本内容
        OapiRobotSendRequest.Markdown markdown = new OapiRobotSendRequest.Markdown();
        markdown.setTitle(title);
        markdown.setText(content);
        //定义 @ 对象
        OapiRobotSendRequest.At at = new OapiRobotSendRequest.At();
        at.setIsAtAll(isAtAll);
        at.setAtUserIds(atUserIds);
        //设置消息类型
        req.setMsgtype("markdown");
        req.setMarkdown(markdown);
        req.setAt(at);
        return req;
    }
}
