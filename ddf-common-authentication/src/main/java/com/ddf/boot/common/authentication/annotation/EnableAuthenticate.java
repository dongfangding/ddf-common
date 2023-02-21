package com.ddf.boot.common.authentication.annotation;

import com.ddf.boot.common.authentication.config.AuthenticateFilterRegistrar;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

/**
 *
 * 开启认证模块
 *
 * @author dongfang.ding
 * @date 2019-12-07 16:45
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(value = {AuthenticateFilterRegistrar.class})
public @interface EnableAuthenticate {

}
