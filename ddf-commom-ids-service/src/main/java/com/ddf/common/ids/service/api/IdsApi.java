package com.ddf.common.ids.service.api;

import com.ddf.common.ids.service.model.bo.SegmentIncrementMultiBO;
import com.ddf.common.ids.service.model.bo.SegmentIncrementSnowflakeMultiBO;
import com.ddf.common.ids.service.model.common.IdsMultiData;
import com.ddf.common.ids.service.model.common.IdsMultiListData;
import com.ddf.common.ids.service.model.dto.SegmentIncrementMultiDTO;
import com.ddf.common.ids.service.model.dto.SegmentIncrementSnowflakeMultiDTO;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

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
     * @param length
     * @return
     */
    List<String> getSnowflakeIds(Integer length);

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
     * @param length
     * @return
     */
    List<String> getSegmentId(String key, Integer length);

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
     * @param length
     * @return
     */
    IdsMultiListData getMultiId(@PathVariable("key") String key, Integer length);

    /**
     * 绝对递增单个序列id
     *
     * @param key
     * @return
     */
    String getSegmentIncrementId(@PathVariable("key") String key);

    /**
     * 绝对递增多个序列id
     *
     * @param key
     * @param length
     * @return
     */
    List<String> getSegmentIncrementId(@PathVariable("key") String key, @PathVariable("length") Integer length);

    /**
     * 多个key的绝对递增序列ID
     *
     * @param bos
     * @return
     */
    SegmentIncrementMultiDTO getMulti(@RequestBody List<SegmentIncrementMultiBO> bos);

    /**
     * 批量绝对递增和雪花组合id
     *
     * @param bo
     * @return
     */
    SegmentIncrementSnowflakeMultiDTO getSegmentIncrementSnowflakeMulti(@RequestBody SegmentIncrementSnowflakeMultiBO bo);
}
