package com.ddf.boot.common.core.sensitive;

import com.ddf.boot.common.core.util.PretendUtils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import java.io.IOException;
import java.util.Objects;
import org.springframework.context.annotation.Configuration;

/**
 * 功能描述:数据脱敏序列化
 *
 * @author dongfang.ding
 * @date 2020/9/25 0025 11:56
 **/
@Configuration
public class SensitiveInfoSerialize extends JsonSerializer<String> implements ContextualSerializer {

    private SensitiveTypeEnum type;

    public SensitiveInfoSerialize() {
    }

    public SensitiveInfoSerialize(final SensitiveTypeEnum type) {
        this.type = type;
    }

    @Override
    public void serialize(String value, JsonGenerator jsonGenerator, SerializerProvider serializers)
            throws IOException {
        switch (this.type) {
            case CHINESE_NAME: {
                jsonGenerator.writeString(PretendUtils.chineseName(value));
                break;
            }
            case ID_CARD: {
                jsonGenerator.writeString(PretendUtils.fixIdCardNum(value));
                break;
            }
            case FIXED_PHONE: {
                jsonGenerator.writeString(PretendUtils.fixedPhone(value));
                break;
            }
            case MOBILE_PHONE: {
                jsonGenerator.writeString(PretendUtils.mobilePhone(value));
                break;
            }
            case ADDRESS: {
                jsonGenerator.writeString(PretendUtils.address(value, 4));
                break;
            }
            case EMAIL: {
                jsonGenerator.writeString(PretendUtils.email(value));
                break;
            }
            case BANK_CARD: {
                jsonGenerator.writeString(PretendUtils.bankCard(value));
                break;
            }
            case PASSWORD: {
                jsonGenerator.writeString(PretendUtils.password(value));
                break;
            }
            default:
                break;
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider serializerProvider, BeanProperty beanProperty)
            throws JsonMappingException {
        // 为空直接跳过
        if (beanProperty != null) {
            // 非 String 类直接跳过
            if (Objects.equals(beanProperty.getType().getRawClass(), String.class)) {
                SensitiveInfo sensitiveInfo = beanProperty.getAnnotation(SensitiveInfo.class);
                if (sensitiveInfo == null) {
                    sensitiveInfo = beanProperty.getContextAnnotation(SensitiveInfo.class);
                }
                // 如果能得到注解，就将注解的 value 传入 SensitiveInfoSerialize
                if (sensitiveInfo != null) {
                    return new SensitiveInfoSerialize(sensitiveInfo.value());
                }
            }
            return serializerProvider.findValueSerializer(beanProperty.getType(), beanProperty);
        }
        return serializerProvider.findNullValueSerializer(beanProperty);
    }

}
