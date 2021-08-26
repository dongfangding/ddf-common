package com.ddf.boot.common.core.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ddf.boot.common.core.model.Order;
import com.ddf.boot.common.core.model.PageRequest;
import com.ddf.boot.common.core.model.PageResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import javax.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
     * 构造基于mybatis的基本分页对象
     *
     * @param <T>
     * @return
     */
    public static <T> Page<T> toMybatis(PageRequest pageRequest) {
        int pageNum = 0;
        int pageSize = 0;
        pageRequest.checkArgument();
        if (!pageRequest.isUnPaged()) {
            pageNum = pageRequest.getPageNum();
            pageSize = pageRequest.getPageSize();
        }
        Page<T> objectPage = new Page<>(pageNum, pageSize);
        if (CollUtil.isNotEmpty(pageRequest.getOrders())) {
            objectPage.addOrder(toMybatisOrder(pageRequest));
        }
        return objectPage;
    }


    /**
     * 构建mybatis排序对象 FIXME 经测试，排序方式，会被最后一条记录给覆盖。所以并没有如现在数据格式设计的如此，可以为每个字段都定义排序类型，所以现在写这么复杂并没有什么卵用
     *
     * @return
     */
    private static List<OrderItem> toMybatisOrder(PageRequest pageRequest) {
        pageRequest.checkArgument();
        if (CollUtil.isEmpty(pageRequest.getOrders())) {
            return Collections.emptyList();
        }
        List<OrderItem> orderItemList = new ArrayList<>();
        for (Order order : pageRequest.getOrders()) {
            if (Sort.Direction.ASC.equals(order.getDirection())) {
                orderItemList.addAll(OrderItem.ascs(order.getColumn()));
            } else {
                orderItemList.addAll(OrderItem.descs(order.getColumn()));
            }
        }
        return orderItemList;
    }


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
     * 将mybatis-plus的分页对象转换为当前对象，主要是为了统一多个不同查询层的分页对象
     *
     * @param page
     * @param <E>
     * @return
     */
    public static <E> PageResult<E> ofMybatis(IPage<E> page) {
        return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords());
    }

    /**
     * 将mybatis-plus的分页对象转换为自定义封装的分页对象，，主要是为了统一多个不同查询层的分页对象
     * <p>
     * 同时提供一个数据库查询结果对象和返回对象的一个转换， 将数据库查询对象转换为指定对象，要求属性相同
     *
     * @param page
     * @param poClazz
     * @param voClazz
     * @param <T>
     * @param <R>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T, R> PageResult<R> ofMybatis(@NotNull IPage<T> page, @NotNull Class<T> poClazz,
            @Nullable Class<R> voClazz) {
        final List<T> list = page.getRecords();
        if (CollectionUtil.isEmpty(list)) {
            return empty((int) page.getCurrent(), (int) page.getSize());
        }
        if (voClazz == null || poClazz.getName().equals(voClazz.getName())) {
            List<R> rtnList = (List<R>) list;
            return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), rtnList);
        } else {
            return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), Convert.toList(voClazz, list));
        }
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
     * 由一个db查询出来的分页对象转换为自定义响应对象
     *  注意这个方法并没有像{@link PageUtil#ofMybatis(com.baomidou.mybatisplus.core.metadata.IPage, java.lang.Class, java.lang.Class)}
     *  一样提供了内部转换，这是由于两者期望的实现方式不同， 提供转换的那个内部使用了根据放射提供的工具类， 而这个方法更期望将转换方法交给调用方自己决定，
     *  如下面的例子演示的，则内部转换使用了mapstruct
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
     * 将Mybatis查询出来的对象转换为PageResult对象，且数据集合类型不同，提供转换
     *
     *  final Page<UserDynamicDTO> page = userDynamicService.searchUserDynamic(request);
     *  PageUtil.convertMybatis(page, (list) -> {
     *      List<UserDynamicResponse> responseList = new ArrayList<>(list.size());
     *      for (UserDynamicDTO dto : list) {
     *          final UserDynamicResponse response = new UserDynamicResponse();
     *      }
     *      return responseList;
     *  });
     *
     * @param page
     * @param function
     * @param <E>
     * @param <R>
     * @return
     */
    public static <E, R> PageResult<R> convertMybatis(@NotNull IPage<E> page, Function<List<E>, List<R>> function) {
        final PageResult<R> result = new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setContent(function.apply(page.getRecords()));
        return result;
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
