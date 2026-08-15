package com.ddf.boot.common.redis.constant;

import com.ddf.boot.common.core.helper.SpringContextHolder;
import com.google.common.base.Joiner;
import java.util.Optional;
import reactor.util.annotation.NonNull;

/**
 * <p>key generator</p >
 * 该类提供以模块名命名的风格：applicationName:key
 *
 * @author ropbot
 */
public class ApplicationNamedKeyGenerator {

    private static final String KEY_SPLIT = ":";

    private static final Joiner JOINER = Joiner.on(KEY_SPLIT);

    private static final String GLOBAL_NAME = "global";

    private static String getApplicationName() {
        return Holder.APPLICATION_NAME;
    }

    private static class Holder {
        // 注意：未配置 spring.application.name 时统一回退为 "unknown"，多应用共享同一 Redis 时会导致 key 前缀冲突。
        // 接入方应显式配置 spring.application.name 以保证 key 隔离。
        private static final String APPLICATION_NAME = Optional.ofNullable(SpringContextHolder.getApplicationContext())
                .map(ctx -> ctx.getEnvironment().getProperty("spring.application.name"))
                .orElse("unknown");
    }

    /**
     * 拼凑key
     *
     * @param keys 键集合
     */
    public static String genKey(@NonNull String... keys) {
        return genKey(false, keys);
    }

    /**
     * 拼凑key
     *
     * @param ignoreApplicationName 是否忽略applicationName，若忽略则用'global'代替
     * @param keys 键集合
     */
    public static String genKey(boolean ignoreApplicationName, @NonNull String... keys) {
        String[] params = new String[keys.length + 1];
        params[0] = ignoreApplicationName ? GLOBAL_NAME : getApplicationName();
        System.arraycopy(keys, 0, params, 1, params.length - 1);
        return JOINER.join(params);
    }

    /**
     * 拼凑key
     *
     * @param keys 键集合
     */
    public static String genNormalKey(@NonNull String... keys) {
        return JOINER.join(keys);
    }

}
