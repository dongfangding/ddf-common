package com.ddf.boot.common.websocket.model.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 资料同步payload$
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
 * @date 2020/1/4 0004 16:03
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("资料同步payload$")
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataSyncPayload implements Serializable {
    private static final long serialVersionUID = -1507474600377796045L;

    @ApiModelProperty("个人基本资料")
    private Base base;

    @ApiModelProperty("银行卡基本资料集合")
    private List<BankCard> bankCardList;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel("个人基本资料")
    @Accessors(chain = true)
    public static class Base {
        @ApiModelProperty("个人真实姓名")
        String name;

        @ApiModelProperty("身份证号")
        private String idCardNo;

        @ApiModelProperty("居住地址")
        private String address;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel("银行卡基本资料")
    @Accessors(chain = true)
    public static class BankCard {
        @ApiModelProperty("银行机构代码")
        private String bankCode;

        @ApiModelProperty("银行名称")
        private String bankName;

        @ApiModelProperty("银行卡号")
        private String cardNo;

        @ApiModelProperty("银行卡预留手机号")
        private String mobile;
    }
}
