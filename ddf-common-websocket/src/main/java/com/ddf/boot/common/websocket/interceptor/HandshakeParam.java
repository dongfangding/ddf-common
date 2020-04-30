package com.ddf.boot.common.websocket.interceptor;

import com.ddf.boot.common.websocket.model.ws.AuthPrincipal;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 握手参数$
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
 * @date 2019/12/27 0027 14:01
 */
@Data
@Accessors(chain = true)
public class HandshakeParam implements Serializable {

    /**
     * 设备号
     */
    private String deviceNumber;

    /**
     * 设备绑定时的随机码
     */
    private String randomCode;

    /**
     * 登录类型
     */
    private AuthPrincipal.LoginType loginType;

    /**
     * 当前时间戳
     */
    private long currentTimeStamp;

    /**
     * 版本号
     */
    private String version;
}
