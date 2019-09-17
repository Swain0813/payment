package com.payment.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @description: 简单产品实体
 * @author: YangXu
 * @create: 2019-02-22 14:16
 **/
@Data
@ApiModel(value = "简单产品实体", description = "简单产品实体")
public class ProVO {

    @ApiModelProperty(value = "产品id")
    private String productId;

    @ApiModelProperty(value = "产品编号")
    private String productCode;

    @ApiModelProperty(value = "交易类型")
    private Byte transType;

    @ApiModelProperty(value = "支付类型")
    private String payType;

    @ApiModelProperty(value = "币种")
    private String currency;

}
