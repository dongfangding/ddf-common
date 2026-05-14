package com.ddf.boot.common.core.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * <em>Description</em>
 *
 * @author zero
 * @since 2023/12/10 11:03
 */
public class BillNoUtil {

    final static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssS");

    /**
     * 生成订单
     *
     * @param prefix 前缀
     * @param code 标识
     * @return 订单号
     */
    public static synchronized String generationBillNo(String prefix, long code) {
        int i = ThreadLocalRandom.current().nextInt(10000, 99999);
        return String.join("", prefix, FORMATTER.format(LocalDateTime.now()), String.valueOf(code), String.valueOf(i));
    }
}
