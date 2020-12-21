package com.ddf.boot.common.mq.persistence;

import com.ddf.boot.common.mq.entity.LogMqListener;
import com.ddf.boot.common.mq.listener.ListenerQueueEntity;

/**
 * mq监听日志落库方案$
 *
 * @author dongfang.ding
 * @date 2020/9/20 0020 13:25
 */
public interface LogMqPersistenceProcessor {

    /**
     * 落库方案
     *
     * @param poll          原始数据内容
     * @param logMqListener 处理后的内容
     */
    void persistence(ListenerQueueEntity<?> poll, LogMqListener logMqListener);
}
