package com.payment.trade.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author XuWenQi
 * @Date: 2019/8/12 14:26
 * @Description: 机构订单金额计算统计实体
 */
@Data
@ApiModel(value = "机构订单金额计算统计实体", description = "机构订单金额计算统计实体")
public class StatisticsVO {

    @ApiModelProperty(value = "币种总金额统计")
    private List<CalcCurrencyAmountVO> totalCurrencyAmountList;

    @ApiModelProperty(value = "支付方式总金额统计")
    private List<CalcTotalAmountVO> totalPayTypeAmountList;

    @ApiModelProperty(value = "每日订单金额统计")
    private List<CalcInsOrdersAmountVO> dailyAmountList;

}
