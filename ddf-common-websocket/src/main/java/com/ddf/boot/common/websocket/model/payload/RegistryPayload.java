package com.ddf.boot.common.websocket.model.payload;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 注册数据业务类$
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
 * @date 2020/4/9 0009 15:55
 */
@Data
@Accessors(chain = true)
public class RegistryPayload implements Serializable {

    /**
     * 登录密码
     */
    private String loginPassword;

    /**
     * 账号
     */
    private String accountName;

}
