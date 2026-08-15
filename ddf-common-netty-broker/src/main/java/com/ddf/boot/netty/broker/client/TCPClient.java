package com.ddf.boot.netty.broker.client;

import com.ddf.boot.netty.broker.message.RequestContent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;

/**
 * TCP 客户端
 * <p>
 * 修复说明：
 * 1. write 方法添加超时和重试机制，避免无限循环
 * 2. 正确处理 InterruptedException，恢复中断状态
 * 3. 添加连接状态检查
 * </p>
 *
 * @author dongfang.ding
 * @since 2019/7/5 11:12
 */
@Slf4j
public class TCPClient {

    /**
     * 最大重试次数
     */
    private static final int MAX_RETRY_COUNT = 10;

    /**
     * 重试间隔（毫秒）
     */
    private static final long RETRY_INTERVAL_MS = 200;

    /**
     * 总超时时间（秒）
     */
    private static final long TOTAL_TIMEOUT_SECONDS = 10;

    private String host;
    private int port;
    private volatile Channel channel;
    private ExecutorService executorService;
    private NioEventLoopGroup worker;
    private SslContext sslContext;

    public TCPClient(String host, int port, ExecutorService executorService, SslContext sslContext) {
        this.host = host;
        this.port = port;
        this.executorService = executorService;
        this.sslContext = sslContext;
    }

    public void connect() {
        executorService.execute(() -> {
            worker = new NioEventLoopGroup();
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(worker).channel(NioSocketChannel.class).option(ChannelOption.SO_KEEPALIVE, true).option(
                    ChannelOption.TCP_NODELAY, true).option(ChannelOption.SO_REUSEADDR, true).option(
                    ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
            bootstrap.remoteAddress(host, port);
            if (sslContext != null) {
                bootstrap.handler(new ClientChannelInit(sslContext));
            } else {
                bootstrap.handler(new ClientChannelInit());
            }

            ChannelFuture future;
            try {
                future = bootstrap.connect().sync();
                if (future.isSuccess()) {
                    log.info("连接到服务端成功，host={}, port={}", host, port);
                }
                channel = future.channel();
                log.info("客户端初始化完成");
                // 这里会一直与服务端保持连接，直到服务端断掉才会同步关闭自己
                channel.closeFuture().sync();
            } catch (InterruptedException e) {
                log.error("客户端连接被中断", e);
                Thread.currentThread().interrupt();
            } finally {
                log.info("客户端连接关闭");
            }
        });
    }

    /**
     * 发送消息
     * <p>
     * 修复说明：
     * 1. 限制最大重试次数，避免无限循环
     * 2. 设置总超时时间
     * 3. 正确处理 InterruptedException
     * </p>
     *
     * @param content 内容
     * @throws InterruptedException 如果线程被中断
     */
    public void write(RequestContent content) throws InterruptedException {
        AtomicInteger retryCount = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();

        while (channel == null) {
            // 检查总超时
            if (System.currentTimeMillis() - startTime > TimeUnit.SECONDS.toMillis(TOTAL_TIMEOUT_SECONDS)) {
                throw new RuntimeException("连接等待超时，无法发送消息");
            }

            // 检查重试次数
            if (retryCount.incrementAndGet() > MAX_RETRY_COUNT) {
                throw new RuntimeException("连接重试次数超过上限，无法发送消息");
            }

            try {
                Thread.sleep(RETRY_INTERVAL_MS);
            } catch (InterruptedException e) {
                // 恢复中断状态
                Thread.currentThread().interrupt();
                throw e;
            }
        }
        channel.writeAndFlush(content);
    }

    /**
     * 关闭连接
     */
    public void close() {
        log.info("客户端尝试主动关闭");
        try {
            if (channel != null) {
                channel.close();
            }
        } finally {
            if (worker != null) {
                try {
                    worker.shutdownGracefully().sync();
                } catch (InterruptedException e) {
                    log.error("关闭 worker 时被中断", e);
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * @param args 参数
     */
    public static void main(String[] args) throws JsonProcessingException, InterruptedException {
        ExecutorService executorService = Executors.newCachedThreadPool();
        ObjectMapper objectMapper = new ObjectMapper();

        TCPClient client = new TCPClient("localhost", 8888, executorService, null);
        client.connect();

        while (true) {
            Thread.sleep(2000);
            Map<String, String> contentMap = new HashMap<>();
            contentMap.put("from", "13185679963");
            contentMap.put("to", "15564325896");
            contentMap.put("timestamp", System.currentTimeMillis() + "");
            contentMap.put("content", "晚上来家吃饭晚上来家吃饭晚上来家吃饭晚");
            RequestContent<?> request = RequestContent.request(RequestContent.Cmd.ECHO.name(), contentMap);
            // 以append的方式增加扩展字段
            request.addExtra("lang", "java");
            request.addExtra("deviceId", "huawei");
            // 直接写入对象
            client.write(request);
        }
    }
}
