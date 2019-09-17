package com.payment.trade.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author XuWenQi
 * @Date: 2019/8/12 14:28
 * @Description: 币种金额计算实体
 */
@Data
@ApiModel(value = "币种金额计算实体", description = "币种金额计算实体")
public class CurrencyAmountVO {

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "每日订单金额")
    private BigDecimal totalAmount;

}
