package com.payment.finance.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel(value = "机构交易对账输出集合实体", description = "机构交易对账输出集合实体")
public class CheckAccountListVO {

    @ApiModelProperty(value = "类型")
    private String type;

    @ApiModelProperty(value = "交易总金额")
    private BigDecimal totalAmount;

    @ApiModelProperty(value = "交易总笔数")
    private Integer totalCount;

    @ApiModelProperty(value = "手续费")
    private BigDecimal fee;

    @ApiModelProperty(value = "退款总金额")
    private BigDecimal refundAmount;

    @ApiModelProperty(value = "退款总笔数")
    private Integer refundCount;

}
