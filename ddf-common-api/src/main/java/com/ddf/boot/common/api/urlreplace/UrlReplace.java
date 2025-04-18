package com.ddf.boot.common.api.urlreplace;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * 标识在要解析的字段上，提供jackson入参和出参的序列化，将入参中的url，替换成指定的host, 主要是为了解决资源存储的问题，
 * 让客户端传相对路径，服务端实时替换成最新的域名，防止资源域名无法使用后迁移问题
 * <p>
 * 标识要替换的文案， 仅支持jackson
 *
 * @author snowball
 * @date 2020/9/25 0025 11:54
 **/
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
