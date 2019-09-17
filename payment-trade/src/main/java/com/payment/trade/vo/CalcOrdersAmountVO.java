package com.payment.trade.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author XuWenQi
 * @Date: 2019/4/2 16:00
 * @Description: 订单金额汇总输出实体
 */
@Data
@ApiModel(value = "订单金额汇总输出实体", description = "订单金额汇总输出实体")
public class CalcOrdersAmountVO {

    @ApiModelProperty(value = "支付方式总金额")
    private BigDecimal payTypeTotalAmount;

    @ApiModelProperty(value = "支付方式对应订单每日交易额")
    private Map<String, BigDecimal> dailyAmount;
}
