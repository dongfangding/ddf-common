package com.ddf.boot.common.redis.helper;

import com.ddf.boot.common.redis.request.GeoCoordinateSearchRequest;
import com.ddf.boot.common.redis.request.GeoMemberSearchRequest;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.redisson.api.GeoOrder;
import org.redisson.api.GeoPosition;
import org.redisson.api.GeoUnit;
import org.redisson.api.RGeo;
import org.redisson.api.RedissonClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * GeoHelper 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class GeoHelperTest {

    private final RedissonClient redissonClient = Mockito.mock(RedissonClient.class);
    @SuppressWarnings("unchecked")
    private final RGeo<Object> geo = Mockito.mock(RGeo.class);
    private final GeoHelper helper = new GeoHelper(redissonClient);

    @Test
    @DisplayName("应代理获取 geo 对象并支持添加与距离查询")
    void shouldProxyGetAddAndDistanceOperations() {
        when(redissonClient.getGeo("geo:test")).thenReturn(geo);
        when(geo.add(any())).thenReturn(1L);
        when(geo.dist("A", "B", GeoUnit.KILOMETERS)).thenReturn(12.5D);

        RGeo<String> actualGeo = helper.get("geo:test");
        long added = helper.add("geo:test", 120.1D, 30.2D, "A");
        Double dist = helper.dist("geo:test", "A", "B", GeoUnit.KILOMETERS);

        assertSame(geo, actualGeo);
        assertEquals(1L, added);
        assertEquals(12.5D, dist);
    }

    @Test
    @DisplayName("应支持基于坐标的范围查询")
    void shouldSearchByCoordinate() {
        when(redissonClient.getGeo("geo:test")).thenReturn(geo);
        when(geo.search(any())).thenReturn(List.of("A", "B"));
        when(geo.searchWithDistance(any())).thenReturn(Map.of("A", 1.2D));
        GeoPosition position = new GeoPosition(120.1, 30.2);
        when(geo.searchWithPosition(any())).thenReturn(Map.of("A", position));

        GeoCoordinateSearchRequest request = GeoCoordinateSearchRequest.builder()
                .key("geo:test")
                .longitude(120.1D)
                .latitude(30.2D)
                .radius(5D)
                .geoUnit(GeoUnit.KILOMETERS)
                .geoOrder(GeoOrder.ASC)
                .count(10)
                .build();

        assertEquals(List.of("A", "B"), helper.radius(request));
        assertEquals(1.2D, helper.radiusWithDistance(request).get("A"));
        assertEquals(position, helper.radiusWithPosition(request).get("A"));
    }

    @Test
    @DisplayName("应支持基于成员的范围查询和坐标查询")
    void shouldSearchByMemberAndPositionLookup() {
        when(redissonClient.getGeo("geo:test")).thenReturn(geo);
        when(geo.pos("A", "B")).thenReturn(Map.of("A", new GeoPosition(120.1, 30.2)));
        when(geo.search(any())).thenReturn(List.of("C"));
        when(geo.searchWithDistance(any())).thenReturn(Map.of("C", 3.5D));
        when(geo.searchWithPosition(any())).thenReturn(Map.of("C", new GeoPosition(121.0, 31.0)));

        GeoMemberSearchRequest<String> request = GeoMemberSearchRequest.<String>builder()
                .key("geo:test")
                .member("A")
                .radius(10D)
                .geoUnit(GeoUnit.KILOMETERS)
                .geoOrder(GeoOrder.DESC)
                .count(5)
                .build();

        assertEquals(120.1, helper.pos("geo:test", "A", "B").get("A").getLongitude());
        assertEquals(List.of("C"), helper.radius(request));
        assertEquals(3.5D, helper.radiusWithDistance(request).get("C"));
        assertEquals(121.0, helper.radiusWithPosition(request).get("C").getLongitude());
        verify(geo).search(any());
    }
}
