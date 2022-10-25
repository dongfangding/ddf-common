package com.ddf.common.ids.service.util;

import com.ddf.boot.common.core.exception200.BusinessException;
import com.ddf.common.ids.service.enumration.BizCode;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.function.Function;


/**
 * <p>业务性工具类</p >
 *
 * @author YUNTAO
 * @version 1.0
 * @date 2020/12/09 11:33
 */
public class BizUtils {

    /**
     * 生成 id biz tag
     *
     * @param appId
     * @param tag
     * @return
     */
    public static String genIdBizTag(String appId, String tag) {
        return appId + "_" + tag;
    }

    /**
     * 生成id - 批量
     *
     * @param length
     * @param func
     * @return
     */
    public static List<Long> multiIds(Integer length, Function<Integer, Long> func) {
        List<Long> ids = Lists.newArrayList();
        int loop = length;
        while (loop > 0) {
            loop--;
            Long id = func.apply(loop);
            if (id == null) {
                throw new BusinessException(BizCode.GEN_ID_FAILURE);
            }
            ids.add(id);
        }
        return ids;
    }

}
