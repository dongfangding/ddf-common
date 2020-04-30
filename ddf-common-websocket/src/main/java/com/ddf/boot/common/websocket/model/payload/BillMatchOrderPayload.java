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
 * BILL_MATCH_ORDER的业务数据请求类
 *
 * @author dongfang.ding
 * @date 2019/10/22 18:13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("BILL_MATCH_ORDER的业务数据请求类")
@Accessors(chain = true)
public class BillMatchOrderPayload implements Serializable {
    private static final long serialVersionUID = -2954919005333115490L;


    @ApiModelProperty("二维码类型")
    private Integer qrCodeType;

    @ApiModelProperty("收款还是支出")
    private BillTypeEnum billType;
}
