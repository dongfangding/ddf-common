package com.ddf.boot.common.core.util;

import com.ddf.boot.common.api.util.DateUtils;
import com.ddf.boot.common.core.model.BaseDomain;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2024/07/08 14:10
 */
public class BeanCopierUtilsTest {

    /**
     * 测试单个对象拷贝
     */
    @Test
    public void testSingleObjectCopy() {
        final BaseDomain domain = new BaseDomain();
        domain.setId(1L);
        domain.setGmtCreated(DateUtils.currentTimeSeconds());
        domain.setGmtModified(DateUtils.currentTimeSeconds());

        final BaseDomain copyDomain = BeanCopierUtils.copy(domain, BaseDomain.class);
        Assertions.assertEquals(domain.getId(), copyDomain.getId());
        Assertions.assertEquals(domain.getGmtCreated(), copyDomain.getGmtCreated());
        Assertions.assertEquals(domain.getGmtModified(), copyDomain.getGmtModified());
    }

    /**
     * 测试空对象拷贝
     */
    @Test
    public void testNullObjectCopy() {
        final BaseDomain domain = null;
        final BaseDomain copy = BeanCopierUtils.copy(domain, BaseDomain.class);
        Assertions.assertNull(copy);
    }

    /**
     * 测试集合拷贝
     */
    @Test
    public void testCollectionCopy() {
        final BaseDomain domain = new BaseDomain();
        domain.setId(1L);
        domain.setGmtCreated(DateUtils.currentTimeSeconds());
        domain.setGmtModified(DateUtils.currentTimeSeconds());

        final BaseDomain domain2 = new BaseDomain();
        domain2.setId(2L);
        domain2.setGmtCreated(DateUtils.currentTimeSeconds());
        domain2.setGmtModified(DateUtils.currentTimeSeconds());

        final ArrayList<BaseDomain> originList = Lists.newArrayList(domain, domain2);
        final List<BaseDomain> copyDomain = BeanCopierUtils.copy(originList, BaseDomain.class);
        Assertions.assertIterableEquals(originList, copyDomain);
    }
}
