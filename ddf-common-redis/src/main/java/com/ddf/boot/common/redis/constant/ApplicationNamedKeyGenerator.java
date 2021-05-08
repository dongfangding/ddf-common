package com.ddf.boot.common.redis.constant;

import com.ddf.boot.common.core.helper.SpringContextHolder;
import com.google.common.base.Joiner;
import reactor.util.annotation.NonNull;

/**
 * <p>key generator</p >
 * 该类提供以模块名命名的风格：applicationName:key
 *
 * @author Mitchell
 * @version 1.0
 * @date 2020/10/24 17:58
 */
public class ApplicationNamedKeyGenerator {

    private static final String KEY_SPLIT = ":";

    private static final Joiner JOINER = Joiner.on(KEY_SPLIT);

    private static final String GLOBAL_NAME = "global";

    private static final String applicationName = SpringContextHolder.getApplicationContext().getEnvironment().getProperty("spring.application.name");

    /**
     * 拼凑key
     *
     * @param keys
     * @return
     */
    public static String genKey(@NonNull String... keys) {
        return genKey(false, keys);
    }

    /**
     * 拼凑key
     *
     * @param ignoreApplicationName 是否忽略applicationName，若忽略则用'global'代替
     * @param keys
     * @return
     */
    public static String genKey(boolean ignoreApplicationName, @NonNull String... keys) {
        String[] params = new String[keys.length + 1];
        params[0] = ignoreApplicationName ? GLOBAL_NAME : applicationName;
        System.arraycopy(keys, 0, params, 1, params.length - 1);
        return JOINER.join(params);
    }

    /**
     * 拼凑key
     *
     * @param keys
     * @return
     */
    public static String genNormalKey(@NonNull String... keys) {
        return JOINER.join(keys);
    }

}
