package com.ddf.common.vps.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * VpsUtil 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class VpsUtilTest {

    @Test
    @DisplayName("应按 FastDFS 规则提取物理存储路径")
    void shouldExtractFastDfsPhysicalStorePath() {
        assertEquals("/00/00/ag8Kh2GnPTWASVlZAM7twHqR7-Y487.mp4",
                VpsUtil.getFDfsPhysicalStorePath("group1/M00/00/00/ag8Kh2GnPTWASVlZAM7twHqR7-Y487.mp4"));
        assertEquals("/02/99/demo.png", VpsUtil.getFDfsPhysicalStorePath("group12/M03/02/99/demo.png"));
    }
}
