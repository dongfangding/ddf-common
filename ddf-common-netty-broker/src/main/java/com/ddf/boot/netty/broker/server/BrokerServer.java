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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * 代理服务器
 * <p>
 * 资源管理说明：
 * 1. 所有线程池必须在 close() 方法中优雅关闭
 * 2. 使用 shutdown() 而非 shutdownNow() 允许正在执行的任务完成
 * </p>
 *
 * @author dongfang.ding
 * @since 2020/9/20 0020 21:30
 */
@Slf4j
public class BrokerServer {

    /**
     * 默认工作线程数
     */
    private static final int WORKER_GROUP_SIZE = Runtime.getRuntime().availableProcessors() * 2;

    /**
     * 配置属性类
     */
    private final BrokerProperties brokerProperties;

    private EventLoopGroup boss;
    private EventLoopGroup worker;
    private ScheduledExecutorService syncExecutor;
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
                serverBootstrap.childHandler(
                        new ServerChannelInit(brokerProperties, KeyManagerFactoryHelper.defaultServerContext()));
            } else {
                serverBootstrap.childHandler(new ServerChannelInit(brokerProperties));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize SSL context", e);
        }
        ChannelFuture future;
        try {
            log.info("服务端启动中.....");
            future = serverBootstrap.bind(brokerProperties.getPort()).sync();
            if (future.isSuccess()) {
                log.info("服务端启动成功，端口: {}", brokerProperties.getPort());
            }
            // 启动同步任务线程池（必须保存引用以便关闭）
            syncExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "netty-broker-sync-task");
                t.setDaemon(true);
                return t;
            });
            syncExecutor.scheduleAtFixedRate(
                    new ChannelStoreSyncTask(), 10, 10, TimeUnit.SECONDS);
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("启动服务端失败", e);
            Thread.currentThread().interrupt();
        }
    }


    /**
     * 关闭服务端
     * <p>
     * 优雅关闭策略：
     * 1. 先关闭同步任务线程池
     * 2. 再关闭 Netty EventLoopGroup
     * </p>
     */
    public void close() {
        log.info("正在关闭 Netty Broker Server...");

        // 1. 关闭同步任务线程池
        if (syncExecutor != null && !syncExecutor.isShutdown()) {
            syncExecutor.shutdown();
            try {
                if (!syncExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                    syncExecutor.shutdownNow();
                    log.warn("同步任务线程池未能在30秒内优雅关闭，已强制关闭");
                }
            } catch (InterruptedException e) {
                syncExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            log.info("同步任务线程池已关闭");
        }

        // 2. 关闭 Netty EventLoopGroup
        try {
            if (boss != null) {
                boss.shutdownGracefully().sync();
            }
            if (worker != null) {
                worker.shutdownGracefully().sync();
            }
            log.info("Netty EventLoopGroup 已关闭");
        } catch (InterruptedException e) {
            log.error("关闭 Netty EventLoopGroup 时被中断", e);
            Thread.currentThread().interrupt();
        }
    }
}
