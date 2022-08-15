package com.ddf.boot.common.core.util;

import com.ddf.boot.common.core.exception200.BusinessException;
import com.ddf.boot.common.core.exception200.GlobalCallbackCode;
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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
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
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            logger.error("json序列化异常, obj = {}", obj, e);
            throw new BusinessException(GlobalCallbackCode.JSON_SERIALIZER_FILED);
        }
    }

    /**
     * 将对象序列化成json字符串
     *
     * @param obj
     * @return
     */
    public static String asString(Object obj) {
        return toJson(obj);
    }

    /**
     * Json转对象
     *
     * @param json
     * @param type
     * @return
     */
    public static <T> T toBean(String json, Class<T> type) {
        return toBean(json, OBJECT_MAPPER.getTypeFactory().constructType(type));
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
        return toBean(json, OBJECT_MAPPER.getTypeFactory().constructParametricType(paramType, types));
    }

    /**
     * Json转对象
     *
     * @param json
     * @param type
     * @return
     */
    public static <T> T toBean(String json, TypeReference<T> type) {
        return toBean(json, OBJECT_MAPPER.getTypeFactory().constructType(type));
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
            return OBJECT_MAPPER.readValue(json, type);
        } catch (IOException e) {
            logger.error("json反序列化异常, json = {}, type = {}", json, type, e);
            throw new BusinessException(GlobalCallbackCode.JSON_DESERIALIZER_FILED);
        }
    }

    /**
     * 对象转字节
     *
     * @param obj
     * @return
     */
    @SneakyThrows
    public static byte[] toByte(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            logger.error("对象序列化字节异常, obj = {}", obj, e);
            throw new BusinessException(GlobalCallbackCode.JSON_SERIALIZER_FILED);
        }
    }

    /**
     * 字节转对象
     *
     * @param bytes
     * @param type
     * @return
     */
    @SneakyThrows
    public static <T> T toBean(byte[] bytes, Class<T> type) {
        return OBJECT_MAPPER.readValue(bytes, type);
    }

    /**
     * 将json数据转换成pojo对象list
     *
     *
     * @param json json数据
     * @param beanType 类型
     * @param <T>      类型
     * @return T
     */
    @SneakyThrows
    public static <T> List<T> toList(String json, Class<T> beanType) {
        try {
            TypeFactory typeFactory = OBJECT_MAPPER.getTypeFactory();
            return OBJECT_MAPPER.readValue(json, typeFactory.constructCollectionType(List.class, beanType));
        } catch (Exception e) {
            logger.error("json反序列化集合异常, json = {}, type = {}", json, beanType, e);
            throw new BusinessException(GlobalCallbackCode.JSON_DESERIALIZER_FILED);
        }
    }


//    public static void main(String[] args) throws JsonProcessingException {
//        String json = "[{\"invitePicUrl\": \"SYSTEM/club_background/1-4.png\", \"myClubPicUrl\": \"SYSTEM/club_background/1-2.png\", \"recommendPicUrl\": \"SYSTEM/club_background/1-1.png\", \"personalCenterPicUrl\": \"SYSTEM/club_background/1-3.png\"}, {\"invitePicUrl\": \"SYSTEM/club_background/2-4.png\", \"myClubPicUrl\": \"SYSTEM/club_background/2-2.png\", \"recommendPicUrl\": \"SYSTEM/club_background/2-1.png\", \"personalCenterPicUrl\": \"SYSTEM/club_background/2-3.png\"}, {\"invitePicUrl\": \"SYSTEM/club_background/3-4.png\", \"myClubPicUrl\": \"SYSTEM/club_background/3-2.png\", \"recommendPicUrl\": \"SYSTEM/club_background/3-1.png\", \"personalCenterPicUrl\": \"SYSTEM/club_background/3-3.png\"}, {\"invitePicUrl\": \"SYSTEM/club_background/4-4.png\", \"myClubPicUrl\": \"SYSTEM/club_background/4-2.png\", \"recommendPicUrl\": \"SYSTEM/club_background/4-1.png\", \"personalCenterPicUrl\": \"SYSTEM/club_background/4-3.png\"}, {\"invitePicUrl\": \"SYSTEM/club_background/5-4.png\", \"myClubPicUrl\": \"SYSTEM/club_background/5-2.png\", \"recommendPicUrl\": \"SYSTEM/club_background/5-1.png\", \"personalCenterPicUrl\": \"SYSTEM/club_background/5-3.png\"}, {\"invitePicUrl\": \"SYSTEM/club_background/6-4.png\", \"myClubPicUrl\": \"SYSTEM/club_background/6-2.png\", \"recommendPicUrl\": \"SYSTEM/club_background/6-1.png\", \"personalCenterPicUrl\": \"SYSTEM/club_background/6-3.png\"}, {\"invitePicUrl\": \"SYSTEM/club_background/7-4.png\", \"myClubPicUrl\": \"SYSTEM/club_background/7-2.png\", \"recommendPicUrl\": \"SYSTEM/club_background/7-1.png\", \"personalCenterPicUrl\": \"SYSTEM/club_background/7-3.png\"}, {\"invitePicUrl\": \"SYSTEM/club_background/8-4.png\", \"myClubPicUrl\": \"SYSTEM/club_background/8-2.png\", \"recommendPicUrl\": \"SYSTEM/club_background/8-1.png\", \"personalCenterPicUrl\": \"SYSTEM/club_background/8-3.png\"}, {\"invitePicUrl\": \"SYSTEM/club_background/9-4.png\", \"myClubPicUrl\": \"SYSTEM/club_background/9-2.png\", \"recommendPicUrl\": \"SYSTEM/club_background/9-1.png\", \"personalCenterPicUrl\": \"SYSTEM/club_background/9-3.png\"}, {\"invitePicUrl\": \"SYSTEM/club_background/10-4.png\", \"myClubPicUrl\": \"SYSTEM/club_background/10-2.png\", \"recommendPicUrl\": \"SYSTEM/club_background/10-1.png\", \"personalCenterPicUrl\": \"SYSTEM/club_background/10-3.png\"}, {\"invitePicUrl\": \"SYSTEM/club_background/11-4.png\", \"myClubPicUrl\": \"SYSTEM/club_background/11-2.png\", \"recommendPicUrl\": \"SYSTEM/club_background/11-1.png\", \"personalCenterPicUrl\": \"SYSTEM/club_background/11-3.png\"}, {\"invitePicUrl\": \"SYSTEM/club_background/12-4.png\", \"myClubPicUrl\": \"SYSTEM/club_background/12-2.png\", \"recommendPicUrl\": \"SYSTEM/club_background/12-1.png\", \"personalCenterPicUrl\": \"SYSTEM/club_background/12-3.png\"}]";
//        long now = System.currentTimeMillis();
//        final List<ClubBackground> x = toList(json, ClubBackground.class);
//        System.out.println("循环耗时: " + (System.currentTimeMillis() - now));
//
//        // 这个的性能最好
//        now = System.currentTimeMillis();
//        final List<ClubBackground> backgrounds = getInstance().readValue(
//                json, new TypeReference<List<ClubBackground>>() {});
//        System.out.println("循环耗时: " + (System.currentTimeMillis() - now));
//
//        now = System.currentTimeMillis();
//        JSONUtil.toList(new JSONArray(json), ClubBackground.class);
//        System.out.println("循环耗时: " + (System.currentTimeMillis() - now));
//
//        // 上述结果， 因此还是用第二种方式
//        //  循环耗时: 83
//        //  循环耗时: 5
//        //  循环耗时: 54
//    }

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
            logger.error("json序列化异常, obj = {}", obj, e);
            throw new BusinessException(GlobalCallbackCode.JSON_SERIALIZER_FILED);
        }
    }

    /**
     * 初始化ObjectMapperW
     *
     * @param objectMapper
     * @return
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
     * @param
     * @return
     * @since 2019年07月25日
     */
    public static ObjectMapper getInstance() {
        return OBJECT_MAPPER;
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
