package com.ddf.boot.common.mvc.util;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.WeakCache;
import com.ddf.boot.common.api.model.common.request.RequestHeaderEnum;
import com.ddf.boot.common.core.helper.SpringContextHolder;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.lang.Nullable;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2024/12/28 19:38
 */
@Slf4j
public class MessageSourceUtil {

    private final static MessageSource MESSAGE_SOURCE;

    private static final WeakCache<String, String> CACHE = CacheUtil.newWeakCache(TimeUnit.DAYS.toMillis(7));

    static {
        MESSAGE_SOURCE = SpringContextHolder.getBeanWithStatic(MessageSource.class);
        if (Objects.isNull(MESSAGE_SOURCE)) {
            log.warn("MessageSource未配置");
        }
    }


    /**
     * 解析message， 适配当前框架
     *
     * @param code
     * @param args
     * @param defaultMessage
     * @return
     */
    public static String getMessage(String code, @Nullable Object[] args, @Nullable String defaultMessage) {
        final String appLanguage = WebUtil.getHeader(RequestHeaderEnum.APP_LANGUAGE.name());
        String defaultLanguage = "en";
        String language = StringUtils.defaultIfBlank(appLanguage, defaultLanguage);
        language = Objects.equals("zh-hans", language) ? "zh-hant" : language;
        return getMessage(code, args, defaultMessage, new Locale(language));
    }


    /**
     * 解析message
     *
     * @param code
     * @param args
     * @param defaultMessage
     * @param locale
     * @return
     */
    public static String getMessage(String code, @Nullable Object[] args, @Nullable String defaultMessage,
            Locale locale) {
        if (Objects.isNull(MESSAGE_SOURCE)) {
            log.error("MessageSource未配置");
            return "";
        }
        String cacheKey = String.format("%s:%s", locale.toLanguageTag(), code);
        if (CACHE.containsKey(cacheKey)) {
            return CACHE.get(cacheKey);
        }
        final String message = MESSAGE_SOURCE.getMessage(code, args, defaultMessage, locale);
        CACHE.put(cacheKey, message);
        return message;
    }
}
