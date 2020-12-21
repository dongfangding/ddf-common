package com.ddf.boot.common.websocket.listeners;

import com.ddf.boot.zookeeper.monitor.properties.MonitorNode;
import com.ddf.boot.zookeeper.monitor.properties.MonitorProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.Nullable;

/**
 * <p>初始化bean时提供一些额外功能</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2020/10/10 16:24
 */
//@Component
public class BeanPostProcessorImpl implements BeanPostProcessor {

    public static final String WEBSOCKET_ONLINE_SERVER_PATH = "/websocket_online_server_path";


    /**
     * 在bean初始化之前执行一些业务
     *
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Nullable
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        // todo 完善服务下线功能
        if (MonitorProperties.class.equals(bean.getClass())) {
            // 注册当前服务需要的监听节点
            ((MonitorProperties) bean).getMonitors().add(
                    new MonitorNode(MonitorNode.HOST_MODE_AUTO, WEBSOCKET_ONLINE_SERVER_PATH, true));
        }
        return bean;
    }

}
