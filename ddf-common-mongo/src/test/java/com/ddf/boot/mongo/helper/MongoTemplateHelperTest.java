package com.ddf.boot.mongo.helper;

import com.ddf.boot.common.api.model.common.request.PageRequest;
import com.ddf.boot.common.api.model.common.response.PageResult;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MongoTemplateHelper 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class MongoTemplateHelperTest {

    private final MongoTemplate mongoTemplate = Mockito.mock(MongoTemplate.class);
    private final MongoTemplateHelper helper = new MongoTemplateHelper(mongoTemplate);

    @Test
    @DisplayName("查询为空时应返回空分页结果")
    void shouldReturnEmptyPageResultWhenCountIsZero() {
        Query query = new Query();
        PageRequest pageRequest = PageRequest.DefaultPageRequest.of(2, 3);
        when(mongoTemplate.count(query, DemoPo.class)).thenReturn(0L);

        PageResult<DemoPo> result = helper.handlerPageResult(pageRequest, query, DemoPo.class);

        assertTrue(result.isEmpty());
        assertEquals(2, result.getPageNum());
        assertEquals(3, result.getPageSize());
        verify(mongoTemplate).count(query, DemoPo.class);
    }

    @Test
    @DisplayName("同类型分页结果应直接返回原列表")
    void shouldReturnOriginalListForSameTypePageResult() {
        Query query = new Query();
        PageRequest pageRequest = PageRequest.DefaultPageRequest.of(2, 3);
        List<DemoPo> dbList = List.of(new DemoPo("A"), new DemoPo("B"));
        when(mongoTemplate.count(query, DemoPo.class)).thenReturn(5L);
        when(mongoTemplate.find(query, DemoPo.class)).thenReturn(dbList);

        PageResult<DemoPo> result = helper.handlerPageResult(pageRequest, query, DemoPo.class);

        assertEquals(5L, result.getTotal());
        assertEquals(2, result.getContent().size());
        assertSame(dbList, result.getContent());
        assertEquals(3, query.getSkip());
        assertEquals(3, query.getLimit());
    }

    @Test
    @DisplayName("不同类型分页结果应完成对象转换")
    void shouldConvertPageResultWhenVoClassDiffers() {
        Query query = new Query();
        PageRequest pageRequest = PageRequest.DefaultPageRequest.of(1, 2);
        when(mongoTemplate.count(query, DemoPo.class)).thenReturn(2L);
        when(mongoTemplate.find(query, DemoPo.class)).thenReturn(List.of(new DemoPo("A"), new DemoPo("B")));

        PageResult<DemoVo> result = helper.handlerPageResult(pageRequest, query, DemoPo.class, DemoVo.class);

        assertEquals(2L, result.getTotal());
        assertEquals(2, result.getContent().size());
        assertEquals("A", result.getContent().get(0).getName());
        assertEquals("B", result.getContent().get(1).getName());
    }

    @Test
    @DisplayName("SpringData 分页为空时应返回空 Page")
    void shouldReturnEmptySpringDataPageWhenCountIsZero() {
        Query query = new Query();
        PageRequest pageRequest = PageRequest.DefaultPageRequest.of(1, 5);
        when(mongoTemplate.count(query, DemoPo.class)).thenReturn(0L);

        Page<DemoPo> page = helper.handlerPage(pageRequest, query, DemoPo.class);

        assertTrue(page.getContent().isEmpty());
        assertEquals(0L, page.getTotalElements());
        verify(mongoTemplate).count(query, DemoPo.class);
    }

    @Test
    @DisplayName("SpringData 分页应支持对象转换")
    void shouldConvertSpringDataPageWhenVoClassDiffers() {
        Query query = new Query();
        PageRequest pageRequest = PageRequest.DefaultPageRequest.of(2, 2);
        when(mongoTemplate.count(query, DemoPo.class)).thenReturn(4L);
        when(mongoTemplate.find(query, DemoPo.class)).thenReturn(List.of(new DemoPo("A"), new DemoPo("B")));

        Page<DemoVo> page = helper.handlerPage(pageRequest, query, DemoPo.class, DemoVo.class);

        assertEquals(4L, page.getTotalElements());
        assertEquals(2, page.getContent().size());
        assertEquals("A", page.getContent().get(0).getName());
        assertEquals("B", page.getContent().get(1).getName());
        assertEquals(2, page.getSize());
        assertEquals(1, page.getNumber());
    }

    public static class DemoPo {
        private String name;

        public DemoPo() {
        }

        public DemoPo(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }


    public static class DemoVo {
        private String name;

        public DemoVo() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
