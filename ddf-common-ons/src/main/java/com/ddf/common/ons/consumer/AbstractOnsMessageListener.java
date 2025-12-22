package com.ddf.common.ons.consumer;

import com.aliyun.openservices.ons.api.Message;
import com.ddf.common.ons.properties.OnsProperties;
import java.io.Serializable;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * ONS抽象类
 *
 * @author snowball
 * @date 2021/8/26 16:43
 **/
public abstract class AbstractOnsMessageListener<D extends Serializable> implements OnsMessageListener<D> {

    @Autowired
    protected OnsProperties onsConfiguration;

    protected Class<D> domainClass;

    @PostConstruct
    protected void init() {
        domainClass = parseDomainClass();
    }

    @Override
    public Class<D> getDomainClass() {
        return domainClass;
    }

    @Override
    public boolean isBizSuccess(Message message, D domain) {
        return false;
    }

    /**
     * 记录消费成功消息日志
     *
     * @param message
     */
    protected void infoMessage(Message message) {
    }

    /**
     * 记录消费失败消息日志
     *
     * @param message
     */
    protected void errorMessage(Message message, String failureReason) {
    }

}
