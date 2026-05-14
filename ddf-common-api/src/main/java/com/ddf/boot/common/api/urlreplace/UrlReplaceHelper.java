package com.ddf.boot.common.api.urlreplace;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.URLUtil;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Configuration;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2025/03/25 11:26
 */
@Slf4j
@Configuration
public class UrlReplaceHelper implements BeanFactoryPostProcessor {
    /**
     * "@PostConstruct"注解标记的类中，由于ApplicationContext还未加载，导致空指针<br>
     * 因此实现BeanFactoryPostProcessor注入ConfigurableListableBeanFactory实现bean的操作
     */
    private static ConfigurableListableBeanFactory beanFactory;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        UrlReplaceHelper.beanFactory = beanFactory;
    }

    /**
     * @param value 参数值
     */
    public static String replaceHost(String value) {
        return replaceHost(value, "default");
    }

    /**
     * @param value 参数值
     * @param bucket 参数
     */
    public static String replaceHost(String value, String bucket) {
        try {
            final StaticProperties bean = beanFactory.getBean(StaticProperties.class);
            final Map<String, List<String>> resourceProxyHosts = bean.getResourceProxyHosts();
            final List<String> ignoreHosts = ObjectUtils.defaultIfNull(bean.getIgnoreHosts(), new ArrayList<>());
            if (StringUtils.isBlank(value) || CollUtil.isEmpty(resourceProxyHosts) || !resourceProxyHosts.containsKey(
                    bucket)) {
                return value;
            }
            final List<String> hosts = resourceProxyHosts.get(bucket);
            if (CollUtil.isEmpty(hosts)) {
                return value;
            }
            String resourceProxyHost = hosts.get(RandomUtil.randomInt(0, hosts.size()));

            // 处理一个字段中可能存在多个url的问题
            final String[] multipleValue = value.split("[,，]");
            StringBuilder allTextAfterReplace = new StringBuilder();
            for (int i = 0; i < multipleValue.length; i++) {
                String singleValue = multipleValue[i];
                String currentTextAfterReplace = "";
                final URI uri = URLUtil.toURI(singleValue);
                if (StringUtils.isBlank(uri.getHost())) {
                    currentTextAfterReplace = resourceProxyHost + (singleValue.startsWith("/") ? "" : "/")
                            + singleValue;
                } else {
                    if (ignoreHosts.contains(uri.getHost())) {
                        currentTextAfterReplace = singleValue;
                    } else {
                        currentTextAfterReplace = singleValue.replace(uri.getScheme() + "://" + uri.getHost(),
                                resourceProxyHost);
                    }
                }
                allTextAfterReplace.append(currentTextAfterReplace);
                if (i < multipleValue.length - 1) {
                    allTextAfterReplace.append(",");
                }
            }
            return allTextAfterReplace.toString();
        } catch (Exception e) {
            log.error("连接全局替换出现异常", e);
        }
        return value;
    }
}
