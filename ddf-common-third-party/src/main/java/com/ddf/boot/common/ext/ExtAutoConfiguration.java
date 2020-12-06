package com.ddf.boot.common.ext;

import com.ddf.boot.common.ext.oss.config.OssBeanAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * <p>ext包的自动配置类</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2020/10/12 13:35
 */
@ComponentScan("com.ddf.boot.common.ext")
@Import(value = {OssBeanAutoConfiguration.class})
public class ExtAutoConfiguration {
}
