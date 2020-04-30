package com.ddf.boot.common.websocket.interceptor;

import com.ddf.boot.common.websocket.model.ws.AuthPrincipal;
import org.springframework.http.server.ServerHttpResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * 如果当前主版本的握手认证失败，提供一个旧版本的支持$
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
 * @date 2019/12/27 0027 13:44
 */
public interface HandshakeDowngrade {

    AuthPrincipal validPrincipal(HttpServletRequest httpServletRequest, ServerHttpResponse response);
}
