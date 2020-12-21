package com.ddf.boot.netty.broker.storage;

import com.ddf.boot.netty.broker.handler.ServerInboundHandler;
import com.ddf.boot.netty.broker.message.RequestContent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 连接信息同步任务类
 *
 * @author dongfang.ding
 * @date 2019/7/8 10:12
 */
public class ChannelStoreSyncTask implements Runnable {

    private ExecutorService executorService;

    public ChannelStoreSyncTask(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public ChannelStoreSyncTask() {
        this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    }

    /**
     * 这一块如果是与数据库同步的话，下面处理没有这么复杂，就简单很多了,连接状态和消息需要分开处理
     * 该方法必须要有先后顺序，不能后面的状态先处理再处理前面的数据状态，可能会导致覆盖
     */
    @Override
    public synchronized void run() {
        // 防止处理的时候数据又有变化，每个任务只处理当前已有处理
        ConcurrentHashMap<String, ChannelInfo> channelStore = new ConcurrentHashMap<>(
                ServerInboundHandler.channelStore);
        if (channelStore != null && !channelStore.isEmpty()) {
            for (Map.Entry<String, ChannelInfo> entry : channelStore.entrySet()) {
                executorService.execute(() -> {
                    try {
                        String k = entry.getKey();
                        ChannelInfo v = entry.getValue();
                        BlockingQueue<RequestContent> queue = v.getQueue();
                        String fileName;
                        if (queue.peek() != null) {
                            fileName = k.replace(":", "_") + "_接收内容.txt";
                            RandomAccessFile file = new RandomAccessFile(
                                    System.getProperty("user.dir") + "/src/main/resources/" + fileName, "rw");
                            ObjectMapper objectMapper = new ObjectMapper();
                            while (queue.peek() != null) {
                                RequestContent content = queue.poll();
                                // FIXME 与数据库同步的时候需要数据库写入成功，再返回
                                v.getChannel().writeAndFlush(RequestContent.responseSuccess(content, null));
                                file.write(objectMapper.writeValueAsBytes(content));
                                file.write(System.lineSeparator().getBytes());
                            }
                            file.close();
                        }
                        if (v.isSyncDone()) {
                            return;
                        }
                        fileName = k.replace(":", "_") + "_连接状态.txt";
                        File file2 = new File(System.getProperty("user.dir") + "/src/main/resources/" + fileName);
                        BufferedWriter bw = new BufferedWriter(
                                new OutputStreamWriter(new FileOutputStream(file2), Charset.forName("utf-8")));
                        if (!file2.exists()) {
                            try {
                                file2.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        bw.write("客户端地址: " + k);
                        bw.newLine();
                        bw.write("通道连接信息: " + v.getChannel());
                        bw.newLine();
                        bw.write("连接注册时间: " + v.getRegistryTime());
                        bw.newLine();
                        bw.write("连接状态最后修改时间: " + v.getModifyTime());
                        bw.newLine();
                        bw.write("当前连接状态: " + ChannelInfo.status2String(v.getStatus()));
                        bw.newLine();
                        v.setSyncDone(true);
                        bw.flush();
                        bw.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }
}
