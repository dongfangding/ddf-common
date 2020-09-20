package com.ddf.boot.netty.broker.storage;

import com.ddf.boot.netty.broker.handler.ServerInboundHandler;
import com.ddf.boot.netty.broker.message.RequestContent;
import io.netty.channel.Channel;

import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 通道连接信息对象
 *
 * @author dongfang.ding
 * @date 2019/7/8 9:49
 */
public class ChannelInfo {
    /**
     * 状态
     */
    private static final int STATUS_REGISTRY = 1;
    private static final int STATUS_ACTIVE = 2;
    private static final int STATUS_INACTIVE = 3;


    /**
     * 通道连接信息
     */
    private Channel channel;
    /**
     * 客户端远程地址
     */
    private String remoteAddress;
    /**
     * 连接状态 1 注册  2 在线 3 掉线
     */
    private int status;
    /**
     * 注册时间
     */
    private Date registryTime;
    /**
     * 最后更改时间
     */
    private Date modifyTime;
    /**
     * 每个连接信息用来存储信息接收的队列
     */
    private BlockingQueue<RequestContent> queue;

    /**
     * 是否已经同步的标识(只同步连接状态，因为连接状态牵扯到状态更新，传输内容是使用队列的，
     * 到时候只要判断队列中是否有数据就可以处理)，防止为了没处理过的数据去浪费时间
     */
    private boolean syncDone;

    public ChannelInfo() {

    }

    public ChannelInfo(Channel channel, String remoteAddress, int status, Date registryTime, Date modifyTime,
                       BlockingQueue<RequestContent> queue, boolean syncDone) {
        this.channel = channel;
        this.remoteAddress = remoteAddress;
        this.status = status;
        this.registryTime = registryTime;
        this.modifyTime = modifyTime;
        this.queue = queue;
        this.syncDone = false;
    }

    /**
     * 注册时连接信息包装
     *
     * @param channel
     * @return
     */
    public static ChannelInfo registry(Channel channel) {
        return new ChannelInfo(channel, channel.remoteAddress().toString(), STATUS_REGISTRY, new Date(), new Date(),
                new ArrayBlockingQueue<>(1024), false);
    }

    /**
     * 连接激活时修改连接信息
     *
     * @param channel
     * @return
     */
    public static ChannelInfo active(Channel channel) {
        return modify(channel, STATUS_ACTIVE);
    }

    /**
     * 连接掉线时修改连接信息
     *
     * @param channel
     * @return
     */
    public static ChannelInfo inactive(Channel channel) {
        return modify(channel, STATUS_INACTIVE);
    }

    private static ChannelInfo modify(Channel channel, int status) {
        String key = channel.remoteAddress().toString();
        ChannelInfo info = ServerInboundHandler.channelStore.get(key);
        if (info == null) {
            info = registry(channel);
        }
        info.setStatus(status);
        info.setModifyTime(new Date());
        info.setSyncDone(false);
        return info;
    }

    public static String status2String(int status) {
        if (status == STATUS_REGISTRY) {
            return "注册";
        } else if (status == STATUS_ACTIVE) {
            return "激活";
        } else if (status == STATUS_INACTIVE) {
            return "掉线";
        }
        return null;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getRegistryTime() {
        return registryTime;
    }

    public void setRegistryTime(Date registryTime) {
        this.registryTime = registryTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public BlockingQueue<RequestContent> getQueue() {
        return queue;
    }

    public void setQueue(BlockingQueue<RequestContent> queue) {
        this.queue = queue;
    }

    public boolean isSyncDone() {
        return syncDone;
    }

    public void setSyncDone(boolean syncDone) {
        this.syncDone = syncDone;
    }
}
