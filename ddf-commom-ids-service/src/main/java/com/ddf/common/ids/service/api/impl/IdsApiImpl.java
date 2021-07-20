package com.ddf.common.ids.service.api.impl;

import com.ddf.common.ids.service.api.IdsApi;
import com.ddf.common.ids.service.exception.LeafServerException;
import com.ddf.common.ids.service.exception.LengthZeroException;
import com.ddf.common.ids.service.exception.NoKeyException;
import com.ddf.common.ids.service.model.bo.SegmentIncrementMultiBO;
import com.ddf.common.ids.service.model.bo.SegmentIncrementSnowflakeMultiBO;
import com.ddf.common.ids.service.model.common.IdsMultiData;
import com.ddf.common.ids.service.model.common.IdsMultiListData;
import com.ddf.common.ids.service.model.common.Result;
import com.ddf.common.ids.service.model.common.ResultList;
import com.ddf.common.ids.service.model.common.Status;
import com.ddf.common.ids.service.model.dto.SegmentIncrementMultiDTO;
import com.ddf.common.ids.service.model.dto.SegmentIncrementSnowflakeMultiDTO;
import com.ddf.common.ids.service.service.IDGen;
import com.ddf.common.ids.service.service.SnowflakeService;
import java.util.Collections;
import java.util.List;

/**
 * <p>description</p >
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
        return get(snowflakeService.getId());
    }

    /**
     * snowflake获取多个id
     *
     * @param length
     * @return
     */
    @Override
    public List<String> getSnowflakeIds(Integer length) {
        return list(length, snowflakeService.getIdList(length));
    }

    /**
     * Segment获取单个id
     *
     * @param key
     * @return
     */
    @Override
    public String getSegmentId(String key) {
        return get(segmentIDGen.get(key));
    }

    /**
     * Segment获取多个id
     *
     * @param key
     * @return
     */
    @Override
    public List<String> getSegmentId(String key, Integer length) {
        return null;
    }

    /**
     * 获取组合ID
     *
     * @param key
     * @return
     */
    @Override
    public IdsMultiData getMultiId(String key) {
        return new IdsMultiData().setSequenceId("")
                .setSnowflakeId(get(snowflakeService.getId()));
    }

    /**
     * 获取组合批量ID
     *
     * @param key
     * @param length
     * @return
     */
    @Override
    public IdsMultiListData getMultiId(String key, Integer length) {
        return new IdsMultiListData()
                .setSequenceIds(Collections.emptyList())
                .setSnowflakeIds(list(length, snowflakeService.getIdList(length)));
    }

    @Override
    public String getSegmentIncrementId(String key) {
        return null;
    }

    @Override
    public List<String> getSegmentIncrementId(String key, Integer length) {
        return null;
    }

    @Override
    public SegmentIncrementMultiDTO getMulti(List<SegmentIncrementMultiBO> bos) {
        return null;
    }

    @Override
    public SegmentIncrementSnowflakeMultiDTO getSegmentIncrementSnowflakeMulti(SegmentIncrementSnowflakeMultiBO bo) {
        return null;
    }


    private String get(Result result) {
        if (result.getStatus().equals(Status.EXCEPTION)) {
            throw new LeafServerException(result.toString());
        }
        return result.getId();
    }

    private List<String> list(String key, ResultList resultList) {
        if (key == null || key.isEmpty()) {
            throw new NoKeyException();
        }
        if (resultList.getStatus().equals(Status.EXCEPTION)) {
            throw new LeafServerException(resultList.toString());
        }
        return resultList.getIdList();
    }

    private List<String> list(Integer length, ResultList resultList) {
        if (length == null || 0 == length) {
            throw new LengthZeroException();
        }
        if (resultList.getStatus().equals(Status.EXCEPTION)) {
            throw new LeafServerException(resultList.toString());
        }
        return resultList.getIdList();
    }

}
