package com.ddf.boot.common.websocket.model.entity;

import com.ddf.boot.common.core.entity.BaseDomain;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 设备基础管理表
 *
 *
 * @author dongfang.ding
 * @date 2019/12/21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class MerchantBaseDevice extends BaseDomain implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("商户id")
    private String merchantId;

    @ApiModelProperty("用户id")
    private String merchantUserBaseInfoId;

    @ApiModelProperty("设备号")
    private String number;

    @ApiModelProperty("设备名称")
    private String name;

    @ApiModelProperty("设备随机码")
    private String randomCode;

    @ApiModelProperty("设备型号")
    private String model;

    @ApiModelProperty("设备内存")
    private String memory;

    @ApiModelProperty("设备系统版本")
    private String osVersion;

    @ApiModelProperty("绑定状态 0 未绑定 1已绑定")
    private Integer bindingType;

    @ApiModelProperty("设备是否在线 0 离线 1在线")
    private Integer isOnline;

    @ApiModelProperty("经度")
    private BigDecimal longitude;

    @ApiModelProperty("纬度 ")
    private BigDecimal latitude;

    @ApiModelProperty("经纬度地址")
    private String address;

    @ApiModelProperty("绑定ip")
    private String bindingIp;

    @ApiModelProperty("0 未重启  1 已重启  每次发指令之前需要把该值置为0")
    private Byte restartFlag;

    @ApiModelProperty("最后一次设备重启时间")
    private Date restartTime;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("连接到服务器的地址")
    private String connectServerAddress;

    @ApiModelProperty("在线状态最后一次变化时间")
    private Long onlineChangeTime;

    @ApiModelProperty("0 未分配  1 已分配")
    private Integer isAllot;

    @ApiModelProperty("远程启用禁用状态 0 否 1 是")
    private Byte isRemoteDisable;

    @ApiModelProperty("序列号")
    private String sequence;

    @ApiModelProperty("当前版本清单号")
    private String currentListNumber;

    @ApiModelProperty("目标版本清单号")
    private String targetListNumber;

}
