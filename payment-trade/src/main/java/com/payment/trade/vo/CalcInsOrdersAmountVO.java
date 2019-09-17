package com.payment.trade.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author XuWenQi
 * @Date: 2019/4/2 16:00
 * @Description: 机构订单金额计算统计实体
 */
@Data
@ApiModel(value = "订单金额汇总输出实体", description = "订单金额汇总输出实体")
public class CalcInsOrdersAmountVO {

    @ApiModelProperty(value = "产品编码")
    private Integer productCode;

    @ApiModelProperty(value = "产品名称")
    private String productName;

    @ApiModelProperty(value = "每日订单金额统计实体")
    private List<CalcDailyAmountVO> calcDailyAmountVOList;
}
