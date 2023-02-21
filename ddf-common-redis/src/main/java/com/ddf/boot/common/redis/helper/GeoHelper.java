package com.ddf.boot.common.redis.helper;

import cn.hutool.core.util.ObjectUtil;
import com.ddf.boot.common.redis.request.GeoCoordinateSearchRequest;
import com.ddf.boot.common.redis.request.GeoMemberSearchRequest;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.redisson.Redisson;
import org.redisson.api.GeoEntry;
import org.redisson.api.GeoOrder;
import org.redisson.api.GeoPosition;
import org.redisson.api.GeoUnit;
import org.redisson.api.RGeo;
import org.redisson.api.RedissonClient;
import org.redisson.api.geo.GeoSearchArgs;
import org.redisson.api.geo.OptionalGeoSearch;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;

/**
 * <p>基于地理空间的的帮助类</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/04/23 15:08
 */
public class GeoHelper {

    private final RedissonClient redissonClient;

    public GeoHelper(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 创建GEO对象
     *
     * @param key
     * @param <V>
     * @return
     */
    public <V> RGeo<V> get(String key) {
        return redissonClient.getGeo(key);
    }

    /**
     * 添加地址位置元素
     *
     * @param key       key
     * @param longitude 经度
     * @param latitude  维度
     * @param value     要添加的元素
     */
    public <V> long add(String key, double longitude, double latitude, V value) {
        final RGeo<Object> geo = get(key);
        return geo.add(new GeoEntry(longitude, latitude, value));
    }

    /**
     * 计算两个成员之间的距离
     *
     * @param key          key
     * @param firstMember  第一个元素
     * @param secondMember 第二个元素
     * @param geoUnit      距离单位
     * @param <V>
     * @return
     */
    public <V> Double dist(String key, V firstMember, V secondMember, GeoUnit geoUnit) {
        return get(key).dist(firstMember, secondMember, geoUnit);
    }

    /**
     * 取出多个成员的定义的地理位置信息, 成员不存在，则返回的map不包含对应的key
     *
     * @param key     key
     * @param members 要取出的成员列表
     * @param <V>
     * @return
     */
    @SafeVarargs
    public final <V> Map<V, GeoPosition> pos(String key, V... members) {
        final RGeo<V> geo = get(key);
        return geo.pos(members);
    }

    /**
     *
     * 返回以给定地址位置计算指定距离半径内满足的地址位置所对应的元素
     *
     * @param request
     * @param <V>
     * @return
     */
    public <V> List<V> radius(GeoCoordinateSearchRequest request) {
        // PreconditionUtil.requiredParamCheck(request);
        final RGeo<V> geo = get(request.getKey());
        final OptionalGeoSearch optionalGeoSearch = GeoSearchArgs.from(request.getLongitude(), request.getLatitude())
                .radius(request.getRadius(), request.getGeoUnit());
        if (ObjectUtil.isAllNotEmpty(request.getCount(), request.getGeoOrder())) {
            optionalGeoSearch.order(request.getGeoOrder()).count(request.getCount());
        } else if (Objects.nonNull(request.getCount())) {
            optionalGeoSearch.count(request.getCount());
        } else if (Objects.nonNull(request.getGeoOrder())) {
            optionalGeoSearch.order(request.getGeoOrder());
        }
        return geo.search(optionalGeoSearch);
    }

    /**
     * 返回以给定地址位置计算指定距离半径内满足的地址位置所对应的元素以及元素与指定位置的距离
     *
     * 由于重载方法太多，这里根据参数来决定调用哪个方法
     *
     * key:   元素value
     * value: 与指定位置相距距离
     *
     * @param <V>
     * @return
     */
    public <V> Map<V, Double> radiusWithDistance(GeoCoordinateSearchRequest request) {
        // PreconditionUtil.requiredParamCheck(request);
        final RGeo<V> geo = get(request.getKey());
        final OptionalGeoSearch optionalGeoSearch = GeoSearchArgs.from(request.getLongitude(), request.getLatitude())
                .radius(request.getRadius(), request.getGeoUnit());
        if (ObjectUtil.isAllNotEmpty(request.getCount(), request.getGeoOrder())) {
            optionalGeoSearch.order(request.getGeoOrder())
                    .count(request.getCount());
        } else if (Objects.nonNull(request.getCount())) {
            optionalGeoSearch.count(request.getCount());
        } else if (Objects.nonNull(request.getGeoOrder())) {
            optionalGeoSearch.order(request.getGeoOrder());
        }
        return geo.searchWithDistance(optionalGeoSearch);
    }

    /**
     * 返回以给定地址位置计算指定距离半径内满足的地址位置所对应的元素以及各自的地址位置对象
     *
     * key: 元素value
     * value: 所属地理位置对象
     *
     * @param request
     * @param <V>
     * @return
     */
    public <V> Map<V, GeoPosition> radiusWithPosition(GeoCoordinateSearchRequest request) {
        // PreconditionUtil.requiredParamCheck(request);
        final RGeo<V> geo = get(request.getKey());
        final OptionalGeoSearch optionalGeoSearch = GeoSearchArgs.from(request.getLongitude(), request.getLatitude())
                .radius(request.getRadius(), request.getGeoUnit());
        if (ObjectUtil.isAllNotEmpty(request.getCount(), request.getGeoOrder())) {
            optionalGeoSearch.order(request.getGeoOrder())
                    .count(request.getCount());
        } else if (Objects.nonNull(request.getCount())) {
            optionalGeoSearch.count(request.getCount());
        } else if (Objects.nonNull(request.getGeoOrder())) {
            optionalGeoSearch.order(request.getGeoOrder());
        }
        return geo.searchWithPosition(optionalGeoSearch);
    }

    /**
     *
     * 返回以给定成员计算指定距离半径内满足的地址位置所对应的元素
     *
     * @param request
     * @param <V>
     * @return
     */
    public <V> List<V> radius(GeoMemberSearchRequest<V> request) {
        // PreconditionUtil.requiredParamCheck(request);
        final RGeo<V> geo = get(request.getKey());
        final OptionalGeoSearch optionalGeoSearch = GeoSearchArgs.from(request.getMember())
                .radius(request.getRadius(), request.getGeoUnit());
        if (ObjectUtil.isAllNotEmpty(request.getCount(), request.getGeoOrder())) {
            optionalGeoSearch.order(request.getGeoOrder())
                    .count(request.getCount());
        } else if (Objects.nonNull(request.getCount())) {
            optionalGeoSearch.count(request.getCount());
        } else if (Objects.nonNull(request.getGeoOrder())) {
            optionalGeoSearch.order(request.getGeoOrder());
        }
        return geo.search(optionalGeoSearch);
    }

    /**
     * 返回以给定成员计算指定距离半径内满足的地址位置所对应的元素以及元素与指定位置的距离
     *
     * 由于重载方法太多，这里根据参数来决定调用哪个方法
     *
     * key:   元素value
     * value: 与指定位置相距距离
     *
     * @param <V>
     * @return
     */
    public <V> Map<V, Double> radiusWithDistance(GeoMemberSearchRequest<V> request) {
        // PreconditionUtil.requiredParamCheck(request);
        final RGeo<V> geo = get(request.getKey());
        final OptionalGeoSearch optionalGeoSearch = GeoSearchArgs.from(request.getMember())
                .radius(request.getRadius(), request.getGeoUnit());
        if (ObjectUtil.isAllNotEmpty(request.getCount(), request.getGeoOrder())) {
            optionalGeoSearch.order(request.getGeoOrder())
                    .count(request.getCount());
        } else if (Objects.nonNull(request.getCount())) {
            optionalGeoSearch.count(request.getCount());
        } else if (Objects.nonNull(request.getGeoOrder())) {
            optionalGeoSearch.order(request.getGeoOrder());
        }
        return geo.searchWithDistance(optionalGeoSearch);
    }

    /**
     * 返回以给定成员计算指定距离半径内满足的地址位置所对应的元素以及各自的地址位置对象
     *
     * key: 元素value
     * value: 所属地理位置对象
     *
     * @param request
     * @param <V>
     * @return
     */
    public <V> Map<V, GeoPosition> radiusWithPosition(GeoMemberSearchRequest<V> request) {
        // PreconditionUtil.requiredParamCheck(request);
        final RGeo<V> geo = get(request.getKey());
        final OptionalGeoSearch optionalGeoSearch = GeoSearchArgs.from(request.getMember())
                .radius(request.getRadius(), request.getGeoUnit());
        if (ObjectUtil.isAllNotEmpty(request.getCount(), request.getGeoOrder())) {
            optionalGeoSearch.order(request.getGeoOrder())
                    .count(request.getCount());
        } else if (Objects.nonNull(request.getCount())) {
            optionalGeoSearch.count(request.getCount());
        } else if (Objects.nonNull(request.getGeoOrder())) {
            optionalGeoSearch.order(request.getGeoOrder());
        }
        return geo.searchWithPosition(optionalGeoSearch);
    }

    public static void main(String[] args) {
        final Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://www.snowball.fans:6379")
                .setPassword("Dongfang.ding_redis");
        config.setCodec(new JsonJacksonCodec());
        final RedissonClient redisson = Redisson.create(config);
        final GeoHelper helper = new GeoHelper(redisson);


        final RGeo<String> geo = helper.get("geo:test");
        String key = "geo:test";

        helper.add(key, 13.361389, 38.115556, "Palermo");
        helper.add(key, 15.087269, 37.502669, "Catalina");
        helper.add(key, 14.087269, 37.502669, "ZhangSan");
        helper.add(key, 15.087169, 38.502669, "LiSi");
        helper.add(key, 18.087269, 37.502669, "WangEr");
        helper.add(key, 25.087269, 39.502669, "MaZi");

        final Double dist = helper.dist(key, "Palermo", "MaZi", GeoUnit.KILOMETERS);
        System.out.printf("相距距离: %s %s%n", dist, GeoUnit.KILOMETERS.name());

        final GeoCoordinateSearchRequest build = GeoCoordinateSearchRequest.builder()
                .key(key)
                .longitude(14.361389)
                .latitude(37.502669)
                .radius(150d)
                .geoUnit(GeoUnit.KILOMETERS)
                .geoOrder(GeoOrder.ASC)
                .count(5)
                .build();

        final List<String> radius = helper.radius(build);
        System.out.println("radius = " + radius);


        final Map<String, Double> map = helper.radiusWithDistance(build);
        System.out.println("map = " + map);

        final Map<String, GeoPosition> map1 = helper.radiusWithPosition(build);
        System.out.println("map1 = " + map1);

    }



}
