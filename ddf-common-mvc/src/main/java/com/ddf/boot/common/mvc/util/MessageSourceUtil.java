package com.ddf.boot.common.mvc.util;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.WeakCache;
import com.ddf.boot.common.api.model.common.request.RequestHeaderEnum;
import com.ddf.boot.common.api.util.JsonUtil;
import com.ddf.boot.common.core.helper.SpringContextHolder;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
 * @since 2024/12/28 19:38
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

    private final static List<String> supportedLanguages;

    static {
        supportedLanguages = List.of("ar", "bn", "en", "es", "fil", "fr", "hi", "id", "ja", "ko", "pt", "vi", "zh-Hant",
                "zh-Hans"
        );
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
        String cacheKey = "%s:%s".formatted(locale.toString(), code);
        if (Objects.isNull(args) || args.length == 0) {
            if (CACHE.containsKey(cacheKey)) {
                return CACHE.get(cacheKey);
            }
        }

        final String message = MESSAGE_SOURCE.getMessage(code, args, defaultMessage, locale);
        CACHE.put(cacheKey, message);
        return message;
    }


    /**
     * 获取一个code的所有语言版本
     *
     * @param code
     * @param args
     * @param defaultMessage
     * @return
     */
    public static Map<String, String> getAllMessages(String code, @Nullable Object[] args,
            @Nullable String defaultMessage) {
        Map<String, String> messages = new HashMap<>();
        for (String locale : supportedLanguages) {
            String message = getMessage(code, args, defaultMessage, new Locale(locale));
            messages.put(locale, message);
        }
        return messages;
    }

    /**
     * 获取一个code的所有语言版本
     *
     * @param code
     * @return
     */
    public static Map<String, String> getAllMessages(String code) {
        Map<String, String> messages = new HashMap<>();
        for (String locale : supportedLanguages) {
            String message = getMessage(code, null, "", new Locale(locale));
            messages.put(locale, message);
        }
        return messages;
    }

    /**
     * 获取一个code的所有语言版本
     *
     * @param code
     * @param args
     * @param defaultMessage
     * @return
     */
    public static String getAllMessagesJson(String code, @Nullable Object[] args, @Nullable String defaultMessage) {
        Map<String, String> messages = getAllMessages(code, args, defaultMessage);
        return JsonUtil.toJson(messages);
    }

    /**
     * 获取一个code的所有语言版本
     *
     * @param code
     * @return
     */
    public static String getAllMessagesJson(String code) {
        return getAllMessagesJson(code, null, "");
    }
}
