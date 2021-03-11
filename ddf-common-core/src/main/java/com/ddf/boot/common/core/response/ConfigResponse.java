package com.ddf.boot.common.core.response;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import com.ddf.boot.common.core.util.JsonUtil;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>配置响应类</p >
 *
 * @author snowball
 * @version 1.0
 * @date 2020/10/09 14:23
 */
@Data
@Accessors(chain = true)
@Slf4j
public class ConfigResponse {

    private Map<String, Constructor> constructorMap = new ConcurrentHashMap<>();

    /**
     * 配置ID
     */
    private Integer id;

    /**
     * 配置 json
     */
    private String configJsonStr;

    /**
     * 获取配置
     *
     * @param classType
     * @param <T>
     * @return
     */
    public <T> T getConfig(Class<T> classType) {
        if (ObjectUtil.isEmpty(configJsonStr)) {
            return null;
        }
        if (NumberUtil.isNumber(configJsonStr)) {
            return getBox(classType);
        }
        if (classType.isAssignableFrom(List.class)) {
            return JsonUtil.toBean(configJsonStr, classType);
        }
        return JsonUtil.toBean(configJsonStr, classType);
    }


    /**
     * 获取列表配置
     *
     * @param classType
     * @param <T>
     * @return
     */
    public <T> List<T> getListConfig(Class<T> classType) {
        if (ObjectUtil.isEmpty(configJsonStr)) {
            return Collections.emptyList();
        }
        return JsonUtil.toList(configJsonStr, classType);
    }

    /**
     * 获取基本数据类型的装箱类型
     * @param classType
     * @param <T>
     * @return
     */
    private <T> T getBox(Class<T> classType) {
        try {
            final String className = classType.getName();
            Constructor<T> constructor;
            if (constructorMap.containsKey(className)) {
                constructor = constructorMap.get(className);
            } else {
                constructor = classType.getConstructor(String.class);
                constructorMap.put(className, constructor);
            }
            try {
                return constructor.newInstance(configJsonStr);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                log.error("转换异常", e);
                return null;
            }
        } catch (NoSuchMethodException e) {
            log.error("转换异常", e);
            return null;
        }
    }

}
