package com.ddf.common.boot.mqtt.model.support.topic;

import com.ddf.common.boot.mqtt.support.GlobalStorage;

/**
 * <p>抽象的通用群组topic类，主要为了定义通用前缀， 其它点对点topic可以继承该类，往下追加区分业务前缀</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/10/28 17:15
 */
public abstract class AbstractGroupMqttTopic implements MqttTopicDefine {

    /**
     * 获取身份id
     *
     * @return
     */
    @Override
    public abstract String getIdentityId();

    /**
     * 获取定义的区分业务的topic前缀, 这个会被附加到通用前缀后面
     *
     * @return
     */
    public abstract String getBizTopicPrefix();

    /**
     * 按照规则通过完整的topic路径反解析成对应的topic对象
     *
     * @param fullTopic
     * @return
     */
    @Override
    public abstract MqttTopicDefine convertTopicObj(String fullTopic);

    /**
     * 获取包装的BizTopicPrefix， 主要是为了处理前缀问题
     *
     * @return
     */
    public String getBoxedBizTopicPrefix() {
        return getBizTopicPrefix().startsWith(GlobalStorage.TOPIC_SEPARATOR) ? getBizTopicPrefix() : GlobalStorage.TOPIC_SEPARATOR + getBoxedBizTopicPrefix();
    }

    /**
     * 获取全局通用topic前缀
     *
     * @return
     */
    public String getTopicPrefix() {
        return String.join(
                GlobalStorage.TOPIC_SEPARATOR,
                GlobalStorage.getSystemClientIdPrefix(), GlobalStorage.GROUP_TOPIC_PREFIX,
                getBoxedBizTopicPrefix()
        );
    }

    /**
     * 获取完整的topic
     *
     * @return
     */
    @Override
    public String getFullTopic() {
        return String.join(GlobalStorage.TOPIC_SEPARATOR, getTopicPrefix(), getIdentityId());
    }
}
