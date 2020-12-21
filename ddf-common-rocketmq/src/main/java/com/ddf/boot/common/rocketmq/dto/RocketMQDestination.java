package com.ddf.boot.common.rocketmq.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>遵循最佳实践的消息目的地</p >
 * <p>
 * 使用topic:tags筛选的方式， 来减少topic定义的数量
 * <p>
 * 参考下述方法对destination的解释，也是能够这么玩的前提； 消息的目的地可以由topic:tags的方式组成
 * org.apache.rocketmq.spring.core.RocketMQTemplate#syncSend(java.lang.String, org.springframework.messaging.Message)
 *
 * @author Snowball
 * @version 1.0
 * @date 2020/11/20 11:36
 */
@Builder
public class RocketMQDestination {

    /**
     * topic:tag
     */
    private static final String SPLIT = ":";


    @Getter
    private final String topic;

    @Setter
    private final String tags;


    public static RocketMQDestination instanceOfDestination(String destinationStr) {
        if (StringUtils.isEmpty(destinationStr) || destinationStr.indexOf(SPLIT) < 1 || destinationStr.indexOf(SPLIT)
                != destinationStr.lastIndexOf(SPLIT)) {
            return null;
        }
        String[] arrays = destinationStr.split(SPLIT);
        return RocketMQDestination.builder().topic(arrays[0]).tags(arrays[1]).build();
    }


    /**
     * 返回
     *
     * @return
     */
    public String toDestination() {
        if (StringUtils.isNotEmpty(tags)) {
            return topic + SPLIT + tags;
        }
        return topic;
    }
}

