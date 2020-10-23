package com.ddf.boot.zookeeper.listener;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;

/**
 * <p>节点事件监听器回调</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2020/10/09 14:02
 */
public interface NodeEventListener {

    /**
     * 实现可以有多个，可以根据sort做优先级排序处理，sort值越小，优先级越高
     * @return
     */
    default int getSort() {
        return Integer.MAX_VALUE;
    }

    /**
     * 节点被创建事件
     *
     * 当前节点创建的时候没有放入数据，如果想要放入数据，可以实现后自己放入数据
     *
     * client.setData().forPath(finalPath, "要放入的数据".getBytes())
     *
     * @param client  客户端连接对象
     * @param path    被创建的节点
     * @param oldData 节点旧数据
     * @param data    节点最新数据
     */
    void nodeCreate(CuratorFramework client, String path, ChildData oldData, ChildData data);

    /**
     * 节点数据改变事件
     *
     * @param client  客户端连接对象
     * @param path    被创建的节点
     * @param oldData 节点旧数据
     * @param data    节点最新数据
     */
    void nodeChange(CuratorFramework client, String path, ChildData oldData, ChildData data);

    /**
     * 节点被删除事件
     * 
     * 还有一种情况，当集群中的服务是属于同一个服务时， 如果集群中的所有服务挂掉了，就没有人来处理这个事件了
     *
     * @param client  客户端连接对象
     * @param path    被创建的节点
     * @param oldData 节点旧数据
     * @param data    节点最新数据
     */
    void nodeDeleted(CuratorFramework client, String path, ChildData oldData, ChildData data);


}
