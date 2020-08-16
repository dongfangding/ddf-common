package com.ddf.boot.common.lock.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * 自动配置类$
 *
 * @author dongfang.ding
 * @date 2020/8/16 0016 13:48
 */
@Configuration
@MapperScan("com.ddf.boot.common.lock")
public class DistributedLockAutoConfiguration {
}
