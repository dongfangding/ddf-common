package com.ddf.common.boot.mqtt.model.support.body;

import com.ddf.common.boot.mqtt.model.request.MqttMessageRequest;
import java.io.Serializable;

/**
 * <p>一个标识类， 只是标识这个类具有在mqtt message body中传递的资格 </p >
 *
 *
 * messageType的作用是body由于是接口，序列化的时候不知道该序列化成哪个对象， 所以需要下面的写法指定通过messageType里的这个字段
 * 的值决定序列化成哪个对象（试验一下），如果不行，宁愿用字符串
 * @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "messageType", visible = true)
 * @JsonSubTypes({ @JsonSubTypes.Type(value = ImMessageBodyCustom.class, name = ImMessageTypeConstants.CUSTOM),
 *
 * topic定义成各种对象的目的是不需要应用方关注具体的topic的路径，而是让使用方知道topic的类型，然后通过topic的类型
 * 对应的参数组装成topic， 这样使用的时候传递的不是具体的topic，而是topic类型和对应类型所需要的组成参数
 *
 * 还需要知道sessionType和formatType的作用
 *
 *
 *
 * {@link MqttMessageRequest#getBody()}
 * 所有要放入body中的数据必须通过这个接口标记，方便追溯或者一些通用处理功能
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/20 13:11
 */
public interface MessageBody extends Serializable {

    /**
     * 消息内容
     * @return
     */
    String getMsg();

    /**
     * 消息格式类型， 如文本、图片、视频等各种样式，通过这个字段来确定渲染方式等，由调用方自助决定，这里只是预留说明可以这么处理
     *
     * @return
     */
    String getContentType();

    /**
     * 消息标题
     * 如果消息本身很长，用来处理一些消息标题相关功能
     *
     * @return
     */
    default String getMsgTitle() {
        return getMsg();
    }
}
