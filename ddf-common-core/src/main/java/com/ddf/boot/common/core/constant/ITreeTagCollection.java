package com.ddf.boot.common.core.constant;

/**
 * <p>树形结构资源需要的核心属性收集接口</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/03/02 10:29
 */
public interface ITreeTagCollection {

    /**
     * 资源的id
     *
     * @return
     */
    String getTreeId();

    /**
     * 资源的父级id
     *
     * @return
     */
    String getTreeParentId();

    /**
     * 当前节点是不是根节点
     *
     * @return
     */
    boolean isRoot();

    /**
     * 将子节点吐出去，实现的时候要自己接收设置给自己类的属性中
     *
     * @param current
     */
    void setChildren(ITreeTagCollection current);
}
