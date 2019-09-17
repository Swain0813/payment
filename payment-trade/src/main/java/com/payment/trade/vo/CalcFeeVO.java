package com.payment.trade.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author shenxinran
 * @Date: 2019/2/25 09:54
 * @Description: 手续费输出实体
 */
@Data
@ApiModel(value = "计算手续费输出实体", description = "计算手续费输出实体")
public class CalcFeeVO {
    @ApiModelProperty(value = "手续费")
    private BigDecimal fee;

    @ApiModelProperty(value = "计费时间")
    private Date chargeTime;

    @ApiModelProperty(value = "计费状态")
    private Byte chargeStatus;

}
