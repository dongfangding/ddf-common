package com.ddf.common.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ddf.common.entity.BaseDomain;

/**
 * 自定义的基础mapper,实体必须继承BaseDomain，方便封装一些统一操作
 *
 * @author dongfang.ding
 * @date 2019/12/7 0007 22:32
 **/
public interface CustomizeBaseMapper<T extends BaseDomain> extends BaseMapper<T> {


}
