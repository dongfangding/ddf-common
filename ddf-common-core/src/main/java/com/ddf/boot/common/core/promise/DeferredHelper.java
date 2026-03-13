package com.ddf.boot.common.core.promise;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import cn.hutool.core.util.IdUtil;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.jdeferred2.Deferred;
import org.jdeferred2.Promise;
import org.jdeferred2.impl.DeferredObject;

/**
 *
 * @since 2020/4/9 0009 13:31
 */
@Slf4j
public class DeferredHelper<D, F, P> {

    /**
     * 会存在回调还没回来的时候服务器宕机了，内存中的数据会被清除，目前业务不会因为这个有影响，有影响的不要使用
     */
    private final Map<String, Deferred<D, Throwable, P>> deferredMap = new ConcurrentHashMap<>();

    /**
     * 如果在指定时间内没有回调，则触发异常
     */
    public static final long DEFAULT_TIMEOUT_MILLIONS = 60000 * 5;

    ScheduledThreadPoolExecutor schedule = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
            ThreadFactoryBuilder.create().setNamePrefix("deferred-helper-pool-").build());


    /**
     * 获取Deferred对象
     *
     * @param requestId 请求 ID
     * @return
     */
    public Deferred<D, Throwable, P> getDeferred(String requestId) {
        return deferredMap.get(requestId);
    }

    public Map<String, Deferred<D, Throwable, P>> getDeferredMap() {
        return deferredMap;
    }

    /**
     * 创建一个带超时的回调函数
     *
     * @param requestId 请求 ID
     * @param timeoutMilliSeconds 超时milliseconds参数
     * @return
     */
    public Promise<D, Throwable, P> createPromise(String requestId, long timeoutMilliSeconds) {
        log.info("[{}]请求创建回调, ", requestId);
        Deferred<D, Throwable, P> deferred = new DeferredObject<>();
        // fixme 加入创建对象的时候请求对象已经有戴回调了怎么处理？
        deferredMap.put(requestId, deferred);
        final long timeout = timeoutMilliSeconds > 0 ? timeoutMilliSeconds : DEFAULT_TIMEOUT_MILLIONS;
        schedule.schedule(() -> {
            // 应该只要锁这个请求id的字符串对象即可，没必要锁整个对象
            synchronized (requestId.intern()) {
                final Deferred<D, Throwable, P> finalDm = deferredMap.get(requestId);
                if (deferredMap.containsKey(requestId)) {
                    if (finalDm.isPending()) {
                        log.info("[{}]将[{}]回调设置为超时，移除回调对象, ", Thread.currentThread().getName(), requestId);
                        finalDm.reject(new CallbackTimeoutException(requestId));
                    }
                    deferredMap.remove(requestId);
                }

            }
        }, timeout, TimeUnit.MILLISECONDS);
        log.info("[{}]请求返回promise, ", requestId);
        return deferred.promise();
    }

    /**
     * 创建promise
     *
     * @param requestId 请求 ID
     * @return
     */
    public Promise<D, Throwable, P> createPromise(String requestId) {
        return createPromise(requestId, DEFAULT_TIMEOUT_MILLIONS);
    }

    /**
     * @param result 结果参数
     * @param id 标识值
     * @return
     */
    public boolean resolve(String id, D result) {
        Deferred<D, Throwable, P> deferredAndRemove = getDeferredAndRemove(id);
        if (deferredAndRemove != null) {
            deferredAndRemove.resolve(result);
            return true;
        }
        return false;
    }

    /**
     * @param id 标识值
     * @param exception 异常对象
     * @return
     */
    public boolean reject(String id, Throwable exception) {
        Deferred<D, Throwable, P> deferredAndRemove = getDeferredAndRemove(id);
        if (deferredAndRemove != null) {
            deferredAndRemove.reject(exception);
            return true;
        }
        return false;
    }

    /**
     * @param result 结果参数
     * @return
     */
    public Promise<D, Throwable, P> resolve(D result) {
        Deferred<D, Throwable, P> deferred = new DeferredObject<>();
        deferred.resolve(result);
        return deferred.promise();
    }

    /**
     * @param exception 异常对象
     * @return
     */
    public Promise<D, Throwable, P> reject(Throwable exception) {
        Deferred<D, Throwable, P> deferred = new DeferredObject<>();
        deferred.reject(exception);
        return deferred.promise();
    }


    /**
     * @param state state参数
     * @param result 结果参数
     * @param exception 异常对象
     * @return
     */
    public Promise<D, Throwable, P> pipeAlways(Promise.State state, D result, Throwable exception) {
        if (state == Promise.State.RESOLVED) {
            return resolve(result);
        } else {
            return reject(exception);
        }
    }

    /**
     * @param id 标识值
     * @return
     */
    private Deferred<D, Throwable, P> getDeferredAndRemove(String id) {
        final Deferred<D, Throwable, P> finalDm = deferredMap.get(id);
        if (deferredMap.containsKey(id) && finalDm.isPending()) {
            deferredMap.remove(id);
            return finalDm;
        }
        return null;
    }

    /**
     * 创建一个自动触发回调的Promise
     */
    public Promise<D, Throwable, P> createAutoResolvePromise() {
        String requestId = IdUtil.objectId();
        Promise<D, Throwable, P> promise = createPromise(requestId);
        getDeferred(requestId).resolve(null);
        return promise;
    }
}
