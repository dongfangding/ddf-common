package com.ddf.boot.common.jwt.exception;

/**
 * 用户信息丢失异常
 *
 */
public class UserClaimMissionException extends Exception {

    public UserClaimMissionException(String message) {
        super(message);
    }
}
