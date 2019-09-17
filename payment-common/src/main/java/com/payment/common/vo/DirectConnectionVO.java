package com.payment.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "直连查询基础数据vo", description = "直连查询基础数据vo")
public class DirectConnectionVO {

    @ApiModelProperty("通道code")
    private String channelCode;

    @ApiModelProperty("institution_product id")
    private String insProId;

    @ApiModelProperty("银行名称")
    private String bankName;

    @ApiModelProperty("产品code")
    private String productCode;
}
