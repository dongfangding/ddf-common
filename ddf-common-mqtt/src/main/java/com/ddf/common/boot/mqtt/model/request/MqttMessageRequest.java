package com.ddf.common.boot.mqtt.model.request;

import com.ddf.common.boot.mqtt.model.support.MqttMessageControl;
import com.ddf.common.boot.mqtt.model.support.body.MessageBody;
import com.ddf.common.boot.mqtt.model.support.header.MqttHeader;
import com.ddf.common.boot.mqtt.model.support.topic.MqttTopicDefine;
import java.io.Serializable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * <p>发送mqtt消息请求类</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/18 20:25
 */
@Data
public class MqttMessageRequest<T extends MessageBody> implements Serializable {

    /**
     * 基础请求头， 当然由于预留了扩展字段， 应该没有必要继承这个类继续扩展了，请使用扩展字段来存储自定义的字段
     */
    private MqttHeader header = MqttHeader.DEFAULT;

    /**
     * 消息类型，比如文本、图片、视频等，用于一些消息的渲染处理， 此值留空，由使用方决定用处
     */
    private String contentType;

    /**
     * 反序列化类型，这个作为预留字段，调用方决定如何使用。整个请求对象在存储时是作为整个请求大对象的而持久化的，
     * 那么下面消息体的body字段持久化的时候就是消息对象的json序列化字符串。而取出来消息的要使用的时候是需要反序列化回来的，
     * 可以根据这个字段来判断来决定如何序列化，当然下面还有一个message_code字段是业务类型，根据场景决定可能也是可以使用的，
     * 使用方自己决定即可，这里只是预留字段
     *
     * @return
     */
    private String deserializeType;

    /**
     * 控制mqtt消息行为参数
     */
    private MqttMessageControl control = MqttMessageControl.DEFAULT;

    /**
     * 接收端topic
     */
    @NotNull(message = "topic不能为空")
    private MqttTopicDefine topic;

    /**
     * 消息代码，  用来标识这个消息具体是做什么用的， 这个字段能够让一个topic服务更多的业务
     * 比如是用户上线通知、用户新消息通知，是一个消息的最小表示单位
     */
    @NotBlank(message = "messageCode不能为空")
    @Size(min = 1, max = 128, message = "messageCode参数过长")
    private String messageCode;

    /**
     * 消息业务类型，这个类型大于消息代码， 标识某个业务下的消息，一个业务类型下面可以有很多消息类型， 这个字段是方便消息归类，
     * 比如在一个群聊里，大家可以发文本消息、红包消息、图片消息，这是messageCode可以决定的东西，这样客户端才能做不同的渲染处理。
     *
     * 但是这些消息又都需要在群聊室里的历史消息里体现，那么有这个字段的话就可以归类，通过这个字段把不同的细粒度的消息统一查询出来
     *
     * 归根到底这个字段也是为了让topic能服务更多的业务， topic决定了是哪一类的topic, 比如现在是建立了一个家族群， 家族群是一个topic,
     * 然后有很多的家族群， 每个家族群的topic就是家族群的topic前缀规则 + 家族群id(一般而言， 能唯一区别就行)。
     * 这里也解释一下topic的粒度， 那么我现在还有一个其它业务的群聊，能不能复用这个topic呢，理论上是可以的，如果都是topic前缀规则 + id的这种形式，
     * 那么不同业务最终组成的topic可能重复。所以可以再建立一个topic或者是topic不区分，但是需要为每条记录附加一个全局不重复的唯一值，
     * 使用topic前缀 + 这个全局唯一值也是可以的。
     *
     * 继续上面的说法, 现在为能够在家族群聊天记录展示的消息都定义一个统一的bizType， 比如为chat_history,
     * 那么发文本消息则是bizType=chat_history, message_code=text
     * 发图片则是bizType=chat_history, message_code=picture
     * 发红包则是bizType=chat_history, message_code=red_pack
     *
     * 客户端根据bizType=chat_history就知道这些消息是放到聊天框里的，但是不同的消息渲染方式不一样，就是靠message_code。
     * 而服务端返回聊天文本的历史消息则是使用bizType=chat_history过滤
     * 这就是这几个字段实际使用的含义
     *
     * 接着聊天记录说一个东西， 即如果要实现聊天记录的查询有几个很尴尬的事情需要处理
     * 1. 发送消息时一般发送方为用户，客户端处理的时候需要处理发送方的头像、昵称等信息，甚至一些身份信息，而且有些字段还应该是以历史数据为准，
     *      那么这部分消息就必然要持久化。但是通用方法在做这部分的逻辑时只能直接序列化扩展字段， 这样就需要外部来自己反序列化解析了。
     * 2. 与1的问题相似， 除了通用字段，聊天的核心信息由于内容不同，对象属性也不同，所以通用模块方法里如果做历史数据保存的功能只能整体序列化整个
     *   body对象， 那么也得需要外部进行反序列化。然后取一页数据，每条都可能面临不同的对象反序列化规则，还得根据message_code来进行不同对象的反序列化，
     *   实在是有些麻烦
     *
     * 3. 业务聊天记录有要显示的需求的是否业务方自己也保存一份， 通用模块保存的只是站在消息持久化的视角上。业务方如果也保存的话，就能设计出一张冗余表，
     *      字段与业务一一对应，不同的message_code不同的字段有值， 这样字段虽然是冗余的，但是取数据的时候，省去了繁琐的反序列化问题，但是保存的时候
     *      会有点麻烦，而且还需要保存一份数据，难以抉择。
     *
     */
    @Size(min = 1, max = 128, message = "messageCode参数过长")
    private String bizType;

    /**
     * 消息body
     */
    private T body;

}
