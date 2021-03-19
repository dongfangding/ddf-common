package com.ddf.boot.common.core.promise;

/**
 *
 * CompletableFuture找不到异常
 *
 * @date 2020/4/9 0009 14:38
 */
public class CompletableFutureNotFindException extends RuntimeException {

    public CompletableFutureNotFindException(String requestId) {
        super(requestId);
    }
}
