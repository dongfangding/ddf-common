package com.ddf.boot.common.websocket.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
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
 * @author
 * @create 2019-09-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class MerchantBaseDevice implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 信息id
     */
    @TableId
    private String id;

    /**
     * 商户id
     */
    private String merchantId;

    /**
     * 设备号
     */
    private String number;

    /**
     * 设备名称
     */
    private String name;

    /**
     * 设备随机码
     */
    private String randomCode;

    /**
     * 设备型号
     */
    private String model;

    /**
     * 设备内存
     */
    private String memory;

    /**
     * 设备系统版本
     */
    private String osVersion;

    /**
     * 绑定状态 0 未绑定 1已绑定
     */
    private Integer bindingType;

    /**
     * 设备是否在线 0 离线 1在线
     */
    private Integer isOnline;

    @ApiModelProperty("经度")
    private BigDecimal longitude;

    @ApiModelProperty("纬度")
    private BigDecimal latitude;

    /**
     * 经纬度地址
     */
    private String address;

    /**
     * 绑定ip
     */
    private String bindingIp;

    /**
     * 0 未重启  1 已重启  每次发指令之前需要把该值置为0
     */
    private Integer restartFlag;

    /**
     * 最后一次设备重启时间
     */
    private Date restartTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createDate;

    /**
     * 连接到服务器的地址
     */
    private String connectServerAddress;

    /**
     * 在线状态最后一次变化时间， 单位毫秒
     */
    private Long onlineChangeTime;

    /**
     * 分配商户
     */
    private Integer isAllot;

    /**
     * 修改日期
     */
    private Date updateDate;

    /**
     * 商户创建人
     */
    private String merchantCreateUserId;

    /**
     * 商户修改人
     */
    private String merchantUpdateUserId;

    /**
     * 总台创建人
     */
    private String platformCreateUserId;

    /**
     * 商户修改人
     */
    private String platformUpdateUserId;

    /**
     * 是否删除 0保留 1删除
     */
    private Integer isDel;

    /**
     * 0启用 1禁用
     */
    private Integer isEnable;

    @ApiModelProperty(value = "是否远程禁用 0 否 1 是", allowableValues = "0,1")
    private Byte isRemoteDisable;

    /**
     * 序列号
     */
    private String sequence;

    @ApiModelProperty("当前版本清单号")
    private String currentListNumber;

    @ApiModelProperty("目标版本清单号")
    private String targetListNumber;

    /**
     * 云闪付运行状态 0未启动 1正常
     */
    private Integer flushStatus;

    /**
     * 云闪付登录状态 0未登录 1登录
     */
    private Integer flushLoginStatus;

    @ApiModelProperty("设备应用运行状态")
    @TableField("app_running_status")
    private String appRunningState;

}
