package com.ddf.boot.common.api.model.common.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link PageResult} 测试类
 *
 * @author dongfang.ding
 */
@DisplayName("PageResult 测试")
class PageResultTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("构造 - 页码和每页大小")
        void constructorWithPageNumAndPageSize_ShouldCreatePageResult() {
            PageResult<String> result = new PageResult<>(1, 10);

            assertThat(result.getPageNum()).isEqualTo(1);
            assertThat(result.getPageSize()).isEqualTo(10);
            assertThat(result.getTotal()).isEqualTo(0);
            assertThat(result.getTotalPage()).isEqualTo(0);
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("构造 - 页码和每页大小带总数")
        void constructorWithAllParams_ShouldCreatePageResult() {
            PageResult<String> result = new PageResult<>(2, 10, 25);

            assertThat(result.getPageNum()).isEqualTo(2);
            assertThat(result.getPageSize()).isEqualTo(10);
            assertThat(result.getTotal()).isEqualTo(25);
            assertThat(result.getTotalPage()).isEqualTo(3); // 25 / 10 = 2.5, 向上取整为 3
        }

        @Test
        @DisplayName("构造 - 完整参数")
        void constructorWithAllFields_ShouldCreatePageResult() {
            List<String> content = Arrays.asList("item1", "item2", "item3");
            PageResult<String> result = new PageResult<>(1, 10, 30, content);

            assertThat(result.getPageNum()).isEqualTo(1);
            assertThat(result.getPageSize()).isEqualTo(10);
            assertThat(result.getTotal()).isEqualTo(30);
            assertThat(result.getTotalPage()).isEqualTo(3);
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getContent()).containsExactlyElementsOf(content);
        }

        @Test
        @DisplayName("构造 - 页码为0应修正为默认值")
        void constructorWithZeroPageNum_ShouldUseDefaultPageNum() {
            PageResult<String> result = new PageResult<>(0, 10);

            assertThat(result.getPageNum()).isEqualTo(1); // DEFAULT_PAGE_NUM
        }

        @Test
        @DisplayName("构造 - 负数页码应修正为1")
        void constructorWithNegativePageNum_ShouldUseDefaultPageNum() {
            PageResult<String> result = new PageResult<>(-5, 10);

            assertThat(result.getPageNum()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("totalPage 静态方法测试")
    class TotalPageMethodTests {

        @Test
        @DisplayName("totalPage - 整除情况")
        void totalPage_Divisible_ShouldCalculateCorrectly() {
            long totalPage = PageResult.totalPage(100, 10);
            assertThat(totalPage).isEqualTo(10);
        }

        @Test
        @DisplayName("totalPage - 非整除情况")
        void totalPage_NotDivisible_ShouldCalculateCorrectly() {
            long totalPage = PageResult.totalPage(25, 10);
            assertThat(totalPage).isEqualTo(3);
        }

        @Test
        @DisplayName("totalPage - 页大小为0")
        void totalPage_ZeroPageSize_ShouldReturnZero() {
            long totalPage = PageResult.totalPage(100, 0);
            assertThat(totalPage).isEqualTo(0);
        }

        @Test
        @DisplayName("totalPage - 总数为0")
        void totalPage_ZeroTotal_ShouldReturnZero() {
            long totalPage = PageResult.totalPage(0, 10);
            assertThat(totalPage).isEqualTo(0);
        }

        @Test
        @DisplayName("totalPage - 总数小于页大小")
        void totalPage_TotalLessThanPageSize_ShouldReturnOne() {
            long totalPage = PageResult.totalPage(5, 10);
            assertThat(totalPage).isEqualTo(1);
        }

        @Test
        @DisplayName("totalPage - 大数计算")
        void totalPage_LargeNumbers_ShouldCalculateCorrectly() {
            long totalPage = PageResult.totalPage(1000000, 100);
            assertThat(totalPage).isEqualTo(10000);
        }
    }

    @Nested
    @DisplayName("isEmpty 方法测试")
    class IsEmptyMethodTests {

        @Test
        @DisplayName("isEmpty - 总数为0应返回true")
        void isEmpty_WithZeroTotal_ShouldReturnTrue() {
            PageResult<String> result = new PageResult<>(1, 10, 0, Collections.emptyList());
            assertThat(result.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("isEmpty - 空内容列表应返回true")
        void isEmpty_WithEmptyContent_ShouldReturnTrue() {
            PageResult<String> result = new PageResult<>(1, 10, 0, Collections.emptyList());
            assertThat(result.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("isEmpty - 有数据应返回false")
        void isEmpty_WithData_ShouldReturnFalse() {
            List<String> content = Collections.singletonList("item");
            PageResult<String> result = new PageResult<>(1, 10, 1, content);
            assertThat(result.isEmpty()).isFalse();
        }
    }

    @Nested
    @DisplayName("分页边界测试")
    class BoundaryTests {

        @Test
        @DisplayName("第一页边界")
        void firstPageBoundary_ShouldWork() {
            PageResult<String> result = new PageResult<>(1, 10, 100, Collections.emptyList());

            assertThat(result.getPageNum()).isEqualTo(1);
            assertThat(result.getTotalPage()).isEqualTo(10);
        }

        @Test
        @DisplayName("最后一页")
        void lastPage_ShouldWork() {
            PageResult<String> result = new PageResult<>(10, 10, 100, Collections.emptyList());

            assertThat(result.getPageNum()).isEqualTo(10);
            assertThat(result.getTotalPage()).isEqualTo(10);
        }

        @Test
        @DisplayName("超出总页数的请求")
        void pageExceedingTotalPage_ShouldWork() {
            PageResult<String> result = new PageResult<>(20, 10, 50, Collections.emptyList());

            assertThat(result.getPageNum()).isEqualTo(20);
            assertThat(result.getTotalPage()).isEqualTo(5);
        }

        @Test
        @DisplayName("单页数据")
        void singlePageData_ShouldWork() {
            List<String> content = Collections.singletonList("only item");
            PageResult<String> result = new PageResult<>(1, 10, 1, content);

            assertThat(result.getTotalPage()).isEqualTo(1);
            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("泛型类型测试")
    class GenericTypeTests {

        @Test
        @DisplayName("String 类型")
        void stringType_ShouldWork() {
            List<String> content = Arrays.asList("a", "b", "c");
            PageResult<String> result = new PageResult<>(1, 10, 3, content);

            assertThat(result.getContent()).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("Integer 类型")
        void integerType_ShouldWork() {
            List<Integer> content = Arrays.asList(1, 2, 3);
            PageResult<Integer> result = new PageResult<>(1, 10, 3, content);

            assertThat(result.getContent()).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("自定义对象类型")
        void customObjectType_ShouldWork() {
            TestItem item1 = new TestItem("id1", "name1");
            TestItem item2 = new TestItem("id2", "name2");
            List<TestItem> content = Arrays.asList(item1, item2);
            PageResult<TestItem> result = new PageResult<>(1, 10, 2, content);

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).getId()).isEqualTo("id1");
            assertThat(result.getContent().get(1).getName()).isEqualTo("name2");
        }

        private static class TestItem {
            private final String id;
            private final String name;

            TestItem(String id, String name) {
                this.id = id;
                this.name = name;
            }

            public String getId() {
                return id;
            }

            public String getName() {
                return name;
            }
        }
    }
}
