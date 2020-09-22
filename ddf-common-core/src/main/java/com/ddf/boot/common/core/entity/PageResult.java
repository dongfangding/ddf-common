package com.ddf.boot.common.core.entity;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * <p>分页展示层</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2020/09/22 09:45
 */
@Data
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
	private long total;

	/**
	 * 数据
	 */
	private List<E> content;

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
