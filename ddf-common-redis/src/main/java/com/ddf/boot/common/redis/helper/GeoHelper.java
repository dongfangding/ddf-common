package com.ddf.boot.common.redis.helper;

import cn.hutool.core.util.ObjectUtil;
import com.ddf.boot.common.redis.request.GeoCoordinateSearchRequest;
import com.ddf.boot.common.redis.request.GeoMemberSearchRequest;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.redisson.api.GeoEntry;
import org.redisson.api.GeoOrder;
import org.redisson.api.GeoPosition;
import org.redisson.api.GeoUnit;
import org.redisson.api.RGeo;
import org.redisson.api.RedissonClient;
import org.redisson.api.geo.GeoSearchArgs;
import org.redisson.api.geo.OptionalGeoSearch;

/**
 * <p>基于地理空间的的帮助类</p >
 *
 * @author rebot
 * @version 1.0
 * @since 2021/04/23 15:08
 */
public class GeoHelper {
    private final RedissonClient redissonClient;

    public GeoHelper(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 创建GEO对象
     *
     * @param key 目标键
     * @param <V> 值泛型类型
     */
    public <V> RGeo<V> get(String key) {
        return redissonClient.getGeo(key);
    }

    /**
     * 添加地址位置元素
     *
     * @param key 目标键
     * @param longitude 经度
     * @param latitude 纬度
     * @param value 参数值
     */
    public <V> long add(String key, double longitude, double latitude, V value) {
        final RGeo<Object> geo = get(key);
        return geo.add(new GeoEntry(longitude, latitude, value));
    }

    /**
     * 计算两个成员之间的距离
     *
     * @param key 目标键
     * @param firstMember 第一个元素
     * @param secondMember 第二个元素
     * @param geoUnit 距离单位
     * @param <V> 值泛型类型
     */
    public <V> Double dist(String key, V firstMember, V secondMember, GeoUnit geoUnit) {
        return get(key).dist(firstMember, secondMember, geoUnit);
    }

    /**
     * 取出多个成员的定义的地理位置信息, 成员不存在，则返回的map不包含对应的key
     *
     * @param key 目标键
     * @param members 成员列表
     * @param <V> 值泛型类型
     */
    @SafeVarargs
    public final <V> Map<V, GeoPosition> pos(String key, V... members) {
        final RGeo<V> geo = get(key);
        return geo.pos(members);
    }

    /**
     * 返回以给定地址位置计算指定距离半径内满足的地址位置所对应的元素
     *
     * @param request 请求对象
     * @param <V> 值泛型类型
     */
    public <V> List<V> radius(GeoCoordinateSearchRequest request) {
        final RGeo<V> geo = get(request.getKey());
        final OptionalGeoSearch optionalGeoSearch = GeoSearchArgs.from(request.getLongitude(), request.getLatitude())
                .radius(request.getRadius(), request.getGeoUnit());
        applyCountAndOrder(optionalGeoSearch, request.getCount(), request.getGeoOrder());
        return geo.search(optionalGeoSearch);
    }

    /**
     * 返回以给定地址位置计算指定距离半径内满足的地址位置所对应的元素以及元素与指定位置的距离
     * 由于重载方法太多，这里根据参数来决定调用哪个方法
     * key:   元素value
     * value: 与指定位置相距距离
     *
     * @param <V> 值泛型类型
     * @param request 请求对象
     */
    public <V> Map<V, Double> radiusWithDistance(GeoCoordinateSearchRequest request) {
        final RGeo<V> geo = get(request.getKey());
        final OptionalGeoSearch optionalGeoSearch = GeoSearchArgs.from(request.getLongitude(), request.getLatitude())
                .radius(request.getRadius(), request.getGeoUnit());
        applyCountAndOrder(optionalGeoSearch, request.getCount(), request.getGeoOrder());
        return geo.searchWithDistance(optionalGeoSearch);
    }

    /**
     * 返回以给定地址位置计算指定距离半径内满足的地址位置所对应的元素以及各自的地址位置对象
     * key: 元素value
     * value: 所属地理位置对象
     *
     * @param request 请求对象
     * @param <V> 值泛型类型
     */
    public <V> Map<V, GeoPosition> radiusWithPosition(GeoCoordinateSearchRequest request) {
        final RGeo<V> geo = get(request.getKey());
        final OptionalGeoSearch optionalGeoSearch = GeoSearchArgs.from(request.getLongitude(), request.getLatitude())
                .radius(request.getRadius(), request.getGeoUnit());
        applyCountAndOrder(optionalGeoSearch, request.getCount(), request.getGeoOrder());
        return geo.searchWithPosition(optionalGeoSearch);
    }

    /**
     * 返回以给定成员计算指定距离半径内满足的地址位置所对应的元素
     *
     * @param request 请求对象
     * @param <V> 值泛型类型
     */
    public <V> List<V> radius(GeoMemberSearchRequest<V> request) {
        final RGeo<V> geo = get(request.getKey());
        final OptionalGeoSearch optionalGeoSearch = GeoSearchArgs.from(request.getMember()).radius(request.getRadius(),
                request.getGeoUnit());
        applyCountAndOrder(optionalGeoSearch, request.getCount(), request.getGeoOrder());
        return geo.search(optionalGeoSearch);
    }

    /**
     * 返回以给定成员计算指定距离半径内满足的地址位置所对应的元素以及元素与指定位置的距离
     * 由于重载方法太多，这里根据参数来决定调用哪个方法
     * key:   元素value
     * value: 与指定位置相距距离
     *
     * @param <V> 值泛型类型
     * @param request 请求对象
     */
    public <V> Map<V, Double> radiusWithDistance(GeoMemberSearchRequest<V> request) {
        final RGeo<V> geo = get(request.getKey());
        final OptionalGeoSearch optionalGeoSearch = GeoSearchArgs.from(request.getMember()).radius(request.getRadius(),
                request.getGeoUnit());
        applyCountAndOrder(optionalGeoSearch, request.getCount(), request.getGeoOrder());
        return geo.searchWithDistance(optionalGeoSearch);
    }

    /**
     * 返回以给定成员计算指定距离半径内满足的地址位置所对应的元素以及各自的地址位置对象
     * key: 元素value
     * value: 所属地理位置对象
     *
     * @param request 请求对象
     * @param <V> 值泛型类型
     */
    public <V> Map<V, GeoPosition> radiusWithPosition(GeoMemberSearchRequest<V> request) {
        final RGeo<V> geo = get(request.getKey());
        final OptionalGeoSearch optionalGeoSearch = GeoSearchArgs.from(request.getMember()).radius(request.getRadius(),
                request.getGeoUnit());
        applyCountAndOrder(optionalGeoSearch, request.getCount(), request.getGeoOrder());
        return geo.searchWithPosition(optionalGeoSearch);
    }

    /**
     * 将可选的数量和排序条件应用到地理查询中
     *
     * @param search 地理查询对象
     * @param count 查询数量
     * @param order 排序方式
     */
    private void applyCountAndOrder(OptionalGeoSearch search, Integer count, GeoOrder order) {
        if (ObjectUtil.isAllNotEmpty(count, order)) {
            search.order(order).count(count);
        } else if (Objects.nonNull(count)) {
            search.count(count);
        } else if (Objects.nonNull(order)) {
            search.order(order);
        }
    }
}
