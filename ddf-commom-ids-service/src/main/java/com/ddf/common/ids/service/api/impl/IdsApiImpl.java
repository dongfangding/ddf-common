package com.ddf.common.ids.service.api.impl;

import com.ddf.boot.common.core.util.PreconditionUtil;
import com.ddf.common.ids.service.api.IdsApi;
import com.ddf.common.ids.service.exception.IdsErrorCodeEnum;
import com.ddf.common.ids.service.exception.LengthZeroException;
import com.ddf.common.ids.service.exception.NoKeyException;
import com.ddf.common.ids.service.model.common.IdsMultiData;
import com.ddf.common.ids.service.model.common.IdsMultiListData;
import com.ddf.common.ids.service.model.common.Result;
import com.ddf.common.ids.service.model.common.ResultList;
import com.ddf.common.ids.service.service.IDGen;
import com.ddf.common.ids.service.service.SnowflakeService;
import java.util.List;
import java.util.Objects;

/**
 * <p>雪花ID和</p >
 *
 * @author Trump
 * @version 1.0
 * @date 2020/04/26 13:34
 */
public class IdsApiImpl implements IdsApi {

    private final SnowflakeService snowflakeService;

    private final IDGen segmentIDGen;

    public IdsApiImpl(SnowflakeService snowflakeService, IDGen segmentIDGen) {
        this.snowflakeService = snowflakeService;
        this.segmentIDGen = segmentIDGen;
    }

    /**
     * snowflake获取单个id
     *
     * @return
     */
    @Override
    public String getSnowflakeId() {
        return get(snowflakeService.get());
    }

    /**
     * snowflake获取多个id
     *
     * @param number
     * @return
     */
    @Override
    public List<String> getSnowflakeIds(Integer number) {
        return list(number, snowflakeService.list(number));
    }

    /**
     * Segment获取单个id
     *
     * @param key
     * @return
     */
    @Override
    public String getSegmentId(String key) {
        checkSegment();
        return get(segmentIDGen.get(key));
    }

    /**
     * Segment获取多个id
     *
     * @param key
     * @param number
     * @return
     */
    @Override
    public List<String> getSegmentIds(String key, Integer number) {
        checkSegment();
        return list(key, segmentIDGen.list(key, number));
    }

    /**
     * 获取组合ID
     *
     * @param key
     * @return
     */
    @Override
    public IdsMultiData getMultiId(String key) {
        checkSegment();
        return new IdsMultiData().setSequenceId(get(segmentIDGen.get(key)))
                .setSnowflakeId(get(snowflakeService.get()));
    }

    /**
     * 获取组合批量ID
     *
     * @param key
     * @param number
     * @return
     */
    @Override
    public IdsMultiListData getMultiIds(String key, Integer number) {
        checkSegment();
        return new IdsMultiListData()
                .setSequenceIds(list(number, segmentIDGen.list(key, number)))
                .setSnowflakeIds(list(number, snowflakeService.list(number)));
    }

    private String get(Result result) {
        return result.getId();
    }

    private List<String> list(String key, ResultList resultList) {
        if (key == null || key.isEmpty()) {
            throw new NoKeyException();
        }
        return resultList.getIdList();
    }

    private List<String> list(Integer length, ResultList resultList) {
        if (length == null || 0 == length) {
            throw new LengthZeroException();
        }
        return resultList.getIdList();
    }

    private void checkSegment() {
        PreconditionUtil.checkArgument(Objects.nonNull(segmentIDGen), IdsErrorCodeEnum.SEGMENT_IS_DISABLED);
    }

}
