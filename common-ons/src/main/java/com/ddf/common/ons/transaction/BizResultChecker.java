package com.ddf.common.ons.transaction;

import com.aliyun.openservices.ons.api.Message;
import org.springframework.beans.factory.SmartInitializingSingleton;

/**
 * 业务结果查询器
 *
 * @author snowball
 * @date 2022/6/17
 **/
public interface BizResultChecker extends SmartInitializingSingleton {

    /**
     * 是否成功
     * @param message
     * @return
     */
    boolean isSuccess(Message message);

    /**
     * 业务实现的路由选择, 必须保证不能重复， 这里会作为user_properties的key放入到消息中
     * @return
     */
    String getRouteKey();

    /**
     * 在单例实例化后注册到奖励实例池
     */
    @Override
    default void afterSingletonsInstantiated() {
        BizResultCheckerPool.register(getRouteKey(), this);
    }


}
