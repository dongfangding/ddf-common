package com.ddf.boot.common.core.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2025/03/15 18:02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class IndexComparatorElement {

    /**
     * 元素索引
     */
    private int index;
    /**
     * 元素值
     */
    private String value;
    /**
     * 比对状态 1-新增 2-替换 3-删除
     */
    private int status;

    /**
     * 被替换前原始值
     */
    private String beforeValue;

    /**
     * 判断这个元素是否被删除
     *
     * @return
     */
    public boolean isDelete() {
        return status == 3;
    }

    /**
     * 判断当前value是否是替换之前元素的
     *
     * @return
     */
    public boolean isChange() {
        return status == 2;
    }


    /**
     * 判断这个元素是否是新增的
     *
     * @return
     */
    public boolean isAdd() {
        return status == 1;
    }

    public String renderStatus() {
        switch (status) {
            case 1:
                return "新增";
            case 2:
                return "替换";
            case 3:
                return "删除";
            default:
                return "未知";
        }
    }

    @Override
    public String toString() {
        if (isChange()) {
            return String.format("第%d位: '%s' 被替换为 '%s'", index, beforeValue, value);
        }
        return String.format("第%d位: '%s' (%s)", index, value, renderStatus());
    }
}
