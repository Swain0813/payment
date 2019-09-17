package com.payment.trade.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 线下查询机构关联信息输入实体
 */
@Data
@ApiModel(value = "线下查询机构关联信息输入实体", description = "线下查询机构关联信息输入实体")
public class TerminalQueryRelevantDTO {

    @NotNull(message = "50002")
    @ApiModelProperty(value = "机构编号")
    private String institutionId;

    @NotNull(message = "50002")
    @ApiModelProperty(value = "设备编号")
    private String terminalId;

    @NotNull(message = "50002")
    @ApiModelProperty(value = "设备操作员")
    private String operatorId;

    @NotNull(message = "50002")
    @ApiModelProperty(value = "签名")
    private String sign;

    @ApiModelProperty(value = "交易类型(收付款)")
    private String dealType;

}
