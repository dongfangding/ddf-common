package com.ddf.common.canal;

import com.alibaba.otter.canal.protocol.FlatMessage;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * <p>消息处理接口</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/07/29 17:17
 */
public interface CanalMessageHandler<T> {

    /**
     * 根据表名决定是否处理这个消息
     *
     * 比如return tableName.startWith("user_base_info_");
     * 则用来处理user_base_info_*表的canal消息
     *
     * @param tableName
     * @return
     */
    boolean match(String tableName);

    /**
     * 表名对应的实体类，用于类型解析
     *
     * @return
     */
    Class<T> getEntityClass();


    /**
     * 处理消息
     *
     * @param flatMessage 原始消息内容
     * @param oldList     解析后的更改前的数据集合对象
     * @param newList     解析后的更改后的数据集合对象
     */
    void handle(FlatMessage flatMessage, List<T> oldList, List<T> newList);

    /**
     * 因全局监听转发， 为了不引起消费阻塞，影响其它业务，在结构上就定义可以为每个业务指定自己的线程池，用来隔离多个业务
     *
     * @return
     */
    default ThreadPoolExecutor getExecutor() {
        return null;
    }
}
