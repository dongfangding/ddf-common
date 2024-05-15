package com.ddf.common.ons.transaction;

import com.aliyun.openservices.ons.api.Message;
import org.springframework.beans.factory.SmartInitializingSingleton;

/**
 * 业务事务执行器
 *
 * @author snowball
 * @date 2022/6/17 18:29
 **/
public interface BizTransactionExecutor extends SmartInitializingSingleton {

    /**
     * 执行
     * @param msg
     * @param arg
     * @return
     */
    boolean execute(Message msg, Object arg);

    /**
     * 获取业务实现路由key
     *
     * @return
     */
    String getRouteKey();

    /**
     * 在单例实例化后注册到奖励实例池
     */
    @Override
    default void afterSingletonsInstantiated() {
        BizTransactionExecutorPool.register(getRouteKey(), this);
    }


}
