package com.ddf.common.ids.service.api.impl;

import com.ddf.boot.common.core.util.PreconditionUtil;
import com.ddf.common.ids.service.api.IdsApi;
import com.ddf.common.ids.service.config.properties.IdsProperties;
import com.ddf.common.ids.service.exception.IdsErrorCodeEnum;
import com.ddf.common.ids.service.exception.LengthZeroException;
import com.ddf.common.ids.service.exception.NoKeyException;
import com.ddf.common.ids.service.model.common.DecodeSnowflakeIdData;
import com.ddf.common.ids.service.model.common.IdsMultiData;
import com.ddf.common.ids.service.model.common.IdsMultiListData;
import com.ddf.common.ids.service.model.common.Result;
import com.ddf.common.ids.service.model.common.ResultList;
import com.ddf.common.ids.service.model.common.SegmentBufferView;
import com.ddf.common.ids.service.service.IDGen;
import com.ddf.common.ids.service.service.SnowflakeService;
import com.ddf.common.ids.service.service.impl.segment.SegmentIDGenImpl;
import com.ddf.common.ids.service.service.impl.segment.model.LeafAlloc;
import com.ddf.common.ids.service.service.impl.segment.model.SegmentBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <p>雪花ID和</p >
 *
 * @author Trump
 * @version 1.0
 * @date 2020/04/26 13:34
 */
public class IdsApiImpl implements IdsApi {

    private final IdsProperties idsProperties;

    private final SnowflakeService snowflakeService;

    private final IDGen segmentIDGen;

    public IdsApiImpl(IdsProperties idsProperties, SnowflakeService snowflakeService, IDGen segmentIDGen) {
        this.idsProperties = idsProperties;
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

    /**
     * 获取号段模式缓存信息
     *
     * @return
     */
    @Override
    public Map<String, SegmentBufferView> getSegmentCache() {
        checkSegment();
        Map<String, SegmentBufferView> data = new HashMap<>(32);
        Map<String, SegmentBuffer> cache = ((SegmentIDGenImpl) segmentIDGen).getCache();
        for (Map.Entry<String, SegmentBuffer> entry : cache.entrySet()) {
            SegmentBufferView sv = new SegmentBufferView();
            SegmentBuffer buffer = entry.getValue();
            sv.setInitOk(buffer.isInitOk());
            sv.setKey(buffer.getKey());
            sv.setPos(buffer.getCurrentPos());
            sv.setNextReady(buffer.isNextReady());
            sv.setMax0(buffer.getSegments()[0].getMax());
            sv.setValue0(buffer.getSegments()[0].getValue().get());
            sv.setStep0(buffer.getSegments()[0].getStep());
            sv.setMax1(buffer.getSegments()[1].getMax());
            sv.setValue1(buffer.getSegments()[1].getValue().get());
            sv.setStep1(buffer.getSegments()[1].getStep());
            data.put(entry.getKey(), sv);
        }
        return data;
    }

    @Override
    public List<LeafAlloc> getDb() {
        return ((SegmentIDGenImpl) segmentIDGen).getAllLeafAllocs();
    }

    /**
     * 解析雪花id信息
     *
     * @param snowflakeIdStr
     * @return
     */
    @Override
    public DecodeSnowflakeIdData decodeSnowflakeId(String snowflakeIdStr) {
        final DecodeSnowflakeIdData data = new DecodeSnowflakeIdData();
        long snowflakeId = Long.parseLong(snowflakeIdStr);
        long originTimestamp = (snowflakeId >> 22) + idsProperties.getBeginTimestamp();
        data.setOriginTimestamp(originTimestamp);
        data.setWorkerId((snowflakeId >> 12) ^ (snowflakeId >> 22 << 10));
        data.setSequence(snowflakeId ^ (snowflakeId >> 12 << 12));
        return data;
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
