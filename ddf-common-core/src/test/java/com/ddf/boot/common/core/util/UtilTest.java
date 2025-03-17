package com.ddf.boot.common.core.util;

import org.junit.jupiter.api.Test;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2024/07/08 14:09
 */
public class UtilTest {


    @Test
    public void testCityUtil() {
        System.out.println(CityUtil.getProvinceByCity("丹东市"));
        System.out.println(CityUtil.getProvinceByCity("亳州市"));
    }
}
