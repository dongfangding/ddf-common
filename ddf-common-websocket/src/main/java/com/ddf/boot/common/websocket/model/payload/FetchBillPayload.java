package com.ddf.boot.common.websocket.model.payload;

import com.ddf.boot.common.websocket.enumerate.BillTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 查看云闪付账单指令数据格式
 *
 * @author dongfang.ding
 * @date 2019/8/24 13:56
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("查看云闪付账单指令数据格式")
@Accessors(chain = true)
public class FetchBillPayload implements Serializable {
    private static final long serialVersionUID = -2954919005333115490L;

    @ApiModelProperty("二维码类型")
    private Integer qrCodeType;

    @ApiModelProperty("收款还是支出")
    private BillTypeEnum billType;

    @ApiModelProperty("获取数据的账单截止时间")
    private Long orderTime;

    private Integer currPage = 1;

    @ApiModelProperty("这个大小是每页最大大小，如果根据账单截止时间拿到的数据不够20条，也不需要继续拿下去了")
    private Integer pageSize = 20;

}
