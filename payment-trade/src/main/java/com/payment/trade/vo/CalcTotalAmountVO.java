package com.payment.trade.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author XuWenQi
 * @Date: 2019/4/2 16:00
 * @Description: 产品金额计算实体
 */
@Data
@ApiModel(value = "产品金额计算实体", description = "每日订单金额统计实体")
public class CalcTotalAmountVO {

    @ApiModelProperty(value = "支付方式名称")
    private String payTypeName;

    @ApiModelProperty(value = "每日订单金额")
    private BigDecimal totalAmount;

}
