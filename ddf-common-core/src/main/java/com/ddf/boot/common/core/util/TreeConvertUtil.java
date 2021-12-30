package com.ddf.boot.common.core.util;

import cn.hutool.core.collection.CollectionUtil;
import com.ddf.boot.common.core.constant.ITreeTagCollection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>树形转换工具</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/03/02 10:25
 */
public class TreeConvertUtil {


    /**
     * 构建树形结构
     *
     * @param domainList
     * @param function
     * @return
     */
    public static <T extends ITreeTagCollection> List<T> convert(List<ITreeTagCollection> domainList, Function<ITreeTagCollection, T> function) {
        if (CollectionUtil.isEmpty(domainList)) {
            return Collections.emptyList();
        }
        Map<String, T> dataMap = new LinkedHashMap<>(domainList.size());
        for (ITreeTagCollection domain : domainList) {
            dataMap.put(domain.getTreeId(), function.apply(domain));
        }
        List<T> responseList = new ArrayList<>();
        for (Map.Entry<String, T> entry : dataMap.entrySet()) {
            T currentNode = entry.getValue();
            // 如果当前节点是根节点，直接添加到返回列表中
            if (currentNode.isRoot()) {
                responseList.add(currentNode);
            } else {
                // 如果不是根节点，查找当前节点的父节点，然后将当前节点添加到父节点的子节点集合中
                if (StringUtils.isNotBlank(currentNode.getTreeParentId())) {
                    if (Objects.nonNull(dataMap.get(currentNode.getTreeId()))) {
                        dataMap.get(currentNode.getTreeId()).setChildren(currentNode);
                    }
                }
            }
        }
        return responseList;
    }
}
