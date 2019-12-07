package com.ddf.common.mybatis.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.company.pay.core.exception.BusinessException;
import com.ddf.common.entity.BaseDomain;


/**
 * 自定义接口查询
 *
 * @author dongfang.ding
 * @date 2019/12/7 0007 22:57
 **/
public interface CustomizeIService<T extends BaseDomain> extends IService<T> {

    /**
     * 保存的时候捕获唯一数据库唯一约束异常，并抛出自定义异常消息（运行时异常）
     *
     * @param entity
     * @param exception
     * @return boolean
     * @author dongfang.ding
     * @date 2019/12/7 0007 22:34
     **/
    boolean saveCheckDuplicateKey(T entity, RuntimeException exception);

    /**
     * 违反数据库唯一约束，抛出默认的运行时异常
     *
     * @param entity
     * @return boolean
     * @author dongfang.ding
     * @date 2019/12/7 0007 22:34
     **/
    default boolean saveCheckDuplicateKey(T entity) {
        return saveCheckDuplicateKey(entity, new BusinessException(entity.getClass().getSimpleName() + "违反数据库唯一约束"));
    }
    
    /**
     * 更新时检查返回结果，如果为false,则抛出运行时异常
     * 
     * @param entity
     * @param updateWrapper
     * @param exception
     * @return boolean
     * @author dongfang.ding
     * @date 2019/12/7 0007 22:34
     **/
    boolean updateCheckBool(T entity, Wrapper<T> updateWrapper, RuntimeException exception);
    
    
    /**
     * 更新时检查返回结果，如果为false,则抛出运行时异常
     * @param updateWrapper
     * @param exception
     * @return boolean
     * @author dongfang.ding
     * @date 2019/12/7 0007 22:35
     **/
    default boolean updateCheckBool(Wrapper<T> updateWrapper, RuntimeException exception) {
        return updateCheckBool(null, updateWrapper, exception);
    }
}
