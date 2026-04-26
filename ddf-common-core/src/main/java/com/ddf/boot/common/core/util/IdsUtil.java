package com.ddf.boot.common.core.util;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.ddf.boot.common.core.config.GlobalProperties;
import com.ddf.boot.common.core.helper.SpringContextHolder;
import java.util.UUID;

/**
 * 本地雪花id生成器
 * <p>
 * 这个默认word_id和center_id都是1
 * 可以配置，但集群时需要保证每台机器不一致
 * <p>
 * 因此这个只作为一个小工具使用
 * 如果需要保证全局唯一，不想引入别的依赖，可以给每台机器配置不桶的word_id和center_id
 * {@link GlobalProperties}
 * <p>
 * 如果不想使用，还是要借助工具 如https://github.com/Meituan-Dianping/Leaf
 *
 * @author dongfang.ding
 * @since 2019/12/9 0009 11:35
 */
public class IdsUtil {

    private static final GlobalProperties GLOBAL_PROPERTIES = SpringContextHolder.getBeanWithStatic(GlobalProperties.class);

    /**
     * 获取string格式的id
     *
     * @return void
     * @since 2019/12/9 0009 11:38
     **/
    public static String getNextStrId() {
        return Long.toString(getNextLongId());
    }

    /**
     * 获取long类型的id
     *
     * @return void
     * @since 2019/12/9 0009 11:39
     **/
    public static long getNextLongId() {
        long workId = 0;
        long datacenterId = 0;
        if (GLOBAL_PROPERTIES != null) {
            workId = GLOBAL_PROPERTIES.getSnowflakeWorkerId();
            datacenterId = GLOBAL_PROPERTIES.getSnowflakeDataCenterId();
        }
        Snowflake snowflake = IdUtil.getSnowflake(workId, datacenterId);
        return snowflake.nextId();
    }


    /**
     * 在雪花的基础上附加更加长的字符串id
     *
     * @return 字符串格式的id
     * @since 2019/12/9 0009 11:38
     **/
    public static String getUniqueId() {
        return  IdsUtil.getNextStrId() + RandomUtil.randomString(32);
    }
}
