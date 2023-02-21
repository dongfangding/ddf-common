package com.ddf.boot.common.lock.exception;

/**
 * 释放锁异常
 *
 * @author dongfang.ding
 * @date 2020/3/13 0013 16:37
 */
public class LockingReleaseException extends Exception {

    public LockingReleaseException(Exception e) {
        super(e);
    }

    public LockingReleaseException(String msg) {
        super(msg);
    }
}
