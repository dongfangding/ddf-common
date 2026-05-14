package com.ddf.boot.common.lock.exception;

/**
 * 获取锁异常
 *
 * @author dongfang.ding
 * @since 2020/3/13 0013 16:37
 */
public class LockingAcquireException extends Exception {
    public LockingAcquireException(Exception e) {
        super(e);
    }

    /**
     * @param lockKey 参数
     */
    public LockingAcquireException(String lockKey) {
        super("获取锁异常, lockKey = " + lockKey);
    }
}
