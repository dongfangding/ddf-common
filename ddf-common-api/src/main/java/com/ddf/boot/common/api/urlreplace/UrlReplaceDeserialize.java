package com.ddf.boot.common.api.urlreplace;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * url替换反序列化
 *
 * @author snowball
 * @date 2020/9/25 0025 11:56
 **/
@Configuration
@Slf4j
public class UrlReplaceDeserialize extends JsonDeserializer<String> implements ContextualDeserializer {

    private String bucket;

    public UrlReplaceDeserialize() {
    }

    public UrlReplaceDeserialize(String bucket) {
        this.bucket = bucket;
    }

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        return UrlReplaceHelper.replaceHost(value, bucket);
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property)
            throws JsonMappingException {
        if (property != null && property
                .getType()
                .getRawClass() == String.class) {
            UrlReplace urlReplace = property.getAnnotation(UrlReplace.class);
            if (urlReplace == null) {
                urlReplace = property.getContextAnnotation(UrlReplace.class);
            }
            if (urlReplace != null) {
                return new UrlReplaceDeserialize(urlReplace.bucket());
            }
        }
        return ctxt.findNonContextualValueDeserializer(property.getType());
    }
}
