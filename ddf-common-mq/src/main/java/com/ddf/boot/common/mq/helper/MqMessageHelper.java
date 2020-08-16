package com.ddf.boot.common.mq.helper;

import com.ddf.boot.common.mq.definition.MqMessageWrapper;
import com.ddf.boot.common.mq.interfaces.MqAuditorAware;
import com.ddf.boot.common.core.util.IdsUtil;
import com.ddf.boot.common.core.util.JsonUtil;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;

/**
 * mq发送消息格式类的工具类
 *
 * _ooOoo_
 * o8888888o
 * 88" . "88
 * (| -_- |)
 * O\ = /O
 * ___/`---'\____
 * .   ' \\| |// `.
 * / \\||| : |||// \
 * / _||||| -:- |||||- \
 * | | \\\ - /// | |
 * | \_| ''\---/'' | |
 * \ .-\__ `-` ___/-. /
 * ___`. .' /--.--\ `. . __
 * ."" '< `.___\_<|>_/___.' >'"".
 * | | : `- \`.;`\ _ /`;.`/ - ` : | |
 * \ \ `-. \_ __\ /__ _/ .-` / /
 * ======`-.____`-.___\_____/___.-`____.-'======
 * `=---='
 * .............................................
 * 佛曰：bug泛滥，我已瘫痪！
 *
 * @author dongfang.ding
 * @date 2019/8/1 16:41
 */
@Component
public class MqMessageHelper {

    @Autowired(required = false)
    private MqAuditorAware mqAuditorAware;

    /**
     * 封装发送消息的统一类
     *
     * @param body
     * @param <T>
     * @return
     */
    public <T> MqMessageWrapper<T> wrapper(T body) {
        MqMessageWrapper<T> message = new MqMessageWrapper<>();
        // todo 消息创建人
        message.setCreator(getCurrentAuditor());
        message.setCreateTime(System.currentTimeMillis());
        message.setBody(body);
        message.setMessageId(IdsUtil.getNextStrId());
        return message;
    }

    /**
     * 封装发送消息的统一类
     *
     * @param bodyObj
     * @param <T>
     * @return
     */
    public <T> String wrapperToString(T bodyObj) {
        return JsonUtil.asString(wrapper(bodyObj));
    }

    /**
     * 将序列化后发送的消息反序列化为{@link MqMessageWrapper}，
     *
     * @param messageStr 序列化后的消息json字符串
     * @param clazz      消息中的body的对象类型
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> MqMessageWrapper<T> parse(@NotNull String messageStr, @NotNull Class<T> clazz) {
        MqMessageWrapper<T> message = JsonUtil.toBean(messageStr, MqMessageWrapper.class);
        T body = message.getBody();
        if (body == null) {
            return message;
        }
        message.setBody(JsonUtil.toBean(JsonUtil.asString(message.getBody()), clazz));
        return message;
    }


    /**
     * 将消息中的byte数组转换为统一对象
     * @param message
     * @param clazz
     * @return com.ddf.boot.common.mq.definition.MqMessageWrapper<T>
     * @author dongfang.ding
     * @date 2019/12/10 0010 22:58
     **/
    public <T> MqMessageWrapper<T> parse(@NotNull Message message, @NotNull Class<T> clazz) {
        if (message == null || clazz == null) return null;
        return parse(getBodyAsString(message.getBody()), clazz);
    }

    /**
     * 将消息体中的字节数组转换为包装对象，但是不关注包装对象中包含的业务数据
     * @param message
     * @return com.ddf.boot.common.mq.definition.MqMessageWrapper
     * @author dongfang.ding
     * @date 2019/12/20 0020 11:35
     **/
    public MqMessageWrapper<?> parseNoBody(@NotNull Message message) {
        return JsonUtil.toBean(getBodyAsString(message.getBody()), MqMessageWrapper.class);
    }

    /**
     * 将消费端的消息body转换为string
     *
     * @return
     * @see Message#getBody()
     */
    public String getBodyAsString(byte[] body) {
        return new String(body, StandardCharsets.UTF_8);
    }


    /**
     * 获取当前操作人
     *
     * @return java.lang.String
     * @author dongfang.ding
     * @date 2019/12/19 0019 18:18
     **/
    public String getCurrentAuditor() {
        String defaultName = "ddf-common-mq";
        return mqAuditorAware == null ? defaultName : mqAuditorAware.getAuditor().orElse(defaultName);
    }
}
