package com.ddf.common.mq.util;

import com.ddf.common.mq.definition.MqMessageBO;
import com.ddf.common.util.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.util.Assert;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * mq发送消息格式类的工具类
 *
 * @author dongfang.ding
 * @date 2019/8/1 16:41
 */
public class MqMessageUtil {

    /**
     * 封装发送消息的统一类
     *
     * @param queueName
     * @param bodyObj
     * @param <T>
     * @return
     */
    public static <T> MqMessageBO<T> wrapper(String queueName, T bodyObj) {
        Assert.notNull(queueName, "queueName is not allowed to be null");
        MqMessageBO<T> message = new MqMessageBO<>();
        // todo 消息创建人
        message.setCreator(0L);
        message.setCreateTime(System.currentTimeMillis());
        message.setQueueName(queueName);
        message.setBodyObj(bodyObj);
        message.setBody(JsonUtil.asString(bodyObj));
        message.setMessageId(UUID.randomUUID().toString());
        return message;
    }

    /**
     * 封装发送消息的统一类
     *
     * @param queueName
     * @param bodyObj
     * @param <T>
     * @return
     */
    public static <T> String wrapperToString(String queueName, T bodyObj) {
        return JsonUtil.asString(wrapper(queueName, bodyObj));
    }

    /**
     * 将序列化后发送的消息反序列化为{@link MqMessageBO}，
     *
     * @param messageStr 序列化后的消息json字符串
     * @param clazz      消息中的body的对象类型
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> MqMessageBO<T> parse(String messageStr, Class<T> clazz) {
        MqMessageBO<T> message = JsonUtil.toBean(messageStr, MqMessageBO.class);
        String body = message.getBody();
        if (StringUtils.isAnyBlank(body)) {
            return message;
        }
        T t = JsonUtil.toBean(message.getBody(), clazz);
        message.setBodyObj(t);
        return message;
    }

    /**
     * 将消费端的消息body转换为string
     *
     * @return
     * @see Message#getBody()
     */
    public static String getBodyAsString(byte[] body) {
        return new String(body, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("xixi", "haha");
        MqMessageBO<Map> hehe = MqMessageUtil.wrapper("hehe", map);

        String str = JsonUtil.asString(hehe);
        System.out.println(str);

        MqMessageBO<Map> parse = MqMessageUtil.parse(str, Map.class);
        System.out.println(parse.getBody());
        System.out.println(parse.getQueueName());
        System.out.println(parse.getBodyObj());

        System.out.println(parse.getBodyObj().get("xixi"));


    }
}
