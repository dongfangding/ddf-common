package com.ddf.boot.common.mybatis.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ddf.boot.common.core.entity.BaseDomain;
import com.ddf.boot.common.mybatis.mapper.CustomizeBaseMapper;
import com.ddf.boot.common.mybatis.service.CustomizeIService;
import org.springframework.dao.DuplicateKeyException;

import java.util.Collection;

/**
 * 自定义接口查询
 *
 * _ooOoo_
 * o8888888o
 * 88" . "88
 * (| -_- |)
 * O\ = /O
 * ___/`---'\____
 * .   ' \\| |// `.
 * / \\||| : |||// \
 * / _||||| -:- |||||- \
 * | | \\\ - /// | |
 * | \_| ''\---/'' | |
 * \ .-\__ `-` ___/-. /
 * ___`. .' /--.--\ `. . __
 * ."" '< `.___\_<|>_/___.' >'"".
 * | | : `- \`.;`\ _ /`;.`/ - ` : | |
 * \ \ `-. \_ __\ /__ _/ .-` / /
 * ======`-.____`-.___\_____/___.-`____.-'======
 * `=---='
 * .............................................
 * 佛曰：bug泛滥，我已瘫痪！
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
     * 保存的时候捕获唯一数据库唯一约束异常，并抛出自定义异常消息（运行时异常）
     *
     * @param entityList
     * @param exception
     * @return company
     */
    @Override
    public boolean saveBatchCheckDuplicateKey(Collection<T> entityList, RuntimeException exception) {
        try {
            return super.saveBatch(entityList);
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

    /**
     * 更新时检查返回结果，如果为false,则抛出运行时异常
     * 如果更新时导致唯一索引异常，提供一个接收异常
     *
     * @param entity
     * @param updateWrapper
     * @param updateFalseException  更新结果为false的异常
     * @param duplicateKeyException 更新导致唯一索引异常
     * @return
     */
    @Override
    public boolean updateCheckBool(T entity, Wrapper<T> updateWrapper, RuntimeException updateFalseException, RuntimeException duplicateKeyException) {
        boolean update;
        try {
            update = super.update(entity, updateWrapper);
        } catch (DuplicateKeyException e) {
            throw duplicateKeyException;
        }
        if (!update) {
            throw duplicateKeyException;
        }
        return true;
    }

    /**
     * 更新时检查返回结果，如果为false,则抛出运行时异常
     * 如果更新时导致唯一索引异常，提供一个接收异常
     *
     * @param entity
     * @param updateFalseException  更新结果为false的异常
     * @param duplicateKeyException 更新导致唯一索引异常
     * @return
     */
    @Override
    public boolean updateByIdCheckBool(T entity, RuntimeException updateFalseException, RuntimeException duplicateKeyException) {
        boolean update;
        try {
            update = super.updateById(entity);
        } catch (DuplicateKeyException e) {
            throw duplicateKeyException;
        }
        if (!update) {
            throw updateFalseException;
        }
        return true;
    }
}
