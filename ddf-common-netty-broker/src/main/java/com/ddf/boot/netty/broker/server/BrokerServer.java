package com.ddf.boot.netty.broker.server;

import com.ddf.boot.netty.broker.server.properties.BrokerProperties;
import com.ddf.boot.netty.broker.ssl.KeyManagerFactoryHelper;
import com.ddf.boot.netty.broker.storage.ChannelStoreSyncTask;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 代理服务器$
 *
 * @author dongfang.ding
 * @date 2020/9/20 0020 21:30
 */
@Slf4j
public class BrokerServer {

    /**
     * 默认工作线程
     */
    private static final int WORKER_GROUP_SIZE = Runtime.getRuntime().availableProcessors() * 2;

    /**
     * 配置属性类
     */
    private final BrokerProperties brokerProperties;

    private EventLoopGroup boss;
    private EventLoopGroup worker;

    public BrokerServer(BrokerProperties brokerProperties) {
        this.brokerProperties = brokerProperties;
    }

    /**
     * 启动服务端
     */
    public void start() {
        boss = new NioEventLoopGroup();
        worker = new NioEventLoopGroup(WORKER_GROUP_SIZE);
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(boss, worker);
        serverBootstrap.channel(NioServerSocketChannel.class);
        serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.ALLOCATOR, new PooledByteBufAllocator(false))
                .childOption(ChannelOption.SO_RCVBUF, brokerProperties.getSoRecBuf())
                .childOption(ChannelOption.SO_SNDBUF, brokerProperties.getSoSndBuf());
        try {
            if (brokerProperties.isSsl()) {
                serverBootstrap.childHandler(new ServerChannelInit(brokerProperties, KeyManagerFactoryHelper.defaultServerContext()));
            } else {
                serverBootstrap.childHandler(new ServerChannelInit(brokerProperties));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ChannelFuture future;
        try {
            System.out.println("服务端启动中.....");
            future = serverBootstrap.bind(brokerProperties.getPort()).sync();
            if (future.isSuccess()) {
                System.out.println("服务端启动成功....");
            }
            // todo 处理同步任务
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new ChannelStoreSyncTask(), 10, 10, TimeUnit.SECONDS);
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("启动服务端失败", e);
        }
    }


    /**
     * 关闭服务端
     */
    public void close() {
        try {
            boss.shutdownGracefully().sync();
            worker.shutdownGracefully().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
