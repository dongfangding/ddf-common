package com.ddf.boot.common.api.model.common.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.Set;
import lombok.Data;

/**
 * <p>description</p >
 *
 * @author snowball
 * @version 1.0
 * @since 2023/07/12 16:08
 */
@Data
public class BatchIdRequest {

    @NotEmpty(message = "ids不能为空")
    private Set<Long> ids;
}
