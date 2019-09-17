package com.payment.common.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @description: 计算订单金额输入实体
 * @author: XuWenQi
 * @create: 2019-04-02 15:41
 **/
@Data
@ApiModel(value = "计算订单金额输入实体", description = "计算订单金额输入实体")
public class CalcOrdersAmountDTO {

    @ApiModelProperty(value = "产品id")
    private String productId;

    @ApiModelProperty(value = "产品币种")
    private String productCurrency;

    @NotNull(message = "50002")
    @ApiModelProperty(value = "机构code")
    private String institutionCode;

    @ApiModelProperty(value = "交易开始时间")
    private String startDate;

    @ApiModelProperty(value = "交易结束时间")
    private String endDate;

    @ApiModelProperty(value = "语言")
    private String language;
}
