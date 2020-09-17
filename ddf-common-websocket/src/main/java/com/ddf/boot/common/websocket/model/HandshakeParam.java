package com.ddf.boot.common.websocket.model;

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
     * 认证身份信息的关键字，如用户id
     * 一种更为常用的方式如使用了jwt,将用户的jwt通过这个字段传入，然后自行解析jwt中包含的用户信息，验证用户信息
     */
    private String accessKeyId;

    /**
     * 认证身份信息的名称
     * 如果是jwt, 推荐解析accessKeyId包含用户信息, 认证通过后将用户名称复制给当前字段
     */
    private String accessKeyName;

    /**
     * 授权码，可以让要建立连接的客户端连接之前要先和服务端申请授权码，可以实现更加安全的连接通道，这里给个默认值
     */
    private String authCode = "000000";

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
    private String version = "1.0";
}
