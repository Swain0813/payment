package com.payment.common.entity;

import com.payment.common.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Table;

/**
 * 设备型号
 */
@Data
@Table(name = "device_model")
@ApiModel(value = "设备型号", description = "设备型号")
public class DeviceModel extends BaseEntity {

    @ApiModelProperty(value = "厂商ID")
    @Column(name = "vendor_id")
    private String vendorId;

    @ApiModelProperty(value = "设备类型")
    @Column(name = "device_type")
    private String deviceType;

    @ApiModelProperty(value = "设备型号")
    @Column(name = "device_name")
    private String deviceName;

    @ApiModelProperty(value = "内存信息")
    @Column(name = "ram")
    private String ram;

    @ApiModelProperty(value = "操作系统")
    @Column(name = "system")
    private String system;

    @ApiModelProperty(value = "网络类型")
    @Column(name = "network")
    private String network;

    @ApiModelProperty(value = "打印机信息")
    @Column(name = "printer")
    private String printer;

    @ApiModelProperty(value = "分辨率信息")
    @Column(name = "resolution_ratio")
    private String resolutionRatio;

    @ApiModelProperty(value = "入网认证编号")
    @Column(name = "access_permit")
    private String accessPermit;

    @ApiModelProperty(value = "读卡器信息")
    @Column(name = "card_reader")
    private String cardReader;

    @ApiModelProperty(value = "启用禁用")
    @Column(name = "enabled")
    private Boolean enabled;


}
