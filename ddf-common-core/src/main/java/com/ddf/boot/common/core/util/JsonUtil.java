package com.ddf.boot.common.core.util;

import com.ddf.boot.common.core.exception200.ServerErrorException;
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
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
        } catch (JsonProcessingException e) {
            logger.error("对象转换Json失败", e);
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
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error("对象序列化失败", e);
            throw new ServerErrorException(e);
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
            logger.error("Json转换对象失败", e);
            throw new ServerErrorException(e);
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
            logger.error("对象转换字节失败", e);
            throw e;
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
        try {
            return OBJECT_MAPPER.readValue(bytes, type);
        } catch (IOException e) {
            logger.error("字节转换对象失败", e);
            throw e;
        }
    }

    /**
     * 将json数据转换成pojo对象list
     *
     * 如果想使用的话，可以参考newInstance().readValue(jsonData, new Ty peReference<List<T>>()， 这里的T不使用泛型，直接使用具体对象是可以的
     *
     * @param jsonData json数据
     * @param beanType 类型
     * @param <T>      类型
     * @return T
     */
    @SneakyThrows
    public static <T> List<T> toList(String jsonData, Class<T> beanType) {
        // 这个可行，但用到了其它库
//        return JSONUtil.toList(new JSONArray(jsonData), beanType);
        // 这里不知道为啥用泛型不行， T如果是个具体类就可以
//        return OBJECT_MAPPER.readValue(jsonData, new TypeReference<List<T>>() {
//            @Override
//            public Type getType() {
//                return super.getType();
//            }
//        });
        final List list = toBean(jsonData, List.class);
        List<T> resList = new ArrayList<>(list.size());
        for (Object obj : list) {
            resList.add(toBean(asString(obj), beanType));
        }
        return resList;
    }


//    public static void main(String[] args) throws JsonProcessingException {
//        String jsonStr = "[{\"invitePicUrl\": \"SYSTEM/club_background/1-4.png\", \"myClubPicUrl\": \"SYSTEM/club_background/1-2.png\", \"recommendPicUrl\": \"SYSTEM/club_background/1-1.png\", \"personalCenterPicUrl\": \"SYSTEM/club_background/1-3.png\"}, {\"invitePicUrl\": \"SYSTEM/club_background/2-4.png\", \"myClubPicUrl\": \"SYSTEM/club_background/2-2.png\", \"recommendPicUrl\": \"SYSTEM/club_background/2-1.png\", \"personalCenterPicUrl\": \"SYSTEM/club_background/2-3.png\"}, {\"invitePicUrl\": \"SYSTEM/club_background/3-4.png\", \"myClubPicUrl\": \"SYSTEM/club_background/3-2.png\", \"recommendPicUrl\": \"SYSTEM/club_background/3-1.png\", \"personalCenterPicUrl\": \"SYSTEM/club_background/3-3.png\"}, {\"invitePicUrl\": \"SYSTEM/club_background/4-4.png\", \"myClubPicUrl\": \"SYSTEM/club_background/4-2.png\", \"recommendPicUrl\": \"SYSTEM/club_background/4-1.png\", \"personalCenterPicUrl\": \"SYSTEM/club_background/4-3.png\"}, {\"invitePicUrl\": \"SYSTEM/club_background/5-4.png\", \"myClubPicUrl\": \"SYSTEM/club_background/5-2.png\", \"recommendPicUrl\": \"SYSTEM/club_background/5-1.png\", \"personalCenterPicUrl\": \"SYSTEM/club_background/5-3.png\"}, {\"invitePicUrl\": \"SYSTEM/club_background/6-4.png\", \"myClubPicUrl\": \"SYSTEM/club_background/6-2.png\", \"recommendPicUrl\": \"SYSTEM/club_background/6-1.png\", \"personalCenterPicUrl\": \"SYSTEM/club_background/6-3.png\"}, {\"invitePicUrl\": \"SYSTEM/club_background/7-4.png\", \"myClubPicUrl\": \"SYSTEM/club_background/7-2.png\", \"recommendPicUrl\": \"SYSTEM/club_background/7-1.png\", \"personalCenterPicUrl\": \"SYSTEM/club_background/7-3.png\"}, {\"invitePicUrl\": \"SYSTEM/club_background/8-4.png\", \"myClubPicUrl\": \"SYSTEM/club_background/8-2.png\", \"recommendPicUrl\": \"SYSTEM/club_background/8-1.png\", \"personalCenterPicUrl\": \"SYSTEM/club_background/8-3.png\"}, {\"invitePicUrl\": \"SYSTEM/club_background/9-4.png\", \"myClubPicUrl\": \"SYSTEM/club_background/9-2.png\", \"recommendPicUrl\": \"SYSTEM/club_background/9-1.png\", \"personalCenterPicUrl\": \"SYSTEM/club_background/9-3.png\"}, {\"invitePicUrl\": \"SYSTEM/club_background/10-4.png\", \"myClubPicUrl\": \"SYSTEM/club_background/10-2.png\", \"recommendPicUrl\": \"SYSTEM/club_background/10-1.png\", \"personalCenterPicUrl\": \"SYSTEM/club_background/10-3.png\"}, {\"invitePicUrl\": \"SYSTEM/club_background/11-4.png\", \"myClubPicUrl\": \"SYSTEM/club_background/11-2.png\", \"recommendPicUrl\": \"SYSTEM/club_background/11-1.png\", \"personalCenterPicUrl\": \"SYSTEM/club_background/11-3.png\"}, {\"invitePicUrl\": \"SYSTEM/club_background/12-4.png\", \"myClubPicUrl\": \"SYSTEM/club_background/12-2.png\", \"recommendPicUrl\": \"SYSTEM/club_background/12-1.png\", \"personalCenterPicUrl\": \"SYSTEM/club_background/12-3.png\"}]";
//        long now = System.currentTimeMillis();
//        final List<ClubBackground> x = toList(jsonStr, ClubBackground.class);
//        System.out.println("循环耗时: " + (System.currentTimeMillis() - now));
//
//        // 这个的性能最好
//        now = System.currentTimeMillis();
//        final List<ClubBackground> backgrounds = getInstance().readValue(
//                jsonStr, new TypeReference<List<ClubBackground>>() {});
//        System.out.println("循环耗时: " + (System.currentTimeMillis() - now));
//
//        now = System.currentTimeMillis();
//        JSONUtil.toList(new JSONArray(jsonStr), ClubBackground.class);
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
        objectMapper.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.setLocale(Locale.SIMPLIFIED_CHINESE);
        objectMapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        // 转换成String序列化的时候对字段进行排序后再序列化
        objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        objectMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new JavaTimeModule());
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
