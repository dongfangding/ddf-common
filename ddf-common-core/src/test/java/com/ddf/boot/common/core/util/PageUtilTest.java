package com.ddf.boot.common.core.util;

import com.ddf.boot.common.api.model.common.request.PageRequest;
import com.ddf.boot.common.api.model.common.response.PageResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * PageUtil 测试类
 *
 * @author X_Agent
 * @since 2025/01/15
 */
public class PageUtilTest {

    @Test
    @DisplayName("测试 empty - 使用pageNum和pageSize创建空分页")
    public void testEmpty_WithNumAndSize() {
        PageResult<String> result = PageUtil.empty(1, 10);

        Assertions.assertEquals(1, result.getPageNum());
        Assertions.assertEquals(10, result.getPageSize());
        Assertions.assertEquals(0, result.getTotal());
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试 empty - 使用PageRequest创建空分页")
    public void testEmpty_WithPageRequest() {
        PageRequest request = PageRequest.DefaultPageRequest.of(2, 20);
        PageResult<String> result = PageUtil.empty(request);

        Assertions.assertEquals(2, result.getPageNum());
        Assertions.assertEquals(20, result.getPageSize());
        Assertions.assertEquals(0, result.getTotal());
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试 ofPageRequest - 有数据的分页")
    public void testOfPageRequest_WithData() {
        PageRequest request = PageRequest.DefaultPageRequest.of(1, 10);
        List<String> content = Arrays.asList("a", "b", "c");
        PageResult<String> result = PageUtil.ofPageRequest(request, 100, content);

        Assertions.assertEquals(1, result.getPageNum());
        Assertions.assertEquals(10, result.getPageSize());
        Assertions.assertEquals(100, result.getTotal());
        Assertions.assertEquals(3, result.getContent().size());
    }

    @Test
    @DisplayName("测试 ofPageRequest - 不分页查询")
    public void testOfPageRequest_UnPaged() {
        PageRequest request = new PageRequest.DefaultPageRequest(1, 10) {
            @Override
            public Boolean isUnPaged() {
                return true;
            }
        };
        List<String> content = Arrays.asList("a", "b", "c", "d", "e");
        PageResult<String> result = PageUtil.ofPageRequest(request, 5, content);

        Assertions.assertEquals(1, result.getPageNum());
        Assertions.assertEquals(5, result.getTotal());
        Assertions.assertEquals(5, result.getContent().size());
    }

    @Test
    @DisplayName("测试 convertPageResult - 转换分页结果内容")
    public void testConvertPageResult() {
        PageResult<String> original = new PageResult<>(1, 10, 50);
        original.setContent(Arrays.asList("1", "2", "3"));

        PageResult<Integer> converted = PageUtil.<String, Integer>convertPageResult(original,
                list -> list.stream().map(Integer::parseInt).collect(Collectors.toList()));

        Assertions.assertEquals(1, converted.getPageNum());
        Assertions.assertEquals(10, converted.getPageSize());
        Assertions.assertEquals(50, converted.getTotal());
        Assertions.assertEquals(3, converted.getContent().size());
        Assertions.assertEquals(Integer.valueOf(1), converted.getContent().get(0));
        Assertions.assertEquals(Integer.valueOf(2), converted.getContent().get(1));
        Assertions.assertEquals(Integer.valueOf(3), converted.getContent().get(2));
    }

    @Test
    @DisplayName("测试 convertPageResult - 空分页转换")
    public void testConvertPageResult_Empty() {
        PageResult<String> original = PageUtil.empty(1, 10);

        PageResult<Integer> converted = PageUtil.<String, Integer>convertPageResult(original,
                list -> list.stream().map(Integer::parseInt).collect(Collectors.toList()));

        Assertions.assertEquals(1, converted.getPageNum());
        Assertions.assertEquals(10, converted.getPageSize());
        Assertions.assertEquals(0, converted.getTotal());
        Assertions.assertTrue(converted.isEmpty());
    }

    @Test
    @DisplayName("测试 toSpringData - 转换为Spring Data分页")
    public void testToSpringData() {
        PageRequest request = PageRequest.DefaultPageRequest.of(2, 20);
        org.springframework.data.domain.Pageable pageable = PageUtil.toSpringData(request);

        // Spring Data分页从0开始
        Assertions.assertEquals(1, pageable.getPageNumber());
        Assertions.assertEquals(20, pageable.getPageSize());
    }

    @Test
    @DisplayName("测试 toSpringData - 不分页转换")
    public void testToSpringData_UnPaged() {
        PageRequest request = new PageRequest.DefaultPageRequest(1, 10) {
            @Override
            public Boolean isUnPaged() {
                return true;
            }
        };
        org.springframework.data.domain.Pageable pageable = PageUtil.toSpringData(request);

        Assertions.assertTrue(pageable.isUnpaged());
    }

    @Test
    @DisplayName("测试 convertFromSpringData - 从Spring Data转换")
    public void testConvertFromSpringData() {
        List<String> content = Arrays.asList("a", "b", "c");
        org.springframework.data.domain.Page<String> springPage =
                new org.springframework.data.domain.PageImpl<>(content,
                        org.springframework.data.domain.PageRequest.of(1, 10), 50);

        PageResult<String> result = PageUtil.convertFromSpringData(springPage);

        Assertions.assertEquals(1, result.getPageNum());
        Assertions.assertEquals(10, result.getPageSize());
        Assertions.assertEquals(50, result.getTotal());
        Assertions.assertEquals(3, result.getContent().size());
        Assertions.assertEquals("a", result.getContent().get(0));
    }

    @Test
    @DisplayName("测试 convertFromSpringData - 空分页")
    public void testConvertFromSpringData_Empty() {
        org.springframework.data.domain.Page<String> springPage =
                new org.springframework.data.domain.PageImpl<>(
                        Collections.emptyList(),
                        org.springframework.data.domain.PageRequest.of(0, 10),
                        0);

        PageResult<String> result = PageUtil.convertFromSpringData(springPage);

        Assertions.assertEquals(1, result.getPageNum());
        Assertions.assertEquals(10, result.getPageSize());
        Assertions.assertEquals(0, result.getTotal());
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试 PageRequest.DefaultPageRequest - 默认值")
    public void testDefaultPageRequest() {
        PageRequest request = PageRequest.DefaultPageRequest.of(5, 20);

        Assertions.assertEquals(Integer.valueOf(5), request.getPageNum());
        Assertions.assertEquals(Integer.valueOf(20), request.getPageSize());
        Assertions.assertEquals(5, request.getPageNumAdaptive());
        Assertions.assertEquals(20, request.getPageSizeAdaptive());
    }

    @Test
    @DisplayName("测试 PageRequest - null值处理")
    public void testPageRequest_NullHandling() {
        PageRequest request = new PageRequest.DefaultPageRequest(null, null);

        // 默认值应该生效
        Assertions.assertEquals(1, request.getPageNumAdaptive());
        Assertions.assertEquals(10, request.getPageSizeAdaptive());
    }
}
