package com.ddf.boot.common.core.util;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TreeConvertUtil 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class TreeConvertUtilTest {

    @Test
    @DisplayName("空列表应返回空树")
    void shouldReturnEmptyListWhenSourceIsEmpty() {
        assertTrue(TreeConvertUtil.convert(List.<TreeConvertUtil.Node>of()).isEmpty());
    }

    @Test
    @DisplayName("应按父子关系构建树结构")
    void shouldConvertFlatNodesToTree() {
        List<TreeConvertUtil.Node> nodes = List.of(TreeConvertUtil.Node.of("root-1", 1L, 0L, new ArrayList<>()),
                TreeConvertUtil.Node.of("child-1-1", 2L, 1L, new ArrayList<>()),
                TreeConvertUtil.Node.of("child-1-2", 3L, 1L, new ArrayList<>()),
                TreeConvertUtil.Node.of("grandchild-1-1-1", 4L, 2L, new ArrayList<>()),
                TreeConvertUtil.Node.of("root-2", 5L, null, new ArrayList<>()));

        List<TreeConvertUtil.Node> tree = TreeConvertUtil.convert(nodes);

        assertEquals(2, tree.size());
        assertEquals("root-1", tree.get(0).getName());
        assertEquals(2, tree.get(0).getChildren().size());
        assertEquals("child-1-1", tree.get(0).getChildren().get(0).getName());
        assertEquals(1, tree.get(0).getChildren().get(0).getChildren().size());
        assertEquals("grandchild-1-1-1", tree.get(0).getChildren().get(0).getChildren().get(0).getName());
        assertEquals("root-2", tree.get(1).getName());
    }

    @Test
    @DisplayName("父节点缺失的孤儿节点不应进入根结果")
    void shouldIgnoreOrphanNodeFromRootResult() {
        List<TreeConvertUtil.Node> nodes = List.of(TreeConvertUtil.Node.of("orphan", 10L, 99L, new ArrayList<>()));

        List<TreeConvertUtil.Node> tree = TreeConvertUtil.convert(nodes);

        assertTrue(tree.isEmpty());
    }
}
