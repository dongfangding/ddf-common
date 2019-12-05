package com.ddf.common.websocket.helper;

import com.ddf.common.util.JsonUtil;
import com.ddf.common.util.SpringContextHolder;
import com.ddf.common.util.StringUtil;
import com.ddf.common.websocket.biz.CmdStrategy;
import com.ddf.common.websocket.enumerate.CmdEnum;
import com.ddf.common.websocket.exception.ClientMessageCodeException;
import com.ddf.common.websocket.model.ws.AuthPrincipal;
import com.ddf.common.websocket.model.ws.Message;
import com.ddf.common.websocket.model.ws.MessageResponse;
import com.ddf.common.websocket.model.ws.WebSocketSessionWrapper;
import com.ddf.common.websocket.service.ChannelTransferService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

/**
 * 处理Cmd指令响应的处理类
 * 要保证实现类添加到容器类中的beanName和指令码保持一致
 *
 * @author dongfang.ding
 * @date 2019/8/24 10:37
 */
@Slf4j
public class CmdAction implements CmdStrategy {

    private CmdStrategy cmdStrategy;

    private ChannelTransferService channelTransferService;

    public CmdAction() {

    }

    public CmdAction(CmdEnum cmdEnum) {
        CmdStrategy cmdStrategy;
        try {
            cmdStrategy = (CmdStrategy) SpringContextHolder.getBean(cmdEnum.name());
        } catch (Exception e) {
            log.warn("[{}]没有找到对应的处理类....", cmdEnum.name());
            cmdStrategy = null;
        }
        this.cmdStrategy = cmdStrategy;
        this.channelTransferService = SpringContextHolder.getBean(ChannelTransferService.class);
    }

    /**
     * 将指令下发给设备
     *
     * @param cmd
     * @param body
     * @return
     */
    @Override
    public <T> Message push(CmdEnum cmd, T body) {
        if (cmdStrategy != null) {
            return cmdStrategy.push(cmd, body);
        }
        return Message.request(cmd, body);
    }

    /**
     *
     * 响应指令码，如果没有实现自己的指令码策略，则默认不做任何业务处理，直接响应成功
     *
     * @param webSocketSessionWrapper
     * @param authPrincipal
     * @param message
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Message responseCmd(WebSocketSessionWrapper webSocketSessionWrapper, AuthPrincipal authPrincipal, Message message) {
        if (message == null || StringUtils.isBlank(message.getRequestId())) {
            return null;
        }
        boolean isSuccess = false;
        Message responseMessage = message;
        try {
            WebsocketSessionStorage.checkResponseIsSuccess(message);
            if (cmdStrategy != null) {
                responseMessage = this.cmdStrategy.responseCmd(webSocketSessionWrapper, authPrincipal, message);
            }
            isSuccess = true;
        } catch (ClientMessageCodeException e) {
            log.error("客户端响应数据错误！", e);
            channelTransferService.updateToComplete(message, false, e.getMessage(), JsonUtil.asString(message), null);
        } catch (Exception e) {
            log.error("指令码处理失败", e);
            e.printStackTrace();
            Message<String> errorMessage = Message.responseReceived(message, e.getMessage(), MessageResponse.SERVER_CODE_ERROR);
            channelTransferService.updateToComplete(message, false, StringUtil.exceptionToString(e),
                    JsonUtil.asString(message), JsonUtil.asString(errorMessage));
            WebsocketSessionStorage.putDefaultResponse(message, MessageResponse.failure(e.getMessage()));
            WebsocketSessionStorage.sendMessage(authPrincipal, errorMessage);
        }
        if (isSuccess) {
            // 如果没有给指令调用方设置响应数据，这里给一个默认值
            WebsocketSessionStorage.putDefaultResponse(message, MessageResponse.success(message.getBody()));
            // 脑瓜疼，后面再理这个记录逻辑
            String response = JsonUtil.asString(responseMessage);
            String messageStr = JsonUtil.asString(message);
            Message textMessage = !response.equals(messageStr) ? responseMessage : Message.responseSuccess(message);
            String textMessageStr = JsonUtil.asString(textMessage);
            // 服务端请求会放在request里，这个时候要记录客户端的响应；客户端如果已经是响应了，服务端响应成功就行，
            // 但是日志不需要再记录服务端响应成功的日志了，主要还是要记录关键数据
            String logResponse = Message.Type.REQUEST.equals(message.getType()) ? textMessageStr : messageStr;
            channelTransferService.updateToComplete(message, true, null, logResponse, textMessageStr);
            log.info("响应[{}]-[{}]数据: {}", authPrincipal.getIme(), authPrincipal.getToken(), textMessageStr);
            // 日志记录了一个请求的完整链，服务端发出的某些请求客户端会给予响应，服务端拿到响应后会去做做一些业务处理，
            // 服务端有没有收到这个数据，客户端并不知道，日志了记录了服务端收到数据之后会给予响应，但是在最后发送的时候
            // 这里做了一个判断，没有把这个响应返回给客户端。仁者见仁吧
            if (Message.Type.REQUEST.equals(message.getType())) {
                WebsocketSessionStorage.sendMessage(authPrincipal, textMessage);
            }
        }
        return responseMessage;
    }
}
