package com.ddf.boot.common.alarm.util;

import com.ddf.boot.common.alarm.config.DingTalkProperties;
import com.ddf.boot.common.alarm.enums.AlarmRedisKeyEnum;
import com.ddf.boot.common.api.exception.BusinessException;
import com.ddf.boot.common.api.util.DateUtils;
import com.ddf.boot.common.core.helper.SpringContextHolder;
import com.ddf.boot.common.redis.helper.RedisTemplateHelper;
import com.ddf.boot.common.redis.response.AccessLimitResponse;
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
import org.apache.commons.lang3.ObjectUtils;

/**
 * <p>description</p >
 *
 * @author snowball
 * @version 1.0
 * @since 2024/06/04 15:01
 */
@Slf4j
public class DingTalkUtil {

    private static final DingTalkProperties DING_TALK_PROPERTIES;
    private static final RedisTemplateHelper REDIS_TEMPLATE_HELPER;

    static {
        DING_TALK_PROPERTIES = SpringContextHolder.getBeanWithStatic(DingTalkProperties.class);
        REDIS_TEMPLATE_HELPER = SpringContextHolder.getBean(RedisTemplateHelper.class);
    }

    /**
     * 使用钉钉发送markdown机器人， 并且@所有人, 并且限制发送次数，主要是为了避免前期就把每月额度用完了，后面直接哑火。所有每天定量，只影响当天。
     *
     * @param secret 签名密钥
     * @param accessToken URL 中的 accessToken
     * @param title 标题
     * @param content 内容
     */
    public static void sendMarkdownMsgToAllWithLimit(String secret, String accessToken, String title, String content) {
        if (!ObjectUtils.allNotNull(DING_TALK_PROPERTIES, REDIS_TEMPLATE_HELPER)) {
            log.error("钉钉机器人发送消息失败， 钉钉配置或redis配置未初始化");
            return;
        }
        final String yearMonthDay = String.valueOf(DateUtils.currentYearMonthDay());
        final AccessLimitResponse response = REDIS_TEMPLATE_HELPER.stringIncrWithLimit(
                AlarmRedisKeyEnum.DING_TALK_DAILY_LIMIT.getKey(yearMonthDay), 1L,
                (long) DING_TALK_PROPERTIES.getDailyLimit(),
                AlarmRedisKeyEnum.DING_TALK_DAILY_LIMIT.getTtl().toSeconds());
        if (response.isLimited()) {
            throw new BusinessException("钉钉机器人发送消息失败， 今日发送次数已达到上限");
        }
        sendMarkdownMsg(secret, accessToken, title, content, true, new ArrayList<>());
    }


    /**
     * 使用钉钉发送markdown机器人， 并且@所有人
     *
     * @param secret 签名密钥
     * @param accessToken URL 中的 accessToken
     * @param title 标题
     * @param content 内容
     */
    public static void sendMarkdownMsgToAll(String secret, String accessToken, String title, String content) {
        sendMarkdownMsg(secret, accessToken, title, content, true, new ArrayList<>());
    }


    /**
     * 使用钉钉发送markdown机器人
     *
     * @param secret 签名密钥
     * @param accessToken URL 中的 accessToken
     * @param title 标题
     * @param content 内容
     * @param isAtAll 是否 @ 所有人
     * @param atUserIds @ 用户 ID 列表
     */
    public static void sendMarkdownMsg(String secret, String accessToken, String title, String content, boolean isAtAll,
            List<String> atUserIds) {
        try {
            Long timestamp = System.currentTimeMillis();
            String stringToSign = timestamp + "\n" + secret;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            String sign = URLEncoder.encode(new String(Base64.encodeBase64(signData)), "UTF-8");
            // sign字段和timestamp字段必须拼接到请求URL上，否则会出现 310000 的错误信息
            String url = "https://oapi.dingtalk.com/robot/send?sign=%s&timestamp=%s&access_token=%s".formatted(sign,
                    timestamp, accessToken);
            DingTalkClient client = new DefaultDingTalkClient(url);
            OapiRobotSendRequest req = buildMarkdownRequest(title, content, isAtAll, atUserIds);
            OapiRobotSendResponse rsp = client.execute(req);
            if (!rsp.isSuccess()) {
                log.error("钉钉机器人消息发送失败，errcode={}, errmsg={}, body={}", rsp.getErrcode(), rsp.getErrmsg(),
                        rsp.getBody());
            }
        } catch (Exception e) {
            log.error("钉钉机器人发送消息失败", e);
        }
    }

    /**
     * 构建markdown请求类
     *
     * @param title 标题
     * @param content 内容
     * @param isAtAll 是否 @ 所有人
     * @param atUserIds @ 用户 ID 列表
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
