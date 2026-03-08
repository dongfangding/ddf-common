package com.ddf.boot.common.api.model.common.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link PageRequest} 测试类
 *
 * @author dongfang.ding
 */
@DisplayName("PageRequest 测试")
class PageRequestTest {

    @Nested
    @DisplayName("默认常量测试")
    class DefaultConstantsTests {

        @Test
        @DisplayName("DEFAULT_PAGE_NUM 应为 1")
        void defaultPageNum_ShouldBeOne() {
            assertThat(PageRequest.DEFAULT_PAGE_NUM).isEqualTo(1);
        }

        @Test
        @DisplayName("DEFAULT_PAGE_SIZE 应为 10")
        void defaultPageSize_ShouldBeTen() {
            assertThat(PageRequest.DEFAULT_PAGE_SIZE).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("isUnPaged 方法测试")
    class IsUnPagedTests {

        @Test
        @DisplayName("默认实现应返回 false")
        void defaultImplementation_ShouldReturnFalse() {
            TestPageRequest request = new TestPageRequest(1, 10);
            assertThat(request.isUnPaged()).isFalse();
        }
    }

    @Nested
    @DisplayName("getPageNumAdaptive 方法测试")
    class GetPageNumAdaptiveTests {

        @Test
        @DisplayName("有值时返回实际值")
        void withValue_ShouldReturnActualValue() {
            TestPageRequest request = new TestPageRequest(5, 10);
            assertThat(request.getPageNumAdaptive()).isEqualTo(5);
        }

        @Test
        @DisplayName("null 时返回默认值")
        void withNull_ShouldReturnDefaultValue() {
            TestPageRequest request = new TestPageRequest(null, 10);
            assertThat(request.getPageNumAdaptive()).isEqualTo(PageRequest.DEFAULT_PAGE_NUM);
        }

        @Test
        @DisplayName("使用默认实现")
        void usingDefaultImplementation_ShouldWork() {
            PageRequest request = PageRequest.DefaultPageRequest.of(3, 10);
            assertThat(request.getPageNumAdaptive()).isEqualTo(3);
            assertThat(request.getPageSize()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("getPageSizeAdaptive 方法测试")
    class GetPageSizeAdaptiveTests {

        @Test
        @DisplayName("有值时返回实际值")
        void withValue_ShouldReturnActualValue() {
            TestPageRequest request = new TestPageRequest(1, 20);
            assertThat(request.getPageSizeAdaptive()).isEqualTo(20);
        }

        @Test
        @DisplayName("null 时返回默认值")
        void withNull_ShouldReturnDefaultValue() {
            TestPageRequest request = new TestPageRequest(1, null);
            assertThat(request.getPageSizeAdaptive()).isEqualTo(PageRequest.DEFAULT_PAGE_SIZE);
        }
    }

    @Nested
    @DisplayName("getStartIndex 方法测试")
    class GetStartIndexTests {

        @Test
        @DisplayName("第一页起始行为0")
        void firstPage_ShouldReturnZero() {
            TestPageRequest request = new TestPageRequest(1, 10);
            assertThat(request.getStartIndex()).isEqualTo(0);
        }

        @Test
        @DisplayName("第二页起始行为10")
        void secondPage_ShouldReturnTen() {
            TestPageRequest request = new TestPageRequest(2, 10);
            assertThat(request.getStartIndex()).isEqualTo(10);
        }

        @Test
        @DisplayName("第三页起始行为20")
        void thirdPage_ShouldReturnTwenty() {
            TestPageRequest request = new TestPageRequest(3, 10);
            assertThat(request.getStartIndex()).isEqualTo(20);
        }

        @Test
        @DisplayName("每页大小为20")
        void pageSizeTwenty_ShouldCalculateCorrectly() {
            TestPageRequest request = new TestPageRequest(3, 20);
            assertThat(request.getStartIndex()).isEqualTo(40);
        }

        @Test
        @DisplayName("页码小于1时修正为1后计算")
        void pageNumLessThanOne_ShouldCalculateFromPageOne() {
            TestPageRequest request = new TestPageRequest(-1, 10);
            assertThat(request.getStartIndex()).isEqualTo(0);
        }

        @Test
        @DisplayName("页大小小于1时返回0")
        void pageSizeLessThanOne_ShouldReturnZero() {
            TestPageRequest request = new TestPageRequest(1, -5);
            assertThat(request.getStartIndex()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("getEndIndex 方法测试")
    class GetEndIndexTests {

        @Test
        @DisplayName("第一页结束行")
        void firstPage_ShouldReturnTen() {
            TestPageRequest request = new TestPageRequest(1, 10);
            assertThat(request.getEndIndex()).isEqualTo(10);
        }

        @Test
        @DisplayName("第二页结束行")
        void secondPage_ShouldReturnTwenty() {
            TestPageRequest request = new TestPageRequest(2, 10);
            assertThat(request.getEndIndex()).isEqualTo(20);
        }

        @Test
        @DisplayName("每页大小为20")
        void pageSizeTwenty_ShouldCalculateCorrectly() {
            TestPageRequest request = new TestPageRequest(2, 20);
            assertThat(request.getEndIndex()).isEqualTo(40);
        }
    }

    @Nested
    @DisplayName("checkArgument 方法测试")
    class CheckArgumentTests {

        @Test
        @DisplayName("有效参数不抛出异常")
        void validParams_ShouldNotThrowException() {
            TestPageRequest request = new TestPageRequest(1, 10);
            request.checkArgument();
        }

        @Test
        @DisplayName("pageNum 为 null 使用默认值，不抛出异常")
        void nullPageNum_ShouldUseDefaultAndNotThrow() {
            TestPageRequest request = new TestPageRequest(null, 10);
            request.checkArgument();
            assertThat(request.getPageNumAdaptive()).isEqualTo(1);
        }

        @Test
        @DisplayName("pageSize 为 null 使用默认值，不抛出异常")
        void nullPageSize_ShouldUseDefaultAndNotThrow() {
            TestPageRequest request = new TestPageRequest(1, null);
            request.checkArgument();
            assertThat(request.getPageSizeAdaptive()).isEqualTo(10);
        }

        @Test
        @DisplayName("两个都为 null 使用默认值，不抛出异常")
        void bothNull_ShouldUseDefaultsAndNotThrow() {
            TestPageRequest request = new TestPageRequest(null, null);
            request.checkArgument();
            assertThat(request.getPageNumAdaptive()).isEqualTo(1);
            assertThat(request.getPageSizeAdaptive()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("DefaultPageRequest 静态工厂测试")
    class DefaultPageRequestTests {

        @Test
        @DisplayName("of 方法创建实例")
        void of_ShouldCreateInstance() {
            PageRequest request = PageRequest.DefaultPageRequest.of(1, 10);

            assertThat(request.getPageNum()).isEqualTo(1);
            assertThat(request.getPageSize()).isEqualTo(10);
        }

        @Test
        @DisplayName("of 方法支持 null 值")
        void of_WithNullValues_ShouldWork() {
            PageRequest request = PageRequest.DefaultPageRequest.of(null, null);

            assertThat(request.getPageNumAdaptive()).isEqualTo(1);
            assertThat(request.getPageSizeAdaptive()).isEqualTo(10);
        }

        @Test
        @DisplayName("DefaultPageRequest 实现 PageRequest 接口")
        void defaultPageRequest_ShouldImplementInterface() {
            PageRequest request = PageRequest.DefaultPageRequest.of(5, 20);

            assertThat(request.getStartIndex()).isEqualTo(80); // (5-1) * 20
            assertThat(request.getEndIndex()).isEqualTo(100); // 80 + 20
        }
    }

    @Nested
    @DisplayName("分页计算集成测试")
    class PaginationCalculationTests {

        @Test
        @DisplayName("标准分页计算")
        void standardPagination_ShouldCalculateCorrectly() {
            TestPageRequest request = new TestPageRequest(3, 15);

            assertThat(request.getPageNumAdaptive()).isEqualTo(3);
            assertThat(request.getPageSizeAdaptive()).isEqualTo(15);
            assertThat(request.getStartIndex()).isEqualTo(30); // (3-1) * 15
            assertThat(request.getEndIndex()).isEqualTo(45); // 30 + 15
        }

        @Test
        @DisplayName("大数据量分页")
        void largeDataPagination_ShouldCalculateCorrectly() {
            TestPageRequest request = new TestPageRequest(1000, 50);

            assertThat(request.getStartIndex()).isEqualTo(49950); // (1000-1) * 50
            assertThat(request.getEndIndex()).isEqualTo(50000); // 49950 + 50
        }

        @Test
        @DisplayName("自定义每页大小")
        void customPageSize_ShouldCalculateCorrectly() {
            TestPageRequest request = new TestPageRequest(1, 100);

            assertThat(request.getStartIndex()).isEqualTo(0);
            assertThat(request.getEndIndex()).isEqualTo(100);
        }
    }

    /**
     * 测试用 PageRequest 实现类
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class TestPageRequest implements PageRequest {
        private Integer pageNum;
        private Integer pageSize;
    }
}
