package com.ddf.boot.common.api.constraint.collect;

import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;


/**
 * <p>树形结构资源需要的核心属性收集接口</p >
 *
 * @author snowball
 * @version 1.0
 * @date 2021/03/02 10:29
 */
public interface ITreeTagCollection<K, T> {

    /**
     * 资源的id
     *
     * @return
     */
    K getTreeId();

    /**
     * 资源的父级id
     *
     * @return
     */
    K getTreeParentId();

    /**
     * 当前节点是不是根节点, 默认父节点为空，则是根节点
     *
     * @return
     */
    default boolean isRoot() {
        return Objects.isNull(getTreeParentId()) || Objects.equals(0L, getTreeParentId()) || StringUtils.isBlank(
                getTreeParentId().toString());
    }

    /**
     * 获取children集合, 这个子类一定要默认的children属性是空集合，不能是null
     *
     * @param
     */
    @Nonnull
    List<T> getChildren();
}
