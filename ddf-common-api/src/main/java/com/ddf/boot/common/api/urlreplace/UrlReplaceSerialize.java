package com.ddf.boot.common.api.urlreplace;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import java.io.IOException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * 功能描述:数据脱敏序列化
 *
 * @author snowball
 * @since 2020/9/25 0025 11:56
 **/
@Configuration
@Slf4j
public class UrlReplaceSerialize extends JsonSerializer<String> implements ContextualSerializer {


    /**
     * 存储桶
     */
    private String bucket;

    public UrlReplaceSerialize() {
    }
    /**
     * @param bucket 参数
     */
    public UrlReplaceSerialize(final String bucket) {
        this.bucket = bucket;
    }
    /**
     * @param value 参数值
     * @param jsonGenerator 参数
     * @param serializers 参数
     */
    @Override
    public void serialize(String value, JsonGenerator jsonGenerator, SerializerProvider serializers)
            throws IOException {
        jsonGenerator.writeString(UrlReplaceHelper.replaceHost(value, bucket));
    }
    /**
     * @param serializerProvider 参数
     * @param beanProperty 参数
     */
    @Override
    public JsonSerializer<?> createContextual(SerializerProvider serializerProvider, BeanProperty beanProperty)
            throws JsonMappingException {
        // 为空直接跳过
        if (beanProperty != null) {
            // 非 String 类直接跳过
            if (Objects.equals(beanProperty
                    .getType()
                    .getRawClass(), String.class)) {
                UrlReplace urlReplace = beanProperty.getAnnotation(UrlReplace.class);
                if (urlReplace == null) {
                    urlReplace = beanProperty.getContextAnnotation(UrlReplace.class);
                }
                if (urlReplace != null) {
                    return new UrlReplaceSerialize(urlReplace.bucket());
                }
            }
            return serializerProvider.findValueSerializer(beanProperty.getType(), beanProperty);
        }
        return serializerProvider.findNullValueSerializer(beanProperty);
    }
}