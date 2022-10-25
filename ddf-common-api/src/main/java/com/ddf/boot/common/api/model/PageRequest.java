package com.ddf.boot.common.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.util.Assert;

/**
 * <p>分页请求通用接口
 * 请求参数类可以实现这个接口，来获得分页参数对象信息， 当然由于这个默认实现了{@code getPageNum}和{@code getPageSize}，
 * 不要忘了在自己的请求参数里加上这两个属性， 否则无法接收参数，只能使用默认值了
 * </p >
 *
 * @author network
 * @version 1.0
 * @date 2020/08/17 15:17
 */
public interface PageRequest {

    int DEFAULT_PAGE_NUM = 1;

    int DEFAULT_PAGE_SIZE = 10;

    /**
     * 不分页查询， 默认false
     *
     * @return 返回是否分页
     */
    default Boolean isUnPaged() {
        return false;
    }

    /**
     * 页码
     *
     * @return
     */
    default Integer getPageNum() {
        return DEFAULT_PAGE_NUM;
    }

    /**
     * 每页条数
     *
     * @return
     */
    default Integer getPageSize() {
        return DEFAULT_PAGE_SIZE;
    }

    /**
     * 开始行
     *
     * @return
     */
    default Integer getStartIndex() {
        checkArgument();
        int pageNum = getPageNum();
        int pageSize = getPageSize();
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1) {
            pageSize = 0;
        }
        return (pageNum - 1) * pageSize;
    }

    /**
     * 结束行
     *
     * @return
     */
    default Integer getEndIndex() {
        checkArgument();
        return getStartIndex() + getPageSize();
    }

    /**
     * 参数校验
     *
     */
    default void checkArgument() {
        Assert.notNull(getPageNum(), "pageNum不能为空");
        Assert.notNull(getPageSize(), "pageSize不能为空");
    }

    @Data
    @AllArgsConstructor
    class DefaultPageRequest implements PageRequest {
        private Integer pageNum;
        private Integer pageSize;

        public static PageRequest of(Integer pageNum, Integer pageSize) {
            return new DefaultPageRequest(pageNum, pageSize);
        }
    }



}
