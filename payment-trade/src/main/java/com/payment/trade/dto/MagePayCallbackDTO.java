package com.payment.trade.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author XuWenQi
 * @Date: 2019/5/30 18:08
 * @Description: magePay回调输入实体
 */
@Data
@ApiModel(value = "eghl回调浏览器输入实体", description = "eghl回调浏览器输入实体")
public class MagePayCallbackDTO {

    @ApiModelProperty(value = "交易类型")
    private String inv;

    @ApiModelProperty(value = "支付方式")
    private String merID;

    @ApiModelProperty(value = "服务id")
    private String refCode;

    @ApiModelProperty(value = "支付id")
    private String amt;

    @ApiModelProperty(value = "订单号")
    private String mark;


}
