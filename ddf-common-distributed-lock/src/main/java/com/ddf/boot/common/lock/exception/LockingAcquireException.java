package com.ddf.boot.common.lock.exception;

/**
 * 获取锁异常
 *
 * @author dongfang.ding
 * @date 2020/3/13 0013 16:37
 */
public class LockingAcquireException extends Exception {

    public LockingAcquireException(Exception e) {
        super(e);
    }

    public LockingAcquireException(String lockKey) {
        super("获取锁异常, lockKey = " + lockKey);
    }
}
