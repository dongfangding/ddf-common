package com.ddf.common.ids.service.api;

import com.ddf.common.ids.service.model.common.IdsMultiData;
import com.ddf.common.ids.service.model.common.IdsMultiListData;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * <p>Ids feign api</p >
 *
 * @author shuaishuai.xiao
 * @version 1.0: SequenceApi.java
 * @date 2019/12/18 18:42
 */
public interface IdsApi {

    /**
     * 单个雪花id
     *
     * @return
     */
    String getSnowflakeId();

    /**
     * 多个雪花id
     *
     * @param number
     * @return
     */
    List<String> getSnowflakeIds(Integer number);

    /**
     * 单个序列id
     *
     * @param key
     * @return
     */
    String getSegmentId(String key);

    /**
     * 多个序列id
     *
     * @param key
     * @param number
     * @return
     */
    List<String> getSegmentIds(String key, Integer number);

    /**
     * 获取组合id
     *
     * @param key
     * @return
     */
    IdsMultiData getMultiId(String key);

    /**
     * 批量获取组合id
     *
     * @param key
     * @param number
     * @return
     */
    IdsMultiListData getMultiIds(@PathVariable("key") String key, Integer number);

}
