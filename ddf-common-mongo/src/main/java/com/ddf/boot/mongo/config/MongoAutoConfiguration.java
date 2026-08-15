package com.ddf.boot.mongo.config;

import com.ddf.boot.mongo.helper.MongoTemplateHelper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * <p>description</p >
 * <p>
 * todo 基于mongo的通用配置服务
 *
 * @author dongfang.ding
 * @version 1.0
 * @since 2020/09/21 19:16
 */
@AutoConfiguration
@ConditionalOnClass(MongoTemplate.class)
public class MongoAutoConfiguration {

    @Bean
    @ConditionalOnBean(MongoTemplate.class)
    public MongoTemplateHelper mongoTemplateHelper(MongoTemplate mongoTemplate) {
        return new MongoTemplateHelper(mongoTemplate);
    }
}
