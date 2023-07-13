package com.ddf.boot.common.api.model.common.response.response;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import com.ddf.boot.common.api.util.JsonUtil;
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
public class ConfigResponse<T> {

    private static Map<String, Constructor> constructorMap = new ConcurrentHashMap<>();

    private static final ConfigResponse EMPTY_CONFIG = new ConfigResponse();

    /**
     * 配置code
     */
    private String code;

    /**
     * 配置 json
     */
    private String configJson;

    /**
     * 配置json对应数据对象
     */
    private Class<T> configJsonClazz;

    /**
     * 配置描述
     */
    private String desc;

    public static <T> ConfigResponse<T> empty() {
        return EMPTY_CONFIG;
    }

    /**
     * 判定当前配置是否是空对象
     *
     * @return
     */
    public boolean isEmpty() {
        return this == EMPTY_CONFIG;
    }

    /**
     * 获取配置
     *
     * @return
     */
    public T resolveConfig() {
        if (ObjectUtil.isEmpty(configJson)) {
            return null;
        }
        if (NumberUtil.isNumber(configJson)) {
            return resolveBox();
        }
        if (configJsonClazz.isAssignableFrom(List.class)) {
            return JsonUtil.toBean(configJson, configJsonClazz);
        }
        return JsonUtil.toBean(configJson, configJsonClazz);
    }


    /**
     * 获取列表配置bi
     *
     * @return
     */
    public List<T> resolveListConfig() {
        if (ObjectUtil.isEmpty(configJson)) {
            return Collections.emptyList();
        }
        return JsonUtil.toList(configJson, configJsonClazz);
    }

    /**
     * 获取基本数据类型的装箱类型
     * @return
     */
    private T resolveBox() {
        try {
            final String className = configJsonClazz.getName();
            Constructor<T> constructor;
            if (constructorMap.containsKey(className)) {
                constructor = constructorMap.get(className);
            } else {
                constructor = configJsonClazz.getConstructor(String.class);
                constructorMap.put(className, constructor);
            }
            try {
                return constructor.newInstance(configJson);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                log.error("[获取配置对象]-转换异常", e);
                return null;
            }
        } catch (NoSuchMethodException e) {
            log.error("[获取配置对象]-转换异常", e);
            return null;
        }
    }

}
