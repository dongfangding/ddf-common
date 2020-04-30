package com.ddf.boot.common.websocket.model.payload;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 绑卡实体类$
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
 * @date 2020/4/9 0009 15:47
 */
@Data
@Accessors(chain = true)
public class BindingCardPayload implements Serializable {

    /**
     * 银行卡号
      */
    private String bankCardNumber;

    /**
     * 银行卡的取款密码
     */
    private String bankCardPayPassword;
}
