package com.ddf.boot.common.api.model.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.util.Assert;

/**
 * <p>分页请求通用接口
 * 请求参数类可以实现这个接口，来获得分页参数对象信息， 当然由于这个默认实现了{@code getPageNumAdaptive}和{@code getPageSizeAdaptive}，
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
     * 实际使用用这个，能够解决默认值问题
     *
     * @return
     */
    default Integer getPageNumAdaptive() {
        return ObjectUtils.defaultIfNull(getPageNum(), DEFAULT_PAGE_NUM);
    }

    /**
     * 实际使用用这个，能够解决默认值问题
     *
     * @return
     */
    default Integer getPageSizeAdaptive() {
        return ObjectUtils.defaultIfNull(getPageSize(), DEFAULT_PAGE_NUM);
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
        int pageNum = getPageNumAdaptive();
        int pageSize = getPageSizeAdaptive();
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
        return getStartIndex() + getPageSizeAdaptive();
    }

    /**
     * 参数校验
     *
     */
    default void checkArgument() {
        Assert.notNull(getPageNumAdaptive(), "pageNum不能为空");
        Assert.notNull(getPageSizeAdaptive(), "pageSize不能为空");
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
