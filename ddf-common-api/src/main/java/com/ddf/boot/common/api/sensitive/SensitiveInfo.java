package com.ddf.boot.common.api.sensitive;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * 标识脱敏字段， 仅支持jackson
 *
 * @author dongfang.ding
 * @date 2020/9/25 0025 11:54
 **/
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = SensitiveInfoSerialize.class)
public @interface SensitiveInfo {
    /**
     * 脱敏方式
     *
     * @return
     */
    SensitiveTypeEnum value();
}
