package com.ddf.boot.common.authenticate.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/05/24 22:48
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class AuthenticateToken implements Serializable {

    private static final long serialVersionUID = 1516322558409231083L;

    private static final String SPLIT = ".";

    /**
     * token前半部分， 用于校验的用户id
     */
    private String userIdToken;

    /**
     * token后半部分，加密后的用户信息详情
     */
    private String detailsToken;

    /**
     * 给前端的完整的token
     *
     * @return
     */
    public String getToken() {
        return String.join(SPLIT, userIdToken, detailsToken);
    }

    public static AuthenticateToken fromToken(String token) {
        final String[] tokenArr = token.split(SPLIT);
        return AuthenticateToken.of(tokenArr[0], tokenArr[1]);
    }
}
