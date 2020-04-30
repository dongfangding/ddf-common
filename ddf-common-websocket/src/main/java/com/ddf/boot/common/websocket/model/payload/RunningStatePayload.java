package com.ddf.boot.common.websocket.model.payload;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 运行状态请求数据类，可以自定义决定要获取哪些应用或类型的状态$
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
 * @date 2020/1/6 0006 18:32
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("运行状态请求数据类")
@Accessors(chain = true)
public class RunningStatePayload implements Serializable {

    static final long serialVersionUID = -720517330096395830L;

    @ApiModelProperty("是否获取当前能获取的所有应用状态")
    private boolean all;

    /**
     * @see RunningState#getUnionPay()
     */
    @ApiModelProperty("是否获取云闪付相关应用状态")
    private boolean unionPayFlag;


    /**
     * @see RunningState#getCmdState()
     */
    @ApiModelProperty("是否要获取指令码运行状态，即指令是否在运行")
    private boolean cmdFlag;
}
