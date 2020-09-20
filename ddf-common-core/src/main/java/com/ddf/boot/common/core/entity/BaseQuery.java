package com.ddf.boot.common.core.entity;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2020/06/29 14:58
 */
@Data
@ApiModel("分页请求对象")
@AllArgsConstructor
@Accessors(chain = true)
@NoArgsConstructor
public class BaseQuery {

    /**
     * 页数, 从1开始
     */
    private int page = 1;

    /**
     * 每页显示条数
     */
    private int size = 10;

    /**
     * 不分页查询， 默认false
     */
    private boolean unPaged = false;

    /**
     * 排序对象
     */
    private List<Order> orders;

    /**
     * 构造基于spring-data基本分页对象
     * @return
     */
    public Pageable ofSpringData() {
        if (unPaged) {
            return Pageable.unpaged();
        }
        // spring-data的分页从0开始
        return PageRequest.of(page - 1, size);
    }

    /**
     * 构造基于mybatis的基本分页对象
     * @param <T>
     * @return
     */
    public <T> Page<T> ofMybatis() {
        if (unPaged) {
            page = 0;
            size = 0;
        }
        Page<T> objectPage = new Page<>(page, size);
        if (CollUtil.isNotEmpty(orders)) {
            objectPage.addOrder(ofMybatisOrder());
        }
        return objectPage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Order {

        private String[] column;

        private Sort.Direction direction;
    }


    /**
     * 构建mybatis排序对象
     * FIXME 经测试，排序方式，会被最后一条记录给覆盖。所以并没有如现在数据格式设计的如此，可以为每个字段都定义排序类型，所以现在写这么复杂并没有什么卵用
     * @return
     */
    public List<OrderItem> ofMybatisOrder() {
        if (CollUtil.isEmpty(orders)) {
            return Collections.emptyList();
        }
        List<OrderItem> orderItemList = new ArrayList<>();
        for (Order order : orders) {
            if (Sort.Direction.ASC.equals(order.getDirection())) {
                orderItemList.addAll(OrderItem.ascs(order.getColumn()));
            } else {
                orderItemList.addAll(OrderItem.descs(order.getColumn()));
            }
        }
        return orderItemList;
    }
}
