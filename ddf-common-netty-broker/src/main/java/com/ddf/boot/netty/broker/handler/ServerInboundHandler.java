package com.ddf.boot.netty.broker.handler;

import com.ddf.boot.netty.broker.message.RequestContent;
import com.ddf.boot.netty.broker.storage.ChannelInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

/**
 * @author dongfang.ding
 * @date 2019/7/5 15:52
 */
@ChannelHandler.Sharable
@Slf4j
public class ServerInboundHandler extends SimpleChannelInboundHandler<RequestContent<?>> {

    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public static Map<String, ChannelInfo> channelStore = new ConcurrentHashMap<>();

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        log.debug("客户端[{}]注册成功>>>>>", ctx.channel().remoteAddress());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.debug("客户端[{}]在线>>>>>", ctx.channel().remoteAddress());
        channels.add(ctx.channel());
        channelStore.put(ctx.channel().remoteAddress().toString(), ChannelInfo.registry(ctx.channel()));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.debug("客户端[{}]掉线>>>>>", ctx.channel().remoteAddress());
        channels.remove(ctx.channel());
        ChannelInfo.inactive(ctx.channel());
    }


    /**
     * 如果是聊天室的功能，其实就是服务端收到消息之后，然后再由服务端向所有连接的客户端转发这个消息而已
     *
     * @param ctx
     * @param msg
     * @throws JsonProcessingException
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RequestContent<?> msg) throws JsonProcessingException {
        putMessage(ctx.channel(), msg);
        log.debug("接收到客户端[{}]发送的数据: {}", ctx.channel().remoteAddress(), msg.serial());
        ctx.writeAndFlush(RequestContent.responseAccept(msg));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("连接出现异常>>>>>", cause);
        log.debug("客户端[{}]出现异常，关闭连接>>>>>>", ctx.channel());
        channels.remove(ctx.channel());
        ChannelInfo.inactive(ctx.channel());
        ctx.close();
    }

    /**
     * 将消息放入对应的客户端的消息队列中
     *
     * @param channel
     * @param requestContent
     */
    private void putMessage(Channel channel, RequestContent<?> requestContent) {
        String key = channel.remoteAddress().toString();
        ChannelInfo channelInfo = ServerInboundHandler.channelStore.get(key);
        // 可能永远也不会出现这种情况
        if (channelInfo == null) {
            channelInfo = ChannelInfo.active(channel);
            channelInfo.getQueue().add(requestContent);
            ServerInboundHandler.channelStore.put(key, channelInfo);
        }
        channelInfo.getQueue().add(requestContent);
    }
}
