package com.ddf.boot.common.mvc.permissionscan;

import com.ddf.boot.common.api.constraint.collect.ITreeTagCollection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Data;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2025/01/07 17:50
 */


/**
 * 系统菜单
 */
@Data
public class SysMenuFunction implements ITreeTagCollection<String, SysMenuFunction> {

    /**
     * 菜单名称
     */
    private String name;

    /**
     * 菜单代码
     */
    private String code;

    /**
     * 菜单类型
     */
    private PermissionMenuType type;

    /**
     * 组件名称/路由地址，前端用，两个字段合一，保持一样了
     */
    private String componentName;

    /**
     * 组件路径，前端用
     */
    private String componentPath;

    /**
     * 父类菜单名称
     */
    private String parentName;

    /**
     * 父类菜单代码
     */
    private String parentCode;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 权限
     */
    private String permission;

    /**
     * 是否导入
     */
    private boolean imported;


    private List<SysMenuFunction> children = new ArrayList<>();

    @Override
    public String getTreeId() {
        return code;
    }

    @Override
    public String getTreeParentId() {
        return parentCode;
    }

    /**
     * code全局唯一
     *
     * @param o O参数
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        SysMenuFunction function = (SysMenuFunction) o;
        return Objects.equals(getCode(), function.getCode());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getCode());
    }
}
