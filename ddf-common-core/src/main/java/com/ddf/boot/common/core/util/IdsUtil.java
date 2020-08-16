package com.ddf.boot.common.core.util;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.ddf.boot.common.core.config.GlobalProperties;

/**
 * 本地雪花id生成器
 *
 * 这个默认word_id和center_id都是1
 * 可以配置，但集群时需要保证每台机器不一致
 *
 * 因此这个只作为一个小工具使用
 * 如果需要保证全局唯一，不想引入别的依赖，可以给每台机器配置不桶的word_id和center_id
 * {@link GlobalProperties}
 *
 * 如果不想使用，还是要借助工具 如https://github.com/Meituan-Dianping/Leaf
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
