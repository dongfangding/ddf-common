package com.ddf.boot.common.core.model;

import java.util.List;
import lombok.Data;

/**
 * <p>如果返回值不希望直接返回List， 可以提供一个通用的包装</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2020/12/31 13:54
 */
@Data
public class ListWrapperResponse<E> {

    /**
     * list数据包装, 这里只考虑List， 未使用Collection, 避免使用时还需要强转
     */
    private List<E> list;

    /**
     * 静态构建
     *
     * @param list 原始数据集合
     * @param <E>  集合泛型
     * @return 返回包装后类型
     */
    public static <E> ListWrapperResponse<E> of(List<E> list) {
        final ListWrapperResponse<E> response = new ListWrapperResponse<>();
        response.setList(list);
        return response;
    }

}
