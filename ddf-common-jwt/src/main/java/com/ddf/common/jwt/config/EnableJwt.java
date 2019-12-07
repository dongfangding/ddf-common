package com.ddf.common.jwt.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 由于Jwt的拦截器类在common中，有些服务引用的其他服务引用了这个服务，也会导致拦截器生效，因此决定需要手动使用
 *
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(value = {JwtFilterRegistrar.class})
public @interface EnableJwt {

}
