package com.payment.trade.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 下单输出参数
 */
@Data
@ApiModel(value = "下单输出实体", description = "下单输出实体")
public class SubmitOrdersVO {

    @ApiModelProperty(value = "订单id")
    private String id;

    @ApiModelProperty(value = "二维码url")
    private String codeUrl;
}
