package com.ddf.boot.common.websocket.model.payload;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * APP升级指令数据格式
 * @author dongfang.ding
 * @date 2019/8/24 13:48
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("APP升级指令数据格式")
@Accessors(chain = true)
public class UpgradePayload implements Serializable {

    private static final long serialVersionUID = -8069195136127549902L;

    @ApiModelProperty("最新apk下载地址")
    private String lastApkUrl;

    @ApiModelProperty("版本代码")
    private String versionCode;

    @ApiModelProperty("版本名称")
    private String versionName;



}
