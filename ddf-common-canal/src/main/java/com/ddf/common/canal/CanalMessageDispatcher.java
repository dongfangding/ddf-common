package com.ddf.common.canal;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.otter.canal.protocol.FlatMessage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * canal消息业务转发
 * <p>
 * 修复说明：
 * 1. findHandler 方法使用传统 for 循环，确保找到第一个匹配后正确返回
 * 2. 添加 executor 空指针检查
 * </p>
 *
 * @author Snowball
 * @version 1.0
 * @since 2022/07/29 17:20
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CanalMessageDispatcher {

    /**
     * bean name 与 实例映射
     */
    private final Map<String, CanalMessageHandler<?>> beanNameHandlerMapping;

    /**
     * table name 与处理类映射
     */
    private final Map<String, CanalMessageHandler<?>> tableNameHandlerMapping = new ConcurrentHashMap<>(16);

    /**
     * 消息分发
     *
     * @param flatMessage
     */
    public void dispatch(FlatMessage flatMessage) {
        String tableName = flatMessage.getTable();
        // 根据消息找到对应的业务处理类
        CanalMessageHandler<?> handler = findHandler(flatMessage);
        if (handler == null) {
            log.warn("canal消息处理器未找到，tableName：{}", tableName);
            return;
        }
        // 根据业务实体解析出变更前后的消息实体
        final Class<?> dataClazz = handler.getEntityClass();
        final List oldData = getData(flatMessage.getOld(), dataClazz);
        final List newData = getData(flatMessage.getData(), dataClazz);
        // 任务分发
        final ThreadPoolExecutor executor = handler.getExecutor();
        if (executor == null || executor.isShutdown() || executor.isTerminated()) {
            handler.handle(flatMessage, oldData, newData);
            return;
        }
        executor.execute(() -> handler.handle(flatMessage, oldData, newData));
    }


    /**
     * 根据消息内容找到对应的业务处理类
     * <p>
     * 注意：使用传统 for 循环而非 forEach，确保找到匹配后正确返回
     * </p>
     *
     * @param flatMessage
     * @return
     */
    private CanalMessageHandler<?> findHandler(FlatMessage flatMessage) {
        final String tableName = flatMessage.getTable();
        if (!tableNameHandlerMapping.containsKey(tableName)) {
            // 使用传统 for 循环，确保找到匹配后正确返回
            for (Map.Entry<String, CanalMessageHandler<?>> entry : beanNameHandlerMapping.entrySet()) {
                CanalMessageHandler<?> handlerInstance = entry.getValue();
                if (handlerInstance.match(tableName)) {
                    tableNameHandlerMapping.put(tableName, handlerInstance);
                    return handlerInstance;
                }
            }
        }
        return tableNameHandlerMapping.get(tableName);
    }

    /**
     * 获取data中的数据
     *
     * @param dataMap
     * @param clazz
     * @return
     */
    private <T> List<T> getData(List<Map<String, String>> dataMap, Class<T> clazz) {
        if (CollectionUtils.isEmpty(dataMap)) {
            return Collections.emptyList();
        }
        List<T> dataList = new ArrayList<>(dataMap.size());
        for (Map<String, String> datum : dataMap) {
            dataList.add(JSONObject.parseObject(JSONObject.toJSONString(datum), clazz));
        }
        return dataList;
    }
}
