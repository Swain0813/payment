package com.payment.trade.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author XuWenQi
 * @Date: 2019/4/2 16:00
 * @Description: 每日订单金额统计实体
 */
@Data
@ApiModel(value = "每日订单金额统计实体", description = "每日订单金额统计实体")
public class CalcDailyAmountVO {

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "每日订单金额")
    private BigDecimal dailyAmount;
}
