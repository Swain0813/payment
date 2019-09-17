package com.payment.common.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 产品导出输入参数
 */
@Data
@ApiModel(value = "产品导出输入参数", description = "产品导出输入参数")
public class ProductSearchExportDTO {
    @ApiModelProperty(value = "支付方式")
    private String payType;

    @ApiModelProperty(value = "交易类型")
    private Byte transType;

    @ApiModelProperty(value = "交易场景")
    private Byte tradeDirection;

    @ApiModelProperty(value = "语言")
    private String language;
}
