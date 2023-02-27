package com.ddf.boot.common.core.util;

import cn.hutool.core.convert.Convert;
import com.ddf.boot.common.api.model.common.PageRequest;
import com.ddf.boot.common.api.model.common.PageResult;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import java.util.List;
import java.util.function.Function;
import javax.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;

/**
 * <p>分页工具类</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2021/02/21 22:43
 */
public class PageUtil {

    /**
     * 空分页
     *
     * @param <E>
     * @return
     */
    public static <E> PageResult<E> empty(Integer pageNum, Integer pageSize) {
        return new PageResult<>(pageNum, pageSize);
    }

    /**
     * 空分页
     *
     * @param <E>
     * @return
     */
    public static <E> PageResult<E> empty(PageRequest pageRequest) {
        pageRequest.checkArgument();
        return new PageResult<>(pageRequest.getPageNum(), pageRequest.getPageSize());
    }

    /**
     * 有数据的分页对象
     *
     * @param pageRequest
     * @param total
     * @param content
     * @param <E>
     * @return
     */
    public static <E> PageResult<E> ofPageRequest(PageRequest pageRequest, long total, List<E> content) {
        pageRequest.checkArgument();
        if (pageRequest.isUnPaged()) {
            return new PageResult<>(pageRequest.getPageNum(), total, total, content);
        }
        return new PageResult<>(pageRequest.getPageNum(), pageRequest.getPageSize(), total, content);
    }

    /**
     * 使用PageHelper分页， 但是会转换为自己的分页结果对象， 并提供查询对象和实际返回结果的转换
     *
     * @param pageRequest
     * @param select
     * @param poClazz
     * @return
     * @param <E>
     * @param <R>
     */
    public static <E, R> PageResult<R> startPage(PageRequest pageRequest, ISelect select, @NotNull Class<E> poClazz) {
        return startPage(pageRequest, select, poClazz, null);
    }


    /**
     * 由一个db查询出来的分页对象转换为自定义响应对象
     *
     * <pre>
     * final PageResult<SysRole> result = sysRoleService.pageList(request);
     * if (result.isEmpty()) {
     *    return PageUtil.empty();
     * }
     * final PageResult<SysRoleDTO> responsePageResult = PageUtil.convertPageResult(
     *          result, SysRoleConvertMapper.INSTANCE::convert);
     * </pre>
     *
     * @param pageResult
     * @param function
     * @param <E>
     * @param <R>
     * @return
     */
    public static <E, R> PageResult<R> convertPageResult(PageResult<E> pageResult, Function<List<E>, List<R>> function) {
        final PageResult<R> result = new PageResult<>(pageResult.getPageNum(), pageResult.getPageSize(), pageResult.getTotal());
        result.setContent(function.apply(pageResult.getContent()));
        return result;
    }

    /**
     * 使用PageHelper分页， 但是会转换为自己的分页结果对象， 并提供查询对象和实际返回结果的转换
     *
     * @param pageRequest
     * @param select
     * @param poClazz     原始查询出来的对象
     * @param voClazz     要转换的对象
     * @param <E>
     * @param <R>
     * @return
     */
    public static <E, R> PageResult<R> startPage(PageRequest pageRequest, ISelect select, @NotNull Class<E> poClazz,
            @Nullable Class<R> voClazz) {
        // 查询出原始对象
        final PageInfo<E> pageInfo = PageHelper.startPage(pageRequest.getPageNum(), pageRequest.getPageSize())
                .doSelectPageInfo(select);
        if (pageInfo.getSize() <= 0) {
            return empty(pageRequest);
        }
        // 转换为自定义对象
        if (voClazz == null || poClazz.getName().equals(voClazz.getName())) {
            List<R> rtnList = (List<R>) pageInfo.getList();
            return new PageResult<>(pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal(), rtnList);
        } else {
            return new PageResult<>(pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal(), Convert.toList(voClazz, pageInfo.getList()));
        }
    }


    /**
     * 使用PageHelper分页， 但是会转换为自己的分页结果对象， 需要自己提供转换方法
     *
     * @param pageRequest
     * @param select
     * @param function
     * @return
     * @param <E>
     * @param <R>
     */
    public static <E, R> PageResult<R> startPage(PageRequest pageRequest, ISelect select, Function<List<E>, List<R>> function) {
        // 查询出原始对象
        final PageInfo<E> pageInfo = PageHelper.startPage(pageRequest.getPageNum(), pageRequest.getPageSize())
                .doSelectPageInfo(select);
        if (pageInfo.getSize() <= 0) {
            return empty(pageRequest);
        }
        final PageResult<R> result = new PageResult<>(pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal());
        result.setContent(function.apply(pageInfo.getList()));
        return result;
    }


    /**
     * 使用PageHelper分页， 转换为自己的分页对象， 但是不转换实体对象
     *
     *
     * @param pageRequest
     * @param select
     * @return
     * @param <E>
     */
    public static <E> PageResult<E> startPage(PageRequest pageRequest, ISelect select) {
        // 查询出原始对象
        final PageInfo<E> pageInfo = PageHelper.startPage(pageRequest.getPageNum(), pageRequest.getPageSize())
                .doSelectPageInfo(select);
        if (pageInfo.getSize() <= 0) {
            return empty(pageRequest);
        }
        return new PageResult<>(pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal(), pageInfo.getList());
    }

    /**
     * 构造基于spring-data基本分页对象
     *
     * @return
     */
    public static Pageable toSpringData(PageRequest pageRequest) {
        pageRequest.checkArgument();
        if (pageRequest.isUnPaged()) {
            return Pageable.unpaged();
        }
        // spring-data的分页从0开始
        return org.springframework.data.domain.PageRequest.of(
                (int) pageRequest.getPageNum() - 1, (int) pageRequest.getPageSize());
    }

    /**
     * 从spring-data分页结果对象转换为自定义分页结果对象
     *
     * @param page
     * @param <E>
     * @return
     */
    public static <E> PageResult<E> convertFromSpringData(@NotNull org.springframework.data.domain.Page<E> page) {
        final PageResult<E> result = new PageResult<E>(page.getNumber(), page.getSize(), page.getTotalElements());
        result.setContent(page.getContent());
        return result;
    }
}
