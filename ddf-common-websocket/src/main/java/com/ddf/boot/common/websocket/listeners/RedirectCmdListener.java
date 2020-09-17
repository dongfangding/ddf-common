package com.ddf.boot.common.websocket.listeners;

import com.ddf.boot.common.core.util.JsonUtil;
import com.ddf.boot.common.websocket.constant.WebsocketConst;
import com.ddf.boot.common.websocket.helper.WebsocketSessionStorage;
import com.ddf.boot.common.websocket.model.MessageRequest;
import com.ddf.boot.common.websocket.model.MessageResponse;
import com.ddf.boot.common.websocket.service.WsMessageService;
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

    @Autowired
    private WsMessageService wsMessageService;

    /**
     *
     *
     * 由于websocket的session不支持外部存储，所以都存储在连接的那台机器上；
     *
     * 当发现目标在本地不存在，但是在表中存在且在线时，就说明连接到了另外一台机器上，收到请求的机器就会进行请求转发，然后获取消息
     * 返还给原始请求
     *
     * 当然也可以采用nginx代理， 用nginx将一致性hash将固定请求直接打到固定的机器上
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
            wsMessageService.executeCmd(JsonUtil.toBean(message.getBody(), MessageRequest.class));
        } else if (WebsocketConst.RETURN_MESSAGE_TOPIC.equals(channel)) {
            log.info("收到转发后的指令回传数据, {}", new String(message.getBody(), StandardCharsets.UTF_8));
            MessageResponse<?> messageResponse = JsonUtil.toBean(message.getBody(), MessageResponse.class);
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
