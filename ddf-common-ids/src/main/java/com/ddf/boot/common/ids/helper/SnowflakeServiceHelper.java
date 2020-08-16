package com.ddf.boot.common.ids.helper;

import com.ddf.boot.common.ids.exception.SnowflakeException;
import com.sankuai.inf.leaf.common.Result;
import com.sankuai.inf.leaf.common.Status;
import com.sankuai.inf.leaf.service.SnowflakeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;

/**
 * 获取雪花id帮助类$
 *
 * @author dongfang.ding
 * @date 2020/8/15 0015 17:53
 */
@DependsOn("snowflakeService")
public class SnowflakeServiceHelper {

    @Autowired
    private SnowflakeService snowflakeService;

    private ThreadLocal<Integer> tryTimes = ThreadLocal.withInitial(() -> 0);

    private final Integer MAX_TRY_TIMES = 3;


    /**
     * 获取雪花id， 失败后有三次尝试次数
     * @return
     */
    public long getLongId() {
        Result result = snowflakeService.getId(null);
        if (!Status.SUCCESS.equals(result.getStatus())) {
            if (tryTimes.get() > MAX_TRY_TIMES) {
                throw new SnowflakeException("获取雪花id异常，状态码: " + result.getId());
            }
            tryTimes.set(tryTimes.get() + 1);
            return getLongId();
        }
        return result.getId();
    }


}
