package com.ddf.boot.common.core.promise;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.google.common.base.Preconditions;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * 回调异常
 *
 * @author dongfang.ding
 * @since 2020/4/9 0009 14:38
 */
@Slf4j
public class CompletableFutureHelper<T> {

    /**
     * 会存在回调还没回来的时候服务器宕机了，内存中的数据会被清除，目前业务不会因为这个有影响，有影响的不要使用
     */
    private final Map<String, CompletableFuture<T>> completableFutureMap = new ConcurrentHashMap<>();

    /**
     * 如果在指定时间内没有回调，则触发异常
     */
    public static final long DEFAULT_TIMEOUT_MILLIONS = 60000 * 5;

    /**
     * 用来处理超时移除CompletableFuture对象的定时线程池（共享、daemon）
     */
    private static final ScheduledThreadPoolExecutor TIMEOUT_SCHEDULER = new ScheduledThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors(), ThreadFactoryBuilder.create().setNamePrefix(
            "completable-helper-pool-").setDaemon(true).build());


    /**
     * 创建一个带超时的回调函数
     *
     * @param requestId 请求 ID
     * @param timeoutMilliSeconds 超时milliseconds参数
     */
    public CompletableFuture<T> create(String requestId, long timeoutMilliSeconds) {
        log.info("[{}]请求创建回调, ", requestId);
        CompletableFuture<T> completableFuture = new CompletableFuture<>();
        // fixme 加入创建对象的时候请求对象已经有戴回调了怎么处理？
        completableFutureMap.put(requestId, completableFuture);
        final long timeout = timeoutMilliSeconds > 0 ? timeoutMilliSeconds : DEFAULT_TIMEOUT_MILLIONS;
        TIMEOUT_SCHEDULER.schedule(() -> {
            // 使用 compute 原子地检查并移除，避免 requestId.intern() 污染字符串常量池
            completableFutureMap.compute(requestId, (key, future) -> {
                if (future != null && !future.isDone() && !future.isCancelled() && !future.isCompletedExceptionally()) {
                    log.info("[{}]将[{}]回调设置为超时，移除回调对象, ", Thread.currentThread().getName(), key);
                    future.completeExceptionally(new CallbackTimeoutException(key));
                }
                return null;
            });
        }, timeout, TimeUnit.MILLISECONDS);
        log.info("[{}]请求返回promise, ", requestId);
        return completableFuture;
    }

    /**
     * 创建要给默认超时时间的CompletableFuture
     *
     * @param requestId 请求 ID
     */
    public CompletableFuture<T> create(String requestId) {
        return create(requestId, DEFAULT_TIMEOUT_MILLIONS);
    }


    /**
     * 创建一个自动触发完成的CompletableFuture
     */
    public CompletableFuture<T> createCompletedFuture() {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * 创建一个自动触发完成的CompletableFuture
     *
     * @param v 参数
     */
    public CompletableFuture<T> createCompletedFuture(T v) {
        return CompletableFuture.completedFuture(v);
    }

    /**
     * 完成一个回调函数
     *
     * @param requestId 请求 ID
     * @param t 异常对象
     */
    public boolean complete(String requestId, T t) {
        boolean complete = find(requestId).complete(t);
        remove(requestId);
        return complete;
    }

    /**
     * 取消任务
     *
     * @param requestId 请求 ID
     */
    public boolean cancel(String requestId) {
        boolean cancel = find(requestId).cancel(true);
        remove(requestId);
        return cancel;
    }

    /**
     * 触发回调任务异常
     *
     * @param requestId 请求 ID
     * @param throwable 异常对象
     */
    public boolean completeExceptionally(String requestId, Throwable throwable) {
        boolean b = find(requestId).completeExceptionally(throwable);
        remove(requestId);
        return b;
    }

    /**
     * @param requestId 参数
     */
    private CompletableFuture<T> find(String requestId) {
        Preconditions.checkArgument(StringUtils.isNotBlank(requestId));
        CompletableFuture<T> completableFuture = completableFutureMap.get(requestId);
        if (completableFuture == null) {
            throw new CompletableFutureNotFindException(requestId);
        }
        return completableFuture;
    }

    /**
     * 删除Map中的指定回调对象
     *
     * @param requestId 请求 ID
     */
    private void remove(String requestId) {
        completableFutureMap.remove(requestId);
    }
}
