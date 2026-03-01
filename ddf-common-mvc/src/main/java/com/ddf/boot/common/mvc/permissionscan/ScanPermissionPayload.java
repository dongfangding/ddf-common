package com.ddf.boot.common.mvc.permissionscan;

import java.util.List;
import lombok.Data;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2025/04/28 19:26
 */
@Data
public class ScanPermissionPayload {

    private List<SysMenuFunction> menuFunctions;
}
