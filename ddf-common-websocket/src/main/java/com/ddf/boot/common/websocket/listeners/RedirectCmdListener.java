package com.ddf.boot.common.websocket.listeners;

import com.ddf.boot.common.websocket.constant.WebsocketConst;
import com.ddf.boot.common.websocket.dubbo.MessageWsDubboService;
import com.ddf.boot.common.websocket.helper.WebsocketSessionStorage;
import com.ddf.boot.common.util.JsonUtil;
import com.ddf.boot.common.websocket.model.ws.MessageRequest;
import com.ddf.boot.common.websocket.model.ws.MessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * cmd指令转发订阅
 *
 * @author dongfang.ding
 * @date 2019/8/27 15:18
 */
@Component
@Slf4j
public class RedirectCmdListener extends MessageListenerAdapter {

    @Autowired(required = false)
    private MessageWsDubboService messageWsDubboService;

    /**
     * Standard Redis {@link MessageListener} entry point.
     * <p>
     * Delegates the message to the target listener method, with appropriate conversion of the message argument. In case
     * of an exception, the {@link #handleListenerException(Throwable)} method will be invoked.
     *
     * @param message the incoming Redis message
     * @param pattern
     * @see #handleListenerException
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        log.info("====================指令转发收到消息: {}==================", new String(message.getBody(), StandardCharsets.UTF_8));
        String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
        if (WebsocketConst.REDIRECT_CMD_TOPIC.equals(channel)) {
            messageWsDubboService.executeCmd(JsonUtil.toBean(message.getBody(), MessageRequest.class));
        } else if (WebsocketConst.RETURN_MESSAGE_TOPIC.equals(channel)) {
            log.info("收到转发后的指令回传数据, {}", new String(message.getBody(), StandardCharsets.UTF_8));
            MessageResponse messageResponse = JsonUtil.toBean(message.getBody(), MessageResponse.class);
            if (messageResponse == null) {
                log.error("返回数据为空，不能处理....");
                return;
            }
            String requestId = messageResponse.getRequestId();
            if (StringUtils.isNotBlank(requestId) && WebsocketSessionStorage.getRequestConnectResponseMap().containsKey(requestId)) {
                log.info("指令回传数据本机能够处理，填充响应....");
                WebsocketSessionStorage.putResponse(requestId, messageResponse);
            } else {
                log.info("指定回传数据本机不能处理！");
            }
        }
    }
}
