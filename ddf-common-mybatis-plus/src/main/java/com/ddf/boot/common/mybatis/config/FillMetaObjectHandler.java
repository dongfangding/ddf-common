package com.ddf.boot.common.mybatis.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.ddf.boot.common.authentication.util.UserContextUtil;
import com.ddf.boot.common.core.model.BaseDomain;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.reflection.MetaObject;

/**
 * 针对通用字段自动填充功能的实现
 * <p>
 * 实现对通用实体字段的赋值，
 * <p>
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
 * @date 2019/5/22 17:15
 */
@Slf4j
public class FillMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        if (metaObject.getOriginalObject() instanceof BaseDomain) {
            // 切记切记，这里是filedName，是实体属性字段名，而不是数据库列名
            this.strictInsertFill(metaObject, "createBy", String.class, UserContextUtil.getUserId());
            this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
            this.strictInsertFill(metaObject, "modifyBy", String.class, UserContextUtil.getUserId());
            this.strictInsertFill(metaObject, "modifyTime", LocalDateTime.class, LocalDateTime.now());
            // 启用乐观锁以后，version并不会自动赋默认值，导致新增的时候对象中没值，如果使用新对象直接获取version来更新，乐观锁会失效，
            // 采用这种方式如果没有值的话，在新增的时候给个默认值
            Object version = metaObject.getValue("version");
            if (null == version) {
                this.strictInsertFill(metaObject, "version", Long.class, 1L);
            }
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        try {
            String bindingParamKey = "param1";
            boolean isFill = metaObject.getOriginalObject() instanceof BaseDomain;
            if (!isFill) {
                MapperMethod.ParamMap mapperMethod = (MapperMethod.ParamMap) metaObject.getOriginalObject();
                isFill = mapperMethod.containsKey(bindingParamKey) && mapperMethod.get(
                        bindingParamKey) instanceof BaseDomain;
            }
            if (isFill) {
                // 切记切记，这里是filedName，是实体属性字段名，而不是数据库列名
                this.strictUpdateFill(metaObject, "modifyBy", String.class, UserContextUtil.getUserId());
                this.strictUpdateFill(metaObject, "modifyTime", LocalDateTime.class, LocalDateTime.now());
            }
        } catch (Exception exception) {
            log.error("自动填充功能异常", exception);
        }
    }
}
