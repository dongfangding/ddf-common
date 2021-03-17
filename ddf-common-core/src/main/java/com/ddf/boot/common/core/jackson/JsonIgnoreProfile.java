package com.ddf.boot.common.core.jackson;

import com.fasterxml.jackson.annotation.JacksonAnnotation;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * 对原有JsonIgnore的不足进行扩充，现在可以根据不同的环境来决定是否忽略字段
 *
 * @see JacksonAnnotationIntrospector#_isIgnorable(com.fasterxml.jackson.databind.introspect.Annotated)
 * @author Snowball
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface JsonIgnoreProfile {

    boolean value() default true;

    /**
     * 要满足的环境，当value为true且profile满足时才忽略字段的序列化
     * @return
     */
    String[] profile() default {};
}
