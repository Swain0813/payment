package com.payment.trade.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author XuWenQi
 * @Date: 2019/3/7 15:35
 * @Description: 云闪付回调
 */
@Data
@ApiModel(value = "云闪付回调", description = "云闪付回调")
public class CloudFlashCallbackDTO {

    @ApiModelProperty(value = "id")
    private String id;

    @ApiModelProperty(value = "订单状态")
    private Byte tradeStatus;

}
