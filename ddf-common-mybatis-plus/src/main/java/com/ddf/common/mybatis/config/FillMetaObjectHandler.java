package com.ddf.common.mybatis.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.ddf.common.entity.BaseDomain;
import com.ddf.common.jwt.exception.UserClaimMissionException;
import com.ddf.common.jwt.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 针对通用字段自动填充功能的实现
 *
 * 实现对通用实体字段的赋值，
 *
 * @author dongfang.ding
 * @date 2019/5/22 17:15
 */
@Slf4j
@Component
public class FillMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        if (metaObject.getOriginalObject() instanceof BaseDomain) {
            log.info("start insert fill ....");
            // 切记切记，这里是filedName，是实体属性字段名，而不是数据库列名
            try {
                setInsertFieldValByName("createBy", JwtUtil.getByContext().getUserId(), metaObject);
            } catch (UserClaimMissionException ignored) {}
            setInsertFieldValByName("createTime", new Date(), metaObject);
            try {
                setInsertFieldValByName("modifyBy", JwtUtil.getByContext().getUserId(), metaObject);
            } catch (UserClaimMissionException ignored) {}
            setInsertFieldValByName("modifyTime", new Date(), metaObject);
            // 启用乐观锁以后，version并不会自动赋默认值，导致新增的时候对象中没值，如果使用新对象直接获取version来更新，乐观锁会失效，
            // 采用这种方式如果没有值的话，在新增的时候给个默认值
            Object version = metaObject.getValue("version");
            if (null == version) {
                setInsertFieldValByName("version", 1, metaObject);
            }
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        Object mapperMethod = ((MapperMethod.ParamMap) metaObject.getOriginalObject()).get("param1");
        if (mapperMethod instanceof BaseDomain) {
            log.info("start update fill ....");
            // 切记切记，这里是filedName，是实体属性字段名，而不是数据库列名
            try {
                setUpdateFieldValByName("modifyBy", JwtUtil.getByContext().getUserId(), metaObject);
            } catch (UserClaimMissionException ignored) {}
            setUpdateFieldValByName("modifyTime", new Date(), metaObject);
        }
    }
}
