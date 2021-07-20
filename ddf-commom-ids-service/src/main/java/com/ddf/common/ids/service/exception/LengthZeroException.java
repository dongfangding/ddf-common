package com.ddf.common.ids.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code= HttpStatus.INTERNAL_SERVER_ERROR,reason="list length zero")
public class LengthZeroException extends RuntimeException {
}
