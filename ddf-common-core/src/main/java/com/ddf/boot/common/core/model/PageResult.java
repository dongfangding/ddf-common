package com.ddf.boot.common.core.model;

import cn.hutool.core.collection.CollectionUtil;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>分页展示层</p >
 * <p>
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
    private long pageNum;
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
     * 是否为空分页对象
     *
     * @return
     */
    public boolean isEmpty() {
        return total == 0 || CollectionUtil.isEmpty(content);
    }

    /**
     * 构造
     *
     * @param pageNum     页码
     * @param pageSize 每页结果数
     */
    public PageResult(long pageNum, long pageSize) {
        this.pageNum = Math.max(pageNum, PageRequest.DEFAULT_PAGE_NUM);
        this.pageSize = pageSize;
    }

    /**
     * 构造
     *
     * @param pageNum     页码
     * @param pageSize 每页结果数
     * @param total 总条数
     */
    public PageResult(long pageNum, long pageSize, long total) {
        this(pageNum, pageSize);
        this.total = total;
        this.totalPage = totalPage(total, pageSize);
    }

    /**
     * 构造
     *
     * @param pageNum     页码
     * @param pageSize 每页结果数
     * @param total    结果总数
     */
    public PageResult(long pageNum, long pageSize, long total, List<E> content) {
        this(pageNum, pageSize);
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
