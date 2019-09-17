package com.payment.common.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 批量更新订单物流信息输入参数明细
 */
@Data
@ApiModel(value = "批量更新订单物流信息输入参数", description = "批量更新订单物流信息输入参数")
public class LogisticsBachDTO {

    @NotNull(message = "50002")
    @ApiModelProperty(value = "订单物流id")
    private String id;

    @NotNull(message = "50002")
    @ApiModelProperty(value = "发货单号")
    private String invoiceNo;

    @NotNull(message = "50002")
    @ApiModelProperty(value = "服务商名称")
    private String providerName;

    @NotNull(message = "50002")
    @ApiModelProperty(value = "收货人地址")
    private String payerAddress;

    @ApiModelProperty(value = "备注")
    private String remark;

}
