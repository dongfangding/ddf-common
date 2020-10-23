package com.ddf.boot.common.core.entity;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * <p>分页展示层</p >
 *
 * // todo 针对下拉列表的查询，会用一个特定值代表全部， 如果前端不想特殊处理，把这个值传过来，后端把这个值重置为null
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2020/09/22 09:45
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<E> implements Serializable {
	private static final long serialVersionUID = 9056411043515781783L;

	/**
	 * 页码
	 */
	private long page;
	/**
	 * 每页结果数
	 */
	private long pageSize;
	/**
	 * 总页数
	 */
	private long totalPage;
	/**
	 * 总数
	 */
	private long total = 0;

	/**
	 * 数据
	 */
	private List<E> content = Collections.emptyList();

	/**
	 * 空分页
	 * @param <E>
	 * @return
	 */
	public static <E> PageResult<E> empty() {
		return new PageResult<>(BaseQuery.DEFAULT_PAGE_NUM, BaseQuery.DEFAULT_PAGE_SIZE);
	}

	/**
	 * 有数据的分页对象
	 * @param baseQuery
	 * @param total
	 * @param content
	 * @param <E>
	 * @return
	 */
	public static <E> PageResult<E> ofBaseQuery(BaseQuery baseQuery, long total, List<E> content) {
		if (baseQuery.isUnPaged()) {
			return new PageResult<>(baseQuery.getPage(), total, total, content);
		}
		return new PageResult<>(baseQuery.getPage(), baseQuery.getPageSize(), total, content);
	}

	/**
	 * 将mybatis-plus的分页对象转换为当前对象，主要是为了统一多个不同查询层的分页对象
	 * @param page
	 * @param <E>
	 * @return
	 */
	public static <E> PageResult<E> ofMybatis(Page<E> page) {
		return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords());
	}

	/**
	 * 将mybatis-plus的分页对象转换为自定义封装的分页对象，，主要是为了统一多个不同查询层的分页对象
	 *
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
	public static <T, R> PageResult<R> ofMybatis(@NotNull Page<T> page, @NotNull Class<T> poClazz
			, @Nullable Class<R> voClazz) {
		final List<T> list = page.getRecords();
		if (CollectionUtil.isEmpty(list)) {
			return PageResult.empty();
		}
		// 如果是这张情况， 应该直接使用com.ddf.boot.common.core.entity.PageResult.ofMybatis(com.baomidou.mybatisplus.extension.plugins.pagination.Page<E>)
		if (voClazz == null || poClazz.getName().equals(voClazz.getName())) {
			List<R> rtnList = (List<R>) list;
			return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), rtnList);
		} else {
			return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), Convert.toList(voClazz, list));
		}
	}

	/**
	 * 构造
	 *
	 * @param page     页码
	 * @param pageSize 每页结果数
	 */
	public PageResult(long page, long pageSize) {
		this.page = Math.max(page, BaseQuery.DEFAULT_PAGE_NUM);
		this.pageSize = pageSize;
	}

	/**
	 * 构造
	 *
	 * @param page     页码
	 * @param pageSize 每页结果数
	 * @param total    结果总数
	 */
	public PageResult(long page, long pageSize, long total, List<E> content) {
		this(page, pageSize);
		this.total = total;
		this.totalPage = totalPage(total, pageSize);
		this.content = content;
	}

	public static long totalPage(long totalCount, long pageSize) {
		if (pageSize == 0) {
			return 0;
		}
		return totalCount % pageSize == 0 ? (totalCount / pageSize) : (totalCount / pageSize + 1);
	}
}
