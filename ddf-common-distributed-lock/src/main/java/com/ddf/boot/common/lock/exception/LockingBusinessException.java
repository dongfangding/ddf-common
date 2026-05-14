package com.ddf.boot.common.lock.exception;

/**
 * 获取锁运行时异常
 *
 * @author dongfang.ding
 * @since 2020/3/13 0013 16:37
 */
public class LockingBusinessException extends RuntimeException {
    public LockingBusinessException(Exception e) {
        super(e);
    }

    /**
     * @param msg 参数
     */
    public LockingBusinessException(String msg) {
        super(msg);
    }
}
