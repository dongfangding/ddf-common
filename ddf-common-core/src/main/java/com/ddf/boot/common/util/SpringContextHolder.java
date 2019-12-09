package com.ddf.boot.common.util;

import lombok.Getter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author dongfang.ding on 2018/12/31
 * 一个工具类，用于获取{@code Spring}的{@link ApplicationContext}，并直接对外暴露一些常用的的方法，
 * 如果需要这里未提供的方法，可以调用{@link SpringContextHolder#getApplicationContext()}然后再具体操作它的方法
 */
@Component
public class SpringContextHolder implements ApplicationContextAware {

	@Getter
	private static ApplicationContext applicationContext;


	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		SpringContextHolder.applicationContext = applicationContext;
	}

	public static <T> T getBean(Class<T> requiredType) throws BeansException {
		return applicationContext.getBean(requiredType);
	}

	public static Object getBean(String name) throws BeansException {
		return applicationContext.getBean(name);
	}

	public static <T> T getBean(String name, Class<T> requiredType) throws BeansException {
		return applicationContext.getBean(name, requiredType);
	}

	public static <T> Map<String, T> getBeansOfType(@Nullable Class<T> type) throws BeansException {
		return applicationContext.getBeansOfType(type);
	}
}
