package com.ddf.boot.common.mq.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ddf.boot.common.core.util.IdsUtil;
import com.ddf.boot.common.mq.entity.LogMqListener;
import com.ddf.boot.common.mq.listener.ListenerQueueEntity;
import com.ddf.boot.common.mq.mapper.LogMqListenerMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * mq监听日志持久化到数据库$
 *
 * @author dongfang.ding
 * @date 2020/9/20 0020 13:26
 */
@Component("dbLogMqPersistenceProcessor")
@Slf4j
public class DbLogMqPersistenceProcessor implements LogMqPersistenceProcessor {
    @Autowired
    private LogMqListenerMapper logMqListenerMapper;

    /**
     * 落库方案
     *
     * @param poll          原始数据内容
     * @param logMqListener 处理后的内容
     */
    @Override
    public void persistence(ListenerQueueEntity<?> poll, LogMqListener logMqListener) {
        // fixme 使用INSERT ... ON DUPLICATE KEY UPDATE，但是这样的话就要写mapper.xml，又要配置mapper-location,目前
        // 未找到注解可以支持配置，而使用配置文件的话，本包是个工具包，
        LambdaQueryWrapper<LogMqListener> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(LogMqListener::getMessageId, poll.getMessageWrapper().getMessageId());
        LogMqListener exist = logMqListenerMapper.selectOne(queryWrapper);
        if (exist == null) {
            logMqListener.setId(IdsUtil.getNextLongId());
            logMqListenerMapper.insert(logMqListener);
        } else {
            logMqListener.setId(exist.getId());
            if (logMqListener.getEventTimestamp() < exist.getEventTimestamp()) {
                log.error("当前数据小于数据库中发生时间，不予更新！ {}===>{}", logMqListener, exist);
                return;
            }
            LambdaUpdateWrapper<LogMqListener> updateWrapper = Wrappers.lambdaUpdate();
            updateWrapper.eq(LogMqListener::getId, exist.getId());
            updateWrapper.le(LogMqListener::getEventTimestamp, logMqListener.getEventTimestamp());
            logMqListenerMapper.update(logMqListener, updateWrapper);
        }
    }
}
