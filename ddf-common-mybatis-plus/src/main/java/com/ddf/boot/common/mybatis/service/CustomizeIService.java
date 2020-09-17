package com.ddf.boot.common.mybatis.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ddf.boot.common.core.entity.BaseDomain;
import com.ddf.boot.common.core.exception200.ServerErrorException;

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
        return saveCheckDuplicateKey(entity, new ServerErrorException(entity.getClass().getSimpleName() + "违反数据库唯一约束"));
    }

    /**
     * 保存的时候捕获唯一数据库唯一约束异常，并抛出自定义异常消息（运行时异常）
     *
     * @param entityList
     * @param exception
     * @return company
     */
    boolean saveBatchCheckDuplicateKey(Collection<T> entityList, RuntimeException exception);


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
    boolean updateCheckBool(T entity, Wrapper<T> updateWrapper, RuntimeException updateFalseException, RuntimeException duplicateKeyException);


    /**
     * 更新时检查返回结果，如果为false,则抛出运行时异常
     * 如果更新时导致唯一索引异常，提供一个接收异常
     *
     * @param entity
     * @param updateFalseException  更新结果为false的异常
     * @param duplicateKeyException 更新导致唯一索引异常
     * @return
     */
    boolean updateByIdCheckBool(T entity, RuntimeException updateFalseException, RuntimeException duplicateKeyException);

    /**
     * 更新时检查返回结果，如果为false,则抛出运行时异常
     * 如果更新时导致唯一索引异常，提供一个接收异常
     *
     * @param entity
     * @param updateFalseException  更新结果为false的异常
     * @return
     */
    default boolean updateByIdCheckBool(T entity, RuntimeException updateFalseException) {
        return updateByIdCheckBool(entity, updateFalseException, new RuntimeException("违反数据库唯一索引！"));
    }
}
