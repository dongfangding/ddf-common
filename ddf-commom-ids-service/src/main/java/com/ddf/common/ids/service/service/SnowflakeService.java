package com.ddf.common.ids.service.service;

import com.ddf.common.ids.service.model.common.Result;
import com.ddf.common.ids.service.model.common.ResultList;


/**
 *
 * 雪花id业务接口
 *
 * @author dongfang.ding
 * @date 2021/7/20 15:56
 **/
public class SnowflakeService {

    private final IDGen idGen;

    public SnowflakeService(IDGen idGen) {
        this.idGen = idGen;
    }

    /**
     * 获取雪花id
     *
     * @return
     */
    public Result get() {
        return idGen.get(null);
    }


    /**
     * 批量获取雪花id
     *
     * @param length
     * @return
     */
    public ResultList list(int length) {
        return idGen.list(null, length);
    }
}
