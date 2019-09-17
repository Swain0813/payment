package com.payment.trade.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author XuWenQi
 * @Date: 2019/3/27 14:00
 * @Description: 订单金额输出实体
 */
@Data
@ApiModel(value = "订单金额输出实体", description = "订单金额输出实体")
public class OrdersAmountVO {

    @ApiModelProperty(value = "订单id")
    private String ordersId;

    @ApiModelProperty(value = "订单金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "创建时间")
    public String createTime;

    @ApiModelProperty(value = "产品id")
    private String productId;

    @ApiModelProperty(value = "产品币种")
    private String productCurrency;

    @ApiModelProperty(value = "产品名称")
    private String productName;

    @ApiModelProperty(value = "字典支付方式名称")
    private String dName;

}
