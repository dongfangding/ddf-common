package com.ddf.boot.netty.broker.client;

import com.ddf.boot.netty.broker.codec.RequestContentCodec;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import javax.net.ssl.SSLEngine;

/**
 * TCP服务端Channel初始化
 *
 * @author dongfang.ding
 * @date 2019/7/5 10:49
 */
public class ClientChannelInit extends ChannelInitializer<Channel> {

    private final SslContext context;

    public ClientChannelInit(SslContext context) {
        this.context = context;
    }

    public ClientChannelInit() {
        context = null;
    }

    @Override
    protected void initChannel(Channel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        if (pipeline != null) {
            if (context != null) {
                SSLEngine engine = context.newEngine(ch.alloc());
                engine.setUseClientMode(true);
                ch.pipeline().addFirst("ssl", new SslHandler(engine));
            }

            pipeline.addLast(new LineBasedFrameDecoder(1024))
                    // 指定字符串编解码器，客户端直接写入字符串，不需要使用ByteBuf,这种写法是如果客户端没有服务端源码，或不想写编码器
                    /*.addLast(new StringEncoder(CharsetUtil.UTF_8))
                    .addLast(new StringDecoder(CharsetUtil.UTF_8))*/.addLast(new RequestContentCodec()).addLast(
                    new ClientInboundHandler()).addLast(new ClientOutboundHandler());
        }
    }
}
