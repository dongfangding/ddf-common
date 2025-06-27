package com.ddf.boot.common.api.model.common.request;

import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * <p>description</p >
 *
 * @author snowball
 * @version 1.0
 * @since 2023/07/12 16:08
 */
@Data
public class IdRequest {

    @NotNull(message = "id不能为空")
    private Long id;
}
