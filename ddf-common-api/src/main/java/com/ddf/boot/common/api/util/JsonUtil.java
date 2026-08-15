package com.ddf.boot.common.api.util;

import com.ddf.boot.common.api.exception.BaseErrorCallbackCode;
import com.ddf.boot.common.api.exception.ServerErrorException;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

/**
 * Json工具类
 */
public final class JsonUtil {

    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);

    private static final ObjectMapper OBJECT_MAPPER = newInstance();

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String TIME_PATTERN = "HH:mm:ss";

    /**
     * 对象转Json
     *
     * @param obj 对象实例
     */
    public static String toJson(Object obj) {
        if (ObjectUtils.isEmpty(obj)) {
            return StringUtils.EMPTY;
        } else if (obj instanceof String string) {
            return string;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            logger.error("json序列化异常, obj = {}", obj, e);
            throw new ServerErrorException(e);
        }
    }

    /**
     * 对象转字符串（委托 toJson）
     *
     * @param obj 对象实例
     * @return JSON 字符串
     */
    public static String asString(Object obj) {
        return toJson(obj);
    }

    /**
     * Json转对象
     *
     * @param json JSON 字符串
     * @param type 类型
     */
    public static <T> T toBean(String json, Class<T> type) {
        return toBean(json, OBJECT_MAPPER.getTypeFactory().constructType(type));
    }

    /**
     * Json转对象
     *
     * @param json JSON 字符串
     * @param type 类型
     */
    public static <T> T toBeanChecked(Object json, Class<T> type) {
        if (Objects.isNull(json) || StringUtils.isBlank(json.toString())) {
            return null;
        }
        return toBean(json.toString(), OBJECT_MAPPER.getTypeFactory().constructType(type));
    }

    /**
     * Json转对象
     *
     * @param json JSON 字符串
     * @param paramType 参数类型
     * @param types 类型集合
     */
    public static <T> T toBean(String json, Class<?> paramType, Class<?>... types) {
        return toBean(json, OBJECT_MAPPER.getTypeFactory().constructParametricType(paramType, types));
    }

    /**
     * Json转对象
     *
     * @param json JSON 字符串
     * @param type 类型
     */
    public static <T> T toBean(String json, TypeReference<T> type) {
        return toBean(json, OBJECT_MAPPER.getTypeFactory().constructType(type));
    }

    /**
     * Json转对象
     *
     * @param json JSON 字符串
     * @param type 类型
     */
    public static <T> T toBean(String json, JavaType type) {
        try {
            if (StringUtils.isBlank(json)) {
                return null;
            }
            return OBJECT_MAPPER.readValue(json, type);
        } catch (IOException e) {
            logger.error("json反序列化异常, json = {}, type = {}", json, type, e);
            throw new ServerErrorException(BaseErrorCallbackCode.JSON_DESERIALIZER_FILED);
        }
    }

    /**
     * 对象转字节
     *
     * @param obj 对象实例
     */
    @SneakyThrows
    public static byte[] toByte(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            logger.error("对象序列化字节异常, obj = {}", obj, e);
            throw new ServerErrorException(e);
        }
    }

    /**
     * 字节转对象
     *
     * @param bytes bytes参数
     * @param type 类型
     */
    @SneakyThrows
    public static <T> T toBean(byte[] bytes, Class<T> type) {
        return OBJECT_MAPPER.readValue(bytes, type);
    }

    /**
     * 将json数据转换成pojo对象list
     *
     * @param json JSON 字符串
     * @param beanType 类型
     * @param <T> 类型
     * @return T
     */
    @SneakyThrows
    public static <T> List<T> toList(String json, Class<T> beanType) {
        try {
            if (StringUtils.isBlank(json)) {
                return Lists.newArrayList();
            }
            TypeFactory typeFactory = OBJECT_MAPPER.getTypeFactory();
            return OBJECT_MAPPER.readValue(json, typeFactory.constructCollectionType(List.class, beanType));
        } catch (Exception e) {
            logger.error("json反序列化集合异常, json = {}, type = {}", json, beanType, e);
            throw new ServerErrorException(BaseErrorCallbackCode.JSON_DESERIALIZER_FILED);
        }
    }

    /**
     * 根据策略生成Json
     *
     * @param obj 对象实例
     * @param strategy 策略参数
     */
    public static String toJson(Object obj, Include strategy) {
        if (ObjectUtils.isEmpty(obj)) {
            return StringUtils.EMPTY;
        } else if (obj instanceof String string) {
            return string;
        }
        try {
            ObjectMapper mapper = newInstance();
            mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
            mapper.setSerializationInclusion(strategy);
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error("json序列化异常, obj = {}", obj, e);
            throw new ServerErrorException(e);
        }
    }

    /**
     * 初始化ObjectMapperW
     *
     * @param objectMapper ObjectMapper 实例
     */
    private static ObjectMapper config(ObjectMapper objectMapper) {
        // 忽略反序列化时在json字符串中存在, 但在java对象中不存在的属性
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 单引号处理
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        objectMapper.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);
        // 在序列化一个空对象时时不抛出异常
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.setLocale(Locale.SIMPLIFIED_CHINESE);
        objectMapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        // 转换成String序列化的时候对字段进行排序后再序列化
        objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        objectMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 收敛字段可见性：不序列化无 getter 的私有字段，避免内部字段泄露（仅通过 getter/setter 访问）
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.NONE);
        // 去掉默认的时间戳格式
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 序列化时，日期的统一格式
        objectMapper.setDateFormat(new SimpleDateFormat(DATE_TIME_PATTERN));
        // 初始化JavaTimeModule
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        // 处理LocalDateTime
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));
        // 处理LocalDate
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter));
        // 处理LocalTime
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(TIME_PATTERN);
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(timeFormatter));
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(timeFormatter));
        // 注册时间模块, 支持支持JSR310, 即新的时间类(java.time包下的时间类)
        objectMapper.registerModule(javaTimeModule);

        return objectMapper;
    }

    /**
     * 返回当前实例
     *
     * @return ObjectMapper 实例
     * @since 2019年07月25日
     */
    public static ObjectMapper getInstance() {
        return OBJECT_MAPPER;
    }

    /**
     * 返回新实例,同时设置默认值
     *
     * @return 配置好的 ObjectMapper 实例
     */
    public static ObjectMapper newInstance() {
        return config(new ObjectMapper());
    }

}
