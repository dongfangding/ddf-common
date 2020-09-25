package com.ddf.boot.common.core.util;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * Json工具类
 */
public final class JsonUtil {

    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);

    private static final ObjectMapper objectMapper = newInstance();

    /**
     * 对象转Json
     *
     * @param obj
     * @return
     */
    public static String toJson(Object obj) {
        if (ObjectUtils.isEmpty(obj)) {
            return StringUtils.EMPTY;
        } else if (obj instanceof String) {
            return (String) obj;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error("对象转换Json失败", e.getMessage(), e);
        }
        return StringUtils.EMPTY;
    }

    /**
     * 将对象序列化成json字符串
     *
     * @param obj
     * @return
     */
    public static String asString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("对象序列化失败！");
        }
    }

    /**
     * Json转对象
     *
     * @param json
     * @param type
     * @return
     */
    public static <T> T toBean(String json, Class<T> type) {
        return toBean(json, objectMapper.getTypeFactory().constructType(type));
    }

    /**
     * Json转对象
     *
     * @param json
     * @param paramType
     * @param types
     * @return
     */
    public static <T> T toBean(String json, Class<?> paramType, Class<?>... types) {
        return toBean(json, objectMapper.getTypeFactory().constructParametricType(paramType, types));
    }

    /**
     * Json转对象
     *
     * @param json
     * @param type
     * @return
     */
    public static <T> T toBean(String json, TypeReference<T> type) {
        return toBean(json, objectMapper.getTypeFactory().constructType(type));
    }

    /**
     * Json转对象
     *
     * @param json
     * @param type
     * @return
     */
    public static <T> T toBean(String json, JavaType type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (IOException e) {
            throw new RuntimeException("Json转换对象失败", e);
        }
    }

    /**
     * 对象转字节
     *
     * @param obj
     * @return
     */
    public static byte[] toByte(Object obj) {
        try {
            return objectMapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("对象转换字节失败", e);
        }
    }

    /**
     * 字节转对象
     *
     * @param bytes
     * @param type
     * @return
     */
    public static <T> T toBean(byte[] bytes, Class<T> type) {
        try {
            return objectMapper.readValue(bytes, type);
        } catch (IOException e) {
            throw new RuntimeException("字节转换对象失败", e);
        }
    }

    /**
     * 根据策略生成Json
     *
     * @param obj
     * @param strategy
     * @return
     */
    public static String toJson(Object obj, Include strategy) {
        if (ObjectUtils.isEmpty(obj)) {
            return StringUtils.EMPTY;
        } else if (obj instanceof String) {
            return (String) obj;
        }
        try {
            ObjectMapper mapper = newInstance();
            mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
            mapper.setSerializationInclusion(strategy);
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error("对象转换Json失败", e);
        }
        return StringUtils.EMPTY;
    }

    /**
     * 初始化ObjectMapperW
     *
     * @param objectMapper
     * @return
     */
    private static ObjectMapper config(ObjectMapper objectMapper) {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 返回当前实例
     *
     * @param
     * @return
     * @since 2019年07月25日
     */
    public static ObjectMapper getInstance() {
        return objectMapper;
    }

    /**
     * 返回新实例,同时设置默认值
     *
     * @param
     * @return
     */
    public static ObjectMapper newInstance() {
        return config(new ObjectMapper());
    }

}
