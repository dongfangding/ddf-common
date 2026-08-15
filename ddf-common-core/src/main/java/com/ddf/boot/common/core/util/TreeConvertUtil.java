package com.ddf.boot.common.core.util;

import cn.hutool.core.collection.CollectionUtil;
import com.ddf.boot.common.api.constraint.collect.ITreeTagCollection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>树形转换工具</p >
 *
 * @author snowball
 * @version 1.0
 * @since 2021/03/02 10:25
 */
public class TreeConvertUtil {



    /**
     * 构建树形结构
     *
     * @param domainList domain列表
     */
    public static <K, T extends ITreeTagCollection<K, T>> List<T> convert(List<T> domainList) {
        if (CollectionUtil.isEmpty(domainList)) {
            return Collections.emptyList();
        }
        Map<K, T> dataMap = new LinkedHashMap<>(domainList.size());
        for (T domain : domainList) {
            dataMap.put(domain.getTreeId(), domain);
        }
        List<T> responseList = new ArrayList<>();
        for (Map.Entry<K, T> entry : dataMap.entrySet()) {
            T currentNode = entry.getValue();
            // 如果当前节点是根节点，直接添加到返回列表中
            if (currentNode.isRoot()) {
                responseList.add(currentNode);
            } else {
                // 如果不是根节点，查找当前节点的父节点，然后将当前节点添加到父节点的子节点集合中
                if (Objects.nonNull(currentNode.getTreeParentId())) {
                    final T t = dataMap.get(currentNode.getTreeParentId());
                    if (Objects.nonNull(t)) {
                        // 初始化children的时候必须是一个集合，不能为null， 因为没有暴露set方法，这里没办法new一个set回去
                        t.getChildren().add(currentNode);
                    }
                }
            }
        }
        return responseList;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor(staticName = "of")
    public static class Node implements ITreeTagCollection<Long, Node> {

        private String name;

        private Long id;

        private Long pid;

        private List<Node> children;

        @Override
        public Long getTreeId() {
            return id;
        }

        @Override
        public Long getTreeParentId() {
            return pid;
        }

        @Override
        public boolean isRoot() {
            return Objects.isNull(pid) || pid == 0;
        }

        @Override
        public List<Node> getChildren() {
            return children;
        }
    }
}
