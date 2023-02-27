package com.ddf.boot.common.mybatis.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * mybatis-plus的配置类
 *
 * @author dongfang.ding
 * @date 2019/5/22 17:14
 */
@Configuration
public class MyBatisConfig {


    /**
     * 针对 update 和 delete 语句 作用: 阻止恶意的全表更新删除
     *
     * @return
     */
    @Bean
    public MybatisPlusInterceptor blockAttackInnerInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        return interceptor;
    }


    /**
     *
     * 乐观锁支持,where条件中必须带version，否则不会生效
     * 说明:
     *
     * 支持的数据类型只有:int,Integer,long,Long,Date,Timestamp,LocalDateTime
     * 整数类型下 newVersion = oldVersion + 1
     * newVersion 会回写到 entity 中
     * 仅支持 updateById(id) 与 update(entity, wrapper) 方法
     * 在 update(entity, wrapper) 方法下, wrapper 不能复用!!!
     * @return
     */
    @Bean
    public MybatisPlusInterceptor optimisticLockerInnerInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }


    /**
     * 通用字段填充
     * update(T t,Wrapper updateWrapper)时t不能为空,否则自动填充失效
     *
     * @return
     */
    @Bean
    public FillMetaObjectHandler fillMetaObjectHandler() {
        return new FillMetaObjectHandler();
    }

}


