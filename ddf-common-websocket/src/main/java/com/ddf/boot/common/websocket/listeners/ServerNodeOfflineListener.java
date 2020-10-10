package com.ddf.boot.common.websocket.listeners;

import com.ddf.boot.zookeeper.listener.NodeEventListener;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2020/10/10 15:59
 */
@Component
public class ServerNodeOfflineListener implements NodeEventListener {

    @Autowired
    private RemoveOfflineKeyListener removeOfflineKeyListener;

    @Override
    public int getSort() {
        return 1;
    }


    /**
     * 节点被创建事件
     * <p>
     * 当前节点创建的时候没有放入数据，如果想要放入数据，可以实现后自己放入数据
     * <p>
     * client.setData().forPath(finalPath, "要放入的数据".getBytes())
     *
     * @param client  客户端连接对象
     * @param path    被创建的节点
     * @param oldData 节点旧数据
     * @param data    节点最新数据
     */
    @Override
    public void nodeCreate(CuratorFramework client, String path, ChildData oldData, ChildData data) {

    }

    /**
     * 节点数据改变事件
     *
     * @param client  客户端连接对象
     * @param path    被创建的节点
     * @param oldData 节点旧数据
     * @param data    节点最新数据
     */
    @Override
    public void nodeChange(CuratorFramework client, String path, ChildData oldData, ChildData data) {

    }

    /**
     * 节点被删除事件
     * <p>
     * 还有一种情况，当集群中的服务是属于同一个服务时， 如果集群中的所有服务挂掉了，就没有人来处理这个事件了
     *
     * @param client  客户端连接对象
     * @param path    被创建的节点
     * @param oldData 节点旧数据
     * @param data    节点最新数据
     */
    @Override
    public void nodeDeleted(CuratorFramework client, String path, ChildData oldData, ChildData data) {
        // fixme
        // 这里需要多个机器只有一个能执行成功，但是又不能是等待锁，又不能不等待直接放弃；
        // 多个执行多次，有可能第一次是下线了，后面在第二次执行期间又上线了就给删除了，如果是数据库可以有乐观锁条件更新，但是缓存没有这种操作。
        // 如果是等待锁，第一个执行后，第二个又会执行就是上面的情况
        // 如果直接获取不到就放弃，可能是因为锁异常，但其实没有一个服务再执行这个逻辑，不等待的话，最终会导致回调不成功
        // 所以使用缓存到底是不是一个好的方案？？根本无法使用事件发生时间来进行事件前后顺序的控制？
        removeOfflineKeyListener.clearOnline();
    }
}
