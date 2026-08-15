package com.ddf.common.ids.service.service;

import com.ddf.common.ids.service.model.common.Result;
import com.ddf.common.ids.service.model.common.ResultList;
import com.ddf.common.ids.service.model.common.Status;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link IdGenRegistry} 扩展点单元测试：验证 supportsKey=false 的实现（雪花）被过滤，
 * 按 key 分发只命中 key-based 实现。
 */
class IdGenRegistryTest {

    private final IDGen snowflake = mock(IDGen.class);
    private final IDGen segment = mock(IDGen.class);

    IdGenRegistryTest() {
        when(snowflake.supportsKey()).thenReturn(false);
        when(segment.supportsKey()).thenReturn(true);
    }

    private IdGenRegistry newRegistry() {
        return new IdGenRegistry(List.of(snowflake, segment));
    }

    @Test
    @DisplayName("get 只命中 supportsKey=true 的实现，雪花实现被过滤")
    void shouldDispatchGetToKeyBasedImplAndSkipSnowflake() {
        Result expected = new Result("1001", Status.SUCCESS);
        when(segment.get("order")).thenReturn(expected);

        IdGenRegistry registry = newRegistry();

        assertThat(registry.get("order")).isSameAs(expected);
        verify(segment).get("order");
        verify(snowflake, never()).get(anyString());
    }

    @Test
    @DisplayName("list 只命中 supportsKey=true 的实现，雪花实现被过滤")
    void shouldDispatchListToKeyBasedImplAndSkipSnowflake() {
        ResultList expected = new ResultList(List.of("1001", "1002", "1003"), Status.SUCCESS);
        when(segment.list("order", 3)).thenReturn(expected);

        IdGenRegistry registry = newRegistry();

        assertThat(registry.list("order", 3)).isSameAs(expected);
        verify(segment).list("order", 3);
        verify(snowflake, never()).list(anyString(), anyInt());
    }

    @Test
    @DisplayName("find 按类型查找，过滤后返回 key-based 实现而非雪花")
    void shouldFindByTypeAfterFiltering() {
        IdGenRegistry registry = newRegistry();

        // 原始顺序为 [snowflake, segment]，若过滤失效会返回 snowflake
        assertThat(registry.find(IDGen.class)).isSameAs(segment);
    }

    @Test
    @DisplayName("全部实现均被过滤时 get 抛出 IllegalStateException")
    void shouldThrowWhenNoKeyBasedImplAvailable() {
        IdGenRegistry registry = new IdGenRegistry(List.of(snowflake));

        assertThatThrownBy(() -> registry.get("order")).isInstanceOf(IllegalStateException.class);
    }
}
