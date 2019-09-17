package com.payment.trade.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 线下查询订单接口输入实体
 */
@Data
@ApiModel(value = "线下查询订单接口输入实体", description = "线下查询订单接口输入实体")
public class TerminalQueryOrdersDTO {

    @NotNull(message = "50002")
    @ApiModelProperty(value = "机构编号")
    private String institutionId;

    @NotNull(message = "50002")
    @ApiModelProperty(value = "机构订单号")
    private String orderNo; //商户订单号-由商户上送

    @NotNull(message = "50002")
    @ApiModelProperty(value = "设备编号")
    private String terminalId;

    @NotNull(message = "50002")
    @ApiModelProperty(value = "操作员ID")
    private String operatorId;

    @NotNull(message = "50002")
    @ApiModelProperty(value = "签名")
    private String sign;

    @ApiModelProperty(value = "语言")
    private String language;
}
