package com.ddf.boot.common.websocket.enumu;

/**
 * 设备指令运行状态
 *
 * @author dongfang.ding
 * @since 2020/3/11 0011 15:59
 */
public enum DeviceRunningStateStatus {

    /**
     * 未运行
     * <p>
     * 未运行和执行结束的区别在于未运行是从未运行，只要运行过就是执行结束，在这里没有在执行结束的时候把状态重置为未运行；
     * 所以如果有业务处理的时候，空闲状态时包含未运行和执行结束的
     */
    NOT_RUNNING(0),

    /**
     * 运行中
     */
    RUNNING(1),

    /**
     * 执行结束
     */
    OVER(2);

    private Integer status;

    DeviceRunningStateStatus(Integer status) {
        this.status = status;
    }

    public Integer getStatus() {
        return status;
    }
}
