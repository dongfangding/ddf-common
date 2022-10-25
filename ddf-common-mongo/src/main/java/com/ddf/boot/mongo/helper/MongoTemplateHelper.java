package com.ddf.boot.mongo.helper;

import com.ddf.boot.common.api.model.PageRequest;
import com.ddf.boot.common.api.model.PageResult;
import com.ddf.boot.common.core.util.BeanCopierUtils;
import com.ddf.boot.common.core.util.PageUtil;
import java.util.Collections;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

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
     * @param pageRequest 原始查询对象，这个是为了在当前方法中提取分页参数
     * @param query     查询对象
     * @param poClazz   原始Mongo对象类型
     * @param voClazz   输出对象
     * @param <T>       原始Mongo对象类型
     * @param <R>       要转换的输出的实体类型
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T, R> PageResult<R> handlerPageResult(@NotNull PageRequest pageRequest, @NotNull Query query,
            @NotNull Class<T> poClazz, @Nullable Class<R> voClazz) {
        long count = mongoTemplate.count(query, poClazz);
        if (count <= 0) {
            return PageUtil.empty(pageRequest);
        }
        if (!pageRequest.isUnPaged()) {
            Pageable pageable = PageUtil.toSpringData(pageRequest);
            query.with(pageable);
        }
        List<T> dbList = mongoTemplate.find(query, poClazz);
        if (voClazz == null || poClazz.getName().equals(voClazz.getName())) {
            List<R> rtnList = (List<R>) dbList;
            return PageUtil.ofPageRequest(pageRequest, count, rtnList);
        } else {
            return PageUtil.ofPageRequest(pageRequest, count, BeanCopierUtils.copy(dbList, voClazz));
        }
    }


    /**
     * 分页通用方法处理, 这个方法用户查询出来的对象和要返回的对象是同一个， 这个返回的是自己包装的分页对象，建议优先使用
     *
     * @param pageRequest 原始查询对象，这个是为了在当前方法中提取分页参数
     * @param query     查询对象
     * @param poClazz   原始Mongo对象类型
     * @param <T>       原始Mongo对象类型
     * @return
     */
    public <T> PageResult<T> handlerPageResult(@NotNull PageRequest pageRequest, @NotNull Query query,
            @NotNull Class<T> poClazz) {
        return handlerPageResult(pageRequest, query, poClazz, null);
    }



    /**
     * 分页通用方法处理， 这个方法用于查询出来的对象和返回的不是同一个，内部会提供转换, 这个返回的是spring-data自己的分页对象
     *
     * @param pageRequest 原始查询对象，这个是为了在当前方法中提取分页参数
     * @param query     查询对象
     * @param poClazz   原始Mongo对象类型
     * @param voClazz   输出对象
     * @param <T>       原始Mongo对象类型
     * @param <R>       要转换的输出的实体类型
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T, R> Page<R> handlerPage(@NotNull PageRequest pageRequest, @NotNull Query query,
            @NotNull Class<T> poClazz, @Nullable Class<R> voClazz) {
        long count = mongoTemplate.count(query, poClazz);
        Pageable pageable = PageUtil.toSpringData(pageRequest);
        if (count <= 0) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        query.with(pageable);
        List<T> dbList = mongoTemplate.find(query, poClazz);
        if (voClazz == null || poClazz.getName().equals(voClazz.getName())) {
            List<R> rtnList = (List<R>) dbList;
            return new PageImpl<>(rtnList, pageable, count);
        } else {
            return new PageImpl<>(BeanCopierUtils.copy(dbList, voClazz), pageable, count);
        }
    }

    /**
     * 分页通用方法处理, 这个方法用户查询出来的对象和要返回的对象是同一个, 这个返回的是spring-data自己的分页对象
     *
     * @param pageRequest 原始查询对象，这个是为了在当前方法中提取分页参数
     * @param query     查询对象
     * @param poClazz   原始Mongo对象类型
     * @param <T>       原始Mongo对象类型
     * @return
     */
    public <T> Page<T> handlerPage(@NotNull PageRequest pageRequest, @NotNull Query query,
            @NotNull Class<T> poClazz) {
        return handlerPage(pageRequest, query, poClazz, null);
    }
}
