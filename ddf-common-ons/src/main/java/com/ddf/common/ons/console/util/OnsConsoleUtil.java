package com.ddf.common.ons.console.util;

import com.ddf.common.ons.enume.MessageModel;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import lombok.SneakyThrows;
import org.springframework.util.CollectionUtils;

/**
 * <p>相关工具类</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/05/18 13:53
 */
public class OnsConsoleUtil {

    private OnsConsoleUtil() {

    }

    /**
     * 获取本机地址
     *
     * @return
     */
    @SneakyThrows
    public static String getLocalHost() {
        return InetAddress.getLocalHost().getHostAddress();
    }


    /**
     * 根据已有属性生成重试表达式, 为了避免广播模式附加主机后tag不同造成订阅关系可能混乱的问题，暂不支持广播模式
     *
     * @param topic
     * @param listenerClassName
     * @param groupId
     * @param messageModel
     * @return
     */
    public static String getRetryExpression(String topic, String listenerClassName, String groupId,
            String messageModel) {
        final StringJoiner joiner = new StringJoiner("-");
        joiner.add(topic)
                .add(listenerClassName)
                .add(groupId);
        // 如果是广播的话，需要知道失败的那台机器, 成功的机器不需要再次处理
        if (Objects.equals(MessageModel.BROADCASTING.getModel(), messageModel)) {
            joiner.add(getLocalHost());
        }
        return joiner.toString();
    }

    /**
     * 动态通过反射修改指定注解实例里的属性的值， 这个是如果只有一个属性要修改时提供的简便方法
     *
     * @param annotation
     * @param name
     * @param value
     */
    @SneakyThrows
    public static void modifyAnnotationValue(Annotation annotation, String name, Object value) {
        Map<String, Object> valueMap = new HashMap<>(2);
        valueMap.put(name, value);
        modifyAnnotationValue(annotation, valueMap);
    }


    /**
     * 动态通过反射修改指定注解示例里的属性的值
     *
     * @param annotation   注解实例对象
     * @param nameValueMap 属性和值集合
     */
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static void modifyAnnotationValue(Annotation annotation, Map<String, Object> nameValueMap) {
        if (CollectionUtils.isEmpty(nameValueMap)) {
            return;
        }
        final InvocationHandler handler = Proxy.getInvocationHandler(annotation);
        // memberValues是注解代理类存储属性的固定属性值， 是个LinkedHashMap
        Field hField = handler.getClass().getDeclaredField("memberValues");
        hField.setAccessible(true);
        Map memberValues = (Map) hField.get(handler);
        nameValueMap.forEach(memberValues::put);
    }

    /**
     * 以指定格式分隔后进行固定格式拼接
     *
     * @param str
     * @param separator
     * @return
     */
    public static String getShortNameBySplit(String str, String separator) {
        return getShortNameBySplit(str, separator, separator);
    }

    /**
     * 以指定格式分隔后进行固定格式拼接
     *
     * @param str
     * @param separator
     * @return
     */
    public static String getShortNameBySplit(String str, String separator, String replaceSeparator) {
        final String[] charArray = str.split(separator);
        final int size = charArray.length;
        if (size == 1) {
            return str;
        }
        final StringBuilder result = new StringBuilder();
        result.append(charArray[0].charAt(0));
        for (int i = 1; i < size; i++) {
            result.append(replaceSeparator).append(charArray[i].charAt(0));
        }
        return result.toString();
    }
}
