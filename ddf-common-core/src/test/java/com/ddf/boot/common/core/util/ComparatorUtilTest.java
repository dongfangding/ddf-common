package com.ddf.boot.common.core.util;

import com.ddf.boot.common.core.model.IndexComparatorElement;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * ComparatorUtil 测试类
 *
 * @author X_Agent
 * @since 2025/01/15
 */
public class ComparatorUtilTest {

    @Test
    @DisplayName("测试 compareDifference - 两个空字符串")
    public void testCompareDifference_BothEmpty() {
        List<IndexComparatorElement> result = ComparatorUtil.compareDifference("", "");
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试 compareDifference - old为空，new不为空")
    public void testCompareDifference_OldEmpty() {
        List<IndexComparatorElement> result = ComparatorUtil.compareDifference("", "a,b,c");
        Assertions.assertEquals(3, result.size());
        for (IndexComparatorElement element : result) {
            Assertions.assertTrue(element.isAdd());
        }
    }

    @Test
    @DisplayName("测试 compareDifference - new为空，old不为空")
    public void testCompareDifference_NewEmpty() {
        List<IndexComparatorElement> result = ComparatorUtil.compareDifference("a,b,c", "");
        Assertions.assertEquals(3, result.size());
        for (IndexComparatorElement element : result) {
            Assertions.assertTrue(element.isDelete());
        }
    }

    @Test
    @DisplayName("测试 compareDifference - 相同字符串")
    public void testCompareDifference_Same() {
        List<IndexComparatorElement> result = ComparatorUtil.compareDifference("a,b,c", "a,b,c");
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试 compareDifference - 新增、删除混合")
    public void testCompareDifference_Mixed() {
        List<IndexComparatorElement> result = ComparatorUtil.compareDifference("a,b,c", "b,c,d");
        Assertions.assertEquals(2, result.size());
        long addCount = result.stream().filter(IndexComparatorElement::isAdd).count();
        long delCount = result.stream().filter(IndexComparatorElement::isDelete).count();
        Assertions.assertEquals(1, addCount);
        Assertions.assertEquals(1, delCount);
    }

    @Test
    @DisplayName("测试 compareDifference - 处理重复值")
    public void testCompareDifference_DuplicateValues() {
        List<IndexComparatorElement> result = ComparatorUtil.compareDifference("a,b,c", "a,c");
        Assertions.assertEquals(1, result.size());
        Assertions.assertTrue(result.get(0).isDelete());
    }

    @Test
    @DisplayName("测试 compareWithWeakReplaceLast - 两个空字符串")
    public void testCompareWithWeakReplaceLast_BothEmpty() {
        List<IndexComparatorElement> result = ComparatorUtil.compareWithWeakReplaceLast("", "");
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试 compareWithWeakReplaceLast - old为空")
    public void testCompareWithWeakReplaceLast_OldEmpty() {
        List<IndexComparatorElement> result = ComparatorUtil.compareWithWeakReplaceLast("", "a,b,c");
        Assertions.assertEquals(3, result.size());
        for (IndexComparatorElement element : result) {
            Assertions.assertTrue(element.isAdd());
        }
    }

    @Test
    @DisplayName("测试 compareWithWeakReplaceLast - new为空")
    public void testCompareWithWeakReplaceLast_NewEmpty() {
        List<IndexComparatorElement> result = ComparatorUtil.compareWithWeakReplaceLast("a,b,c", "");
        Assertions.assertEquals(3, result.size());
        for (IndexComparatorElement element : result) {
            Assertions.assertTrue(element.isDelete());
        }
    }

    @Test
    @DisplayName("测试 compareWithWeakReplaceLast - 完全相同")
    public void testCompareWithWeakReplaceLast_Same() {
        List<IndexComparatorElement> result = ComparatorUtil.compareWithWeakReplaceLast("a,b,c", "a,b,c");
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试 compareWithWeakReplaceLast - 完全不同")
    public void testCompareWithWeakReplaceLast_Different() {
        List<IndexComparatorElement> result = ComparatorUtil.compareWithWeakReplaceLast("a,b,c", "d,e,f");
        // 由于索引相同，方法会将不同元素识别为替换（weak replacement detection）
        Assertions.assertEquals(3, result.size());
        long changeCount = result.stream().filter(IndexComparatorElement::isChange).count();
        Assertions.assertEquals(3, changeCount);
    }

    @Test
    @DisplayName("测试 compareWithWeakReplaceLast - 替换场景")
    public void testCompareWithWeakReplaceLast_Replace() {
        List<IndexComparatorElement> result = ComparatorUtil.compareWithWeakReplaceLast("a,b,c,q,x,y,z", "d,b,e,f,z");
        Assertions.assertEquals(5, result.size());

        long changeCount = result.stream().filter(IndexComparatorElement::isChange).count();
        long delCount = result.stream().filter(IndexComparatorElement::isDelete).count();
        Assertions.assertEquals(3, changeCount);
        Assertions.assertEquals(2, delCount);
    }

    @Test
    @DisplayName("测试 compareWithWeakReplaceLast - 部分相同")
    public void testCompareWithWeakReplaceLast_PartialSame() {
        List<IndexComparatorElement> result = ComparatorUtil.compareWithWeakReplaceLast("a,b,c", "a,x,c,q");
        // a和c在相同位置，b被x替换，q是新增
        Assertions.assertEquals(2, result.size());
        // 验证有一个替换和一个新增
        long changeCount = result.stream().filter(IndexComparatorElement::isChange).count();
        long addCount = result.stream().filter(IndexComparatorElement::isAdd).count();
        Assertions.assertEquals(1, changeCount);
        Assertions.assertEquals(1, addCount);
    }

    @Test
    @DisplayName("测试 replaceSplitValueIfMatch - 空字符串")
    public void testReplaceSplitValueIfMatch_Empty() {
        String result = ComparatorUtil.replaceSplitValueIfMatch("", "a", "b");
        Assertions.assertEquals("b", result);
    }

    @Test
    @DisplayName("测试 replaceSplitValueIfMatch - 匹配替换")
    public void testReplaceSplitValueIfMatch_Replace() {
        String result = ComparatorUtil.replaceSplitValueIfMatch("a,b,c,d", "b", "z");
        Assertions.assertEquals("a,z,c,d", result);
    }

    @Test
    @DisplayName("测试 replaceSplitValueIfMatch - 不匹配")
    public void testReplaceSplitValueIfMatch_NoMatch() {
        String result = ComparatorUtil.replaceSplitValueIfMatch("a,b,c,d", "x", "z");
        Assertions.assertEquals("a,b,c,d", result);
    }

    @Test
    @DisplayName("测试 replaceSplitValueIfMatch - 替换多个匹配项")
    public void testReplaceSplitValueIfMatch_MultipleMatches() {
        String result = ComparatorUtil.replaceSplitValueIfMatch("a,b,b,c", "b", "z");
        Assertions.assertEquals("a,z,z,c", result);
    }

    @Test
    @DisplayName("测试 removeSplitValueIfMatch - 单个元素匹配删除")
    public void testRemoveSplitValueIfMatch_Single() {
        String result = ComparatorUtil.removeSplitValueIfMatch("a,b,c,d", "b");
        Assertions.assertEquals("a,c,d", result);
    }

    @Test
    @DisplayName("测试 removeSplitValueIfMatch - 批量删除")
    public void testRemoveSplitValueIfMatch_Batch() {
        String result = ComparatorUtil.removeSplitValueIfMatch("a,b,c,d,b", java.util.Set.of("a", "c"));
        Assertions.assertEquals("b,d,b", result);
    }

    @Test
    @DisplayName("测试 removeSplitValueIfMatch - 删除所有")
    public void testRemoveSplitValueIfMatch_RemoveAll() {
        String result = ComparatorUtil.removeSplitValueIfMatch("a,b,c", java.util.Set.of("a", "b", "c"));
        Assertions.assertEquals("", result);
    }

    @Test
    @DisplayName("测试 removeSplitValueIfMatch - 不匹配")
    public void testRemoveSplitValueIfMatch_NoMatch() {
        String result = ComparatorUtil.removeSplitValueIfMatch("a,b,c", "x");
        Assertions.assertEquals("a,b,c", result);
    }
}
