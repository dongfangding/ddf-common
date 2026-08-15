package com.ddf.common.ons.console.util;

import com.ddf.common.ons.enume.MessageModel;
import java.net.InetAddress;
import java.util.Objects;
import java.util.StringJoiner;
import lombok.SneakyThrows;

/**
 * <p>相关工具类</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2021/05/18 13:53
 */
public class OnsConsoleUtil {

    private OnsConsoleUtil() {

    }

    /**
     * 获取本机地址
     */
    @SneakyThrows
    public static String getLocalHost() {
        return InetAddress.getLocalHost().getHostAddress();
    }


    /**
     * 根据已有属性生成重试表达式, 为了避免广播模式附加主机后tag不同造成订阅关系可能混乱的问题，暂不支持广播模式
     *
     * @param topic 主题参数
     * @param listenerClassName listener类型名称参数
     * @param groupId 分组ID
     * @param messageModel 消息model参数
     */
    public static String getRetryExpression(String topic, String listenerClassName, String groupId,
            String messageModel) {
        final StringJoiner joiner = new StringJoiner("-");
        joiner.add(topic).add(listenerClassName).add(groupId);
        // 如果是广播的话，需要知道失败的那台机器, 成功的机器不需要再次处理
        if (Objects.equals(MessageModel.BROADCASTING.getModel(), messageModel)) {
            joiner.add(getLocalHost());
        }
        return joiner.toString();
    }

    /**
     * 以指定格式分隔后进行固定格式拼接
     *
     * @param str STR参数
     * @param separator 分隔符
     */
    public static String getShortNameBySplit(String str, String separator) {
        return getShortNameBySplit(str, separator, separator);
    }

    /**
     * 以指定格式分隔后进行固定格式拼接
     *
     * @param str STR参数
     * @param separator 分隔符
     * @param replaceSeparator 参数
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
