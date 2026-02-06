package com.ddf.boot.common.api.urlreplace;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 标识要替换的文案， 仅支持jackson
 *
 * @author snowball
 * @since 2020/9/25 0025 11:54
 **/
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = UrlReplaceSerialize.class)
@JsonDeserialize(using = UrlReplaceDeserialize.class)
public @interface UrlReplace {

    /**
     * 存储桶名称
     *
     * @return
     */
    String bucket() default "default";
}
