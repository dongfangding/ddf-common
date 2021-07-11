package com.ddf.boot.common.lock.config;

import com.ddf.boot.common.lock.zk.config.CuratorFrameworkConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 自动配置类$
 *
 * @author dongfang.ding
 * @date 2020/8/16 0016 13:48
 */
@Configuration
@Import(value = {CuratorFrameworkConfig.class})
public class DistributedLockAutoConfiguration {
}
