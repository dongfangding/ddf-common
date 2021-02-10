package com.ddf.boot.common.mybatis.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.ddf.boot.common.core.model.BaseDomain;
import com.ddf.boot.common.jwt.util.JwtUtil;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

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
            log.info("start insert fill ....");
            // 切记切记，这里是filedName，是实体属性字段名，而不是数据库列名
            setFieldValByName("createBy", JwtUtil.getByContextNotNecessary().getUserId(), metaObject);
            setFieldValByName("createTime", new Date(), metaObject);
            setFieldValByName("modifyBy", JwtUtil.getByContextNotNecessary().getUserId(), metaObject);
            setFieldValByName("modifyTime", new Date(), metaObject);
            // 启用乐观锁以后，version并不会自动赋默认值，导致新增的时候对象中没值，如果使用新对象直接获取version来更新，乐观锁会失效，
            // 采用这种方式如果没有值的话，在新增的时候给个默认值
            Object version = metaObject.getValue("version");
            if (null == version) {
                setFieldValByName("version", 1, metaObject);
            }
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        try {
            String bindingParamKey = "param1";
            MapperMethod.ParamMap mapperMethod = (MapperMethod.ParamMap) metaObject.getOriginalObject();
            boolean isMapperMethod = mapperMethod.containsKey(bindingParamKey) && mapperMethod.get(
                    bindingParamKey) instanceof BaseDomain;
            if (metaObject.getOriginalObject() instanceof BaseDomain || isMapperMethod) {
                log.info("start update fill ....");
                // 切记切记，这里是filedName，是实体属性字段名，而不是数据库列名
                setFieldValByName("modifyBy", JwtUtil.getByContextNotNecessary().getUserId(), metaObject);
                setFieldValByName("modifyTime", new Date(), metaObject);
            }
        } catch (Exception ignored) {
        }
    }
}
