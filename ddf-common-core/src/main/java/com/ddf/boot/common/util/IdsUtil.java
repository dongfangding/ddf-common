package com.ddf.boot.common.util;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.ddf.boot.common.config.GlobalProperties;

/**
 * ID生成器$
 *
 * @author dongfang.ding
 * @date 2019/12/9 0009 11:35
 */
public class IdsUtil {

    private static GlobalProperties globalProperties = SpringContextHolder.getBean(GlobalProperties.class);

    /**
     * 获取string格式的id
     *
     * @return void
     * @author dongfang.ding
     * @date 2019/12/9 0009 11:38
     **/
    public static String getNextStrId() {
        Snowflake snowflake = IdUtil.getSnowflake(globalProperties.getSnowflakeWorkerId(),
                globalProperties.getSnowflakeDataCenterId());
        return snowflake.nextIdStr();
    }

    /**
     * 获取long类型的id
     * @return void
     * @author dongfang.ding
     * @date 2019/12/9 0009 11:39
     **/
    public static long getNextLongId() {
        Snowflake snowflake = IdUtil.getSnowflake(globalProperties.getSnowflakeWorkerId(),
                globalProperties.getSnowflakeDataCenterId());
        return snowflake.nextId();
    }

}
