package com.ddf.boot.common.redis.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.redisson.api.GeoOrder;
import org.redisson.api.GeoUnit;

/**
 * <p>GEO基于坐标查询对象参数包装</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/04/23 16:14
 */
@Getter
@Setter
@Builder
public class GeoCoordinateSearchRequest {

    /**
     * key
     */
    @NotBlank(message = "key不能为空")
    private String key;

    /**
     * 经度
     */
    @NotNull(message = "经度不能为空")
    private Double longitude;

    /**
     * 纬度
     */
    @NotNull(message = "纬度不能为空")
    private Double latitude;

    /**
     * 半径
     */
    @NotNull(message = "半径不能为空")
    private Double radius;

    /**
     * 半径单位
     */
    @NotNull(message = "半径单位不能为空")
    private GeoUnit geoUnit;

    /**
     * 排序
     */
    private GeoOrder geoOrder;

    /**
     * 本次查询数量
     */
    private Integer count;
}
