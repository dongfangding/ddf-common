package com.ddf.common.ons.listener;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.SmartLifecycle;

/**
 * ONS监听器容器接口
 *
 * @author snowball
 * @date 2021/8/26 14:22
 **/
public interface OnsListenerContainer extends InitializingBean, DisposableBean, SmartLifecycle {
}
