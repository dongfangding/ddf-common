package com.ddf.boot.common.authentication.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

;

/**
 * <p>token校验返回对象</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/05/25 10:37
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class AuthenticateCheckResult implements Serializable {

    private static final long serialVersionUID = 1516322558409231083L;

    /**
     * token解析出来的对象
     */
    private AuthenticateToken authenticateToken;

    /**
     * 还原后的用户信息
     */
    private UserClaim userClaim;

}
