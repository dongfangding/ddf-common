package com.ddf.boot.common.mybatis.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ddf.boot.common.entity.BaseDomain;
import com.ddf.boot.common.mybatis.mapper.CustomizeBaseMapper;
import com.ddf.boot.common.mybatis.service.CustomizeIService;
import org.springframework.dao.DuplicateKeyException;

/**
 * 自定义接口查询
 *
 * @author dongfang.ding
 * @date 2019/12/7 0007 22:48
 **/
public class CusomizeIServiceImpl<M extends CustomizeBaseMapper<T>, T extends BaseDomain> extends ServiceImpl<M, T> implements CustomizeIService<T> {

    /**
     * 保存的时候捕获唯一数据库唯一索引异常，并抛出自定义异常消息（运行时异常）
     *
     * @param entity
     * @param exception
     * @return
     */
    @Override
    public boolean saveCheckDuplicateKey(T entity, RuntimeException exception) {
        try {
            return super.save(entity);
        } catch (DuplicateKeyException e) {
            throw exception;
        }
    }

    /**
     * 更新时检查返回结果，如果为false,则抛出运行时异常
     *
     * @param entity
     * @param updateWrapper
     * @param exception
     * @return
     */
    @Override
    public boolean updateCheckBool(T entity, Wrapper<T> updateWrapper, RuntimeException exception) {
        boolean update = super.update(entity, updateWrapper);
        if (!update) {
            throw exception;
        }
        return true;
    }
}
