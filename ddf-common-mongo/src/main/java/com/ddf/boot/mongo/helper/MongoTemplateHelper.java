package com.ddf.boot.mongo.helper;

import cn.hutool.core.convert.Convert;
import com.ddf.boot.common.core.entity.BaseQuery;
import com.ddf.boot.common.core.entity.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

/**
 * <p>mongo帮助类</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2020/09/21 19:11
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MongoTemplateHelper {

    private final MongoTemplate mongoTemplate;

    /**
     * 分页通用方法处理， 这个方法用于查询出来的对象和返回的不是同一个，内部会提供转换， 这个返回的是自己包装的分页对象，建议优先使用
     *
     * @param baseQuery 原始查询对象，这个是为了在当前方法中提取分页参数
     * @param query     查询对象
     * @param poClazz   原始Mongo对象类型
     * @param voClazz   输出对象
     * @param <Q>       原始查询对象
     * @param <T>       原始Mongo对象类型
     * @param <R>       要转换的输出的实体类型
     * @return
     */
    @SuppressWarnings("unchecked")
    public <Q extends BaseQuery, T, R> PageResult<R> handlerPageResult(@NotNull Q baseQuery, @NotNull Query query, @NotNull Class<T> poClazz
            , @Nullable Class<R> voClazz) {
        long count = mongoTemplate.count(query, poClazz);
        if (count <= 0) {
            return PageResult.empty();
        }
        if (!baseQuery.isUnPaged()) {
            Pageable pageable = baseQuery.toSpringData();
            query.with(pageable);
        }
        List<T> dbList = mongoTemplate.find(query, poClazz);
        if (voClazz == null || poClazz.getName().equals(voClazz.getName())) {
            List<R> rtnList = (List<R>) dbList;
            return PageResult.ofBaseQuery(baseQuery, count, rtnList);
        } else {
            return PageResult.ofBaseQuery(baseQuery, count, Convert.toList(voClazz, dbList));
        }
    }


    /**
     * 分页通用方法处理, 这个方法用户查询出来的对象和要返回的对象是同一个， 这个返回的是自己包装的分页对象，建议优先使用
     *
     * @param baseQuery 原始查询对象，这个是为了在当前方法中提取分页参数
     * @param query     查询对象
     * @param poClazz   原始Mongo对象类型
     * @param <Q>       原始查询对象
     * @param <T>       原始Mongo对象类型
     * @return
     */
    public <Q extends BaseQuery, T> PageResult<T> handlerPageResult(@NotNull Q baseQuery, @NotNull Query query, @NotNull Class<T> poClazz) {
        return handlerPageResult(baseQuery, query, poClazz, null);
    }



    /**
     * 分页通用方法处理， 这个方法用于查询出来的对象和返回的不是同一个，内部会提供转换, 这个返回的是spring-data自己的分页对象
     *
     * @param baseQuery 原始查询对象，这个是为了在当前方法中提取分页参数
     * @param query     查询对象
     * @param poClazz   原始Mongo对象类型
     * @param voClazz   输出对象
     * @param <Q>       原始查询对象
     * @param <T>       原始Mongo对象类型
     * @param <R>       要转换的输出的实体类型
     * @return
     */
    @SuppressWarnings("unchecked")
    public <Q extends BaseQuery, T, R> Page<R> handlerPage(@NotNull Q baseQuery, @NotNull Query query, @NotNull Class<T> poClazz
            , @Nullable Class<R> voClazz) {
        long count = mongoTemplate.count(query, poClazz);
        Pageable pageable = baseQuery.toSpringData();
        if (count <= 0) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        query.with(pageable);
        List<T> dbList = mongoTemplate.find(query, poClazz);
        if (voClazz == null || poClazz.getName().equals(voClazz.getName())) {
            List<R> rtnList = (List<R>) dbList;
            return new PageImpl<>(rtnList, pageable, count);
        } else {
            return new PageImpl<>(Convert.toList(voClazz, dbList), pageable, count);
        }
    }

    /**
     * 分页通用方法处理, 这个方法用户查询出来的对象和要返回的对象是同一个, 这个返回的是spring-data自己的分页对象
     *
     * @param baseQuery 原始查询对象，这个是为了在当前方法中提取分页参数
     * @param query     查询对象
     * @param poClazz   原始Mongo对象类型
     * @param <Q>       原始查询对象
     * @param <T>       原始Mongo对象类型
     * @return
     */
    public <Q extends BaseQuery, T> Page<T> handlerPage(@NotNull Q baseQuery, @NotNull Query query, @NotNull Class<T> poClazz) {
        return handlerPage(baseQuery, query, poClazz, null);
    }
}
