package com.ddf.common.ons.transaction;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 业务结果查询器实例池
 *
 * @author snowball
 * @date 2022/6/17
 **/
public abstract class BizResultCheckerPool {

    private static final Logger LOGGER = LoggerFactory.getLogger(BizResultCheckerPool.class);

    /**
     * 当前所有业务结果查询器实例池
     */
    private static final Map<String, BizResultChecker> INSTANCE_POOL = new HashMap<>();

    /**
     * 注册业务结果查询器实例
     * @param routeKey 业务实现路由key
     * @param instance 业务结果查询器实例
     */
    public static void register(String routeKey, BizResultChecker instance) {
        INSTANCE_POOL.putIfAbsent(routeKey, instance);
        LOGGER.info("注册事务消息本地事务处理器：[{}]的业务结果查询器实例：{}", routeKey, instance.toString());
    }

    /**
     * 根据主题获取业务结果查询器实例
     * @return
     */
    public static <T extends BizResultChecker> T get(String routeKey) {
        BizResultChecker instance = INSTANCE_POOL.get(routeKey);
        if(Objects.isNull(instance)) {
            throw new RuntimeException("没有找到：[" + routeKey + "]的业务结果查询器实例");
        }
        return (T) instance;
    }

}
