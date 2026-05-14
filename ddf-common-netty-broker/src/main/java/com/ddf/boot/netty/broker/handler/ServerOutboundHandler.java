package com.ddf.boot.netty.broker.handler;


import com.ddf.boot.netty.broker.message.RequestContent;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import lombok.extern.slf4j.Slf4j;

/**
 * @author dongfang.ding
 * @since 2019/7/5 15:16
 */
@ChannelHandler.Sharable
@Slf4j
public class ServerOutboundHandler extends ChannelOutboundHandlerAdapter {
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        log.info("向客户端[{}]发送数据: {}", ctx.channel().remoteAddress(), ((RequestContent<?>) msg).serial());
        ctx.writeAndFlush(msg);
    }

    /**
     * @param ctx 参数
     * @param cause 参数
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("连接出现异常", cause);
        ctx.close();
    }
}
