package com.ddf.boot.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 访问拒绝，无权限异常$
 * <p>
 * <p>
 * _ooOoo_
 * o8888888o
 * 88" . "88
 * (| -_- |)
 * O\ = /O
 * ___/`---'\____
 * .   ' \\| |// `.
 * / \\||| : |||// \
 * / _||||| -:- |||||- \
 * | | \\\ - /// | |
 * | \_| ''\---/'' | |
 * \ .-\__ `-` ___/-. /
 * ___`. .' /--.--\ `. . __
 * ."" '< `.___\_<|>_/___.' >'"".
 * | | : `- \`.;`\ _ /`;.`/ - ` : | |
 * \ \ `-. \_ __\ /__ _/ .-` / /
 * ======`-.____`-.___\_____/___.-`____.-'======
 * `=---='
 * .............................................
 * 佛曰：bug泛滥，我已瘫痪！
 *
 * @author dongfang.ding
 * @date 2019/12/24 0024 10:46
 */
@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class AccessDeniedException extends GlobalCustomizeException {
    public AccessDeniedException(Throwable e) {
        super(e);
    }

    /**
     * @param codeResolver
     * @param params
     */
    public AccessDeniedException(GlobalExceptionCodeResolver codeResolver, Object... params) {
        super(codeResolver, params);
    }

    public AccessDeniedException(GlobalExceptionCodeResolver codeResolver) {
        super(codeResolver);
    }

    public AccessDeniedException(String msg) {
        super(msg);
    }
}
