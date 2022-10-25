package com.ddf.boot.common.websocket.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.ddf.boot.common.core.model.BaseDomain;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 设备运行状态监控$
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
 * @date 2020/3/11 0011 15:13
 */
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Accessors(chain = true)
public class MerchantBaseDeviceRunningState extends BaseDomain {

    /**
     * 设备id
     */
    private Long deviceId;

    /**
     * 指令码
     */
    private String cmd;

    /**
     * 对应的请求日志记录
     */
    private String requestId;

    /**
     * 指令最近一次下发时间
     */
    private Long requestTime;

    /**
     * 指令最近一次响应时间
     */
    private Long responseTime;

    /**
     * 指令状态 1 执行中 2 未执行 3（超长时间未响应的需要额外代码支持，暂缓开发）
     */
    private Integer status;

    /**
     * 区分是请求还是响应数据
     */
    @TableField(exist = false)
    private boolean responseFlag;

}
