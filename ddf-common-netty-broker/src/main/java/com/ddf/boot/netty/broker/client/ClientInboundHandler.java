package com.ddf.boot.netty.broker.client;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author dongfang.ding
 * @date 2019/7/5 11:03
 */
@ChannelHandler.Sharable
@Slf4j
public class ClientInboundHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.debug("连接到服务器成功>>>>>>>>>>>>>");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.debug("与服务器连接断开>>>>>>>>>>>>>");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        log.debug("读取到服务器的发送信息>>>>>>>>>>>>>: {}", msg.toString());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("连接异常", cause);
        ctx.close();
    }
}
