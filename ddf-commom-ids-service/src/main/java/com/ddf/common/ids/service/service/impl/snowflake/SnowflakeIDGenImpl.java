package com.ddf.common.ids.service.service.impl.snowflake;

import com.ddf.boot.common.core.exception200.BusinessException;
import com.ddf.common.ids.service.config.properties.IdsProperties;
import com.ddf.common.ids.service.exception.IdsErrorCodeEnum;
import com.ddf.common.ids.service.model.common.Result;
import com.ddf.common.ids.service.model.common.ResultList;
import com.ddf.common.ids.service.model.common.Status;
import com.ddf.common.ids.service.service.IDGen;
import com.ddf.common.ids.service.util.Utils;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 雪花id核心实现
 */
public class SnowflakeIDGenImpl implements IDGen {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnowflakeIDGenImpl.class);

    private final long twepoch;
    private final long workerIdBits = 10L;
    private final long maxWorkerId = ~(-1L << workerIdBits);//最大能够分配的workerid =1023
    private final long sequenceBits = 12L;
    private final long workerIdShift = sequenceBits;
    private final long timestampLeftShift = sequenceBits + workerIdBits;
    private final long sequenceMask = ~(-1L << sequenceBits);
    private long workerId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;
    private static final Random RANDOM = new Random();
    private IdsProperties idsProperties;

    public SnowflakeIDGenImpl(IdsProperties idsProperties) {
        this.idsProperties = idsProperties;
        this.twepoch = idsProperties.getBeginTimestamp();
        init();
    }

    @Override
    public boolean init() {
        final String zkAddress = idsProperties.getZkAddress();
        final Integer port = idsProperties.getPort();
        Preconditions.checkArgument(timeGen() > twepoch, "Snowflake not support twepoch gt currentTime");
        final String ip = Utils.getIp();
        SnowflakeZookeeperHolder holder = new SnowflakeZookeeperHolder(ip, idsProperties);
        LOGGER.info("twepoch:{} ,ip:{} ,zkAddress:{} port:{}", twepoch, ip, zkAddress, port);
        boolean initFlag = holder.init();
        if (initFlag) {
            workerId = holder.getWorkerID();
            LOGGER.info("START SUCCESS USE ZK WORKERID-{}", workerId);
        } else {
            Preconditions.checkArgument(initFlag, "Snowflake Id Gen is not init ok");
        }
        Preconditions.checkArgument(workerId >= 0 && workerId <= maxWorkerId, "workerID must gte 0 and lte 1023");
        return true;
    }


    /**
     * 对于雪花id来说，这个key毫无意义，如果调用方需要这个作为前缀，自行处理
     *
     * @param key
     * @return
     */
    @Override
    public synchronized Result get(String key) {
        long timestamp = timeGen();
        if (timestamp < lastTimestamp) {
            long offset = lastTimestamp - timestamp;
            if (offset <= 5) {
                try {
                    wait(offset << 1);
                    timestamp = timeGen();
                    if (timestamp < lastTimestamp) {
                        throw new BusinessException(IdsErrorCodeEnum.CLOCK_BACK_RETRY_FAILURE);
                    }
                } catch (InterruptedException e) {
                    LOGGER.error("wait interrupted");
                    throw new BusinessException(IdsErrorCodeEnum.INTERRUPTED_EXCEPTION);
                }
            } else {
                throw new BusinessException(IdsErrorCodeEnum.CLOCK_BACK);
            }
        }
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                //seq 为0的时候表示是下一毫秒时间开始对seq做随机
                sequence = RANDOM.nextInt(100);
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            //如果是新的ms开始
            sequence = RANDOM.nextInt(100);
        }
        lastTimestamp = timestamp;
        long id = ((timestamp - twepoch) << timestampLeftShift) | (workerId << workerIdShift) | sequence;
        return new Result(String.valueOf(id), Status.SUCCESS);

    }


    @Override
    public ResultList list(String key, int length) {
        if (0 >length) {
            throw new BusinessException(IdsErrorCodeEnum.BATCH_NUMBER_IS_VALID);
        }
        ResultList resultList = new ResultList();
        resultList.setStatus(Status.SUCCESS);
        resultList.setIdList(Lists.newArrayList());
        for (int i = 0; i < length; i++) {
            resultList.getIdList().add(get(key).getId());
        }
        return resultList;
    }

    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    protected long timeGen() {
        return System.currentTimeMillis();
    }

    public long getWorkerId() {
        return workerId;
    }

}
