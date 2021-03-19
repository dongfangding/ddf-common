package com.ddf.boot.common.core.promise;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import cn.hutool.core.util.RandomUtil;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 回调异常
 *
 * @author dongfang.ding
 * @date 2020/4/9 0009 14:38
 */
@Slf4j
@Component
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
     * 用来处理超时移除CompletableFuture对象的定时线程池
     */
    ScheduledThreadPoolExecutor schedule = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
            ThreadFactoryBuilder.create().setNamePrefix("completable-helper-pool-").build());


    /**
     * 创建一个带超时的回调函数
     *
     * @param requestId
     * @param timeoutMilliSeconds
     * @return
     */
    public CompletableFuture<T> create(String requestId, long timeoutMilliSeconds) {
        log.info("[{}]请求创建回调, ", requestId);
        CompletableFuture<T> completableFuture = new CompletableFuture<>();
        // fixme 加入创建对象的时候请求对象已经有戴回调了怎么处理？
        completableFutureMap.put(requestId, completableFuture);
        final long timeout = timeoutMilliSeconds > 0 ? timeoutMilliSeconds : DEFAULT_TIMEOUT_MILLIONS;
        schedule.schedule(() -> {
            // 应该只要锁这个请求id的字符串对象即可，没必要锁整个对象
            synchronized (requestId.intern()) {
                final CompletableFuture<T> finalFuture = completableFutureMap.get(requestId);
                if (completableFutureMap.containsKey(requestId)) {
                    if (!finalFuture.isDone() && !finalFuture.isCancelled() && !finalFuture.isCompletedExceptionally()) {
                        log.info("[{}]将[{}]回调设置为超时，移除回调对象, ", Thread.currentThread().getName(), requestId);
                        finalFuture.completeExceptionally(new CallbackTimeoutException(requestId));
                    }
                    completableFutureMap.remove(requestId);
                }

            }
        }, timeout, TimeUnit.MILLISECONDS);
        log.info("[{}]请求返回promise, ", requestId);
        return completableFuture;
    }

    /**
     * 创建要给默认超时时间的CompletableFuture
     *
     * @param requestId
     * @return
     */
    public CompletableFuture<T> create(String requestId) {
        return create(requestId, DEFAULT_TIMEOUT_MILLIONS);
    }


    /**
     * 创建一个自动触发完成的CompletableFuture
     * @return
     */
    public CompletableFuture<T> createCompletedFuture() {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * 创建一个自动触发完成的CompletableFuture
     * @return
     */
    public CompletableFuture<T> createCompletedFuture(T v) {
        return CompletableFuture.completedFuture(v);
    }

    /**
     * 完成一个回调函数
     * @param requestId
     * @param t
     * @return
     */
    public boolean complete(String requestId, T t) {
        boolean complete = find(requestId).complete(t);
        remove(requestId);
        return complete;
    }

    /**
     * 取消任务
     * @param requestId
     * @return
     */
    public boolean cancel(String requestId) {
        boolean cancel = find(requestId).cancel(true);
        remove(requestId);
        return cancel;
    }

    /**
     * 触发回调任务异常
     * @param requestId
     * @param throwable
     * @return
     */
    public boolean completeExceptionally(String requestId, Throwable throwable) {
        boolean b = find(requestId).completeExceptionally(throwable);
        remove(requestId);
        return b;
    }

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
     * @param requestId
     */
    private void remove(String requestId) {
        completableFutureMap.remove(requestId);
    }


    public static void main(String[] args) {
        List<String> list = new ArrayList<>(2);
        list.add("hello");
        list.add("world");

        String str = "hello";
        CompletableFutureHelper<String> completableFutureHelper = new CompletableFutureHelper<>();

        CompletableFuture<String> completedFuture = completableFutureHelper.createCompletedFuture("sdsdds");
        for (int i = 0; i < str.length(); i++) {
            final int _i = i;
            completedFuture = completedFuture.thenCompose((e)->{
                String id = String.valueOf(_i);
                CompletableFuture<String> stringCompletableFuture = completableFutureHelper.create(id);
                new Thread(() -> {
                    try {
                        Thread.sleep(RandomUtil.randomInt(5000));
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    System.out.println(str.charAt(_i));
                    stringCompletableFuture.complete("dsds");
                }).start();
                return stringCompletableFuture;
            }).thenCombine(completableFutureHelper.createCompletedFuture(""),(a,b)->{
                return a;
            })     ;
        }





        completableFutureHelper.createCompletedFuture(str).thenApply((r) -> {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return CompletableFuture.completedFuture(r).thenApply((s) -> {
                if (s.length() > 0) {
                    System.out.println(s.charAt(0));
                    return s.substring(1);
                }
                return s;
            });
        }).thenApply((r) -> {
            try {
                Thread.sleep(8000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                return CompletableFuture.completedFuture(r.get()).thenApply((s) -> {
                    if (str.length() > 0) {
                        System.out.println(s.charAt(0));
                        return s.substring(1);
                    }
                    return s;
                });
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            return null;
        }).thenApply((r) -> {
            try {
                Thread.sleep(6000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                return CompletableFuture.completedFuture(r.get()).thenApply((s) -> {
                    if (str.length() > 0) {
                        System.out.println(s.charAt(0));
                        return s.substring(1);
                    }
                    return s;
                });
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            return null;
        }).thenApply((r) -> {
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                return CompletableFuture.completedFuture(r.get()).thenApply((s) -> {
                    if (str.length() > 0) {
                        System.out.println(s.charAt(0));
                        return s.substring(1);
                    }
                    return s;
                });
            } catch (InterruptedException e) {
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            return null;
        }).thenApply((r) -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                return CompletableFuture.completedFuture(r.get()).thenApply((s) -> {
                    if (str.length() > 0) {
                        System.out.println(s.charAt(0));
                        return s.substring(1);
                    }
                    return s;
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            return null;
        });
    }
}
