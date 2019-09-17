package com.payment.common.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author shenxinran
 * @Date: 2019/6/13 15:07
 * @Description: 资金解冻输入实体
 */

@Data
@ApiModel(value = "资金解冻输入实体", description = "资金解冻输入实体")
public class ThawingFundsDTO {

    @ApiModelProperty(value = "机构code")
    private String institutionCode;

    @ApiModelProperty(value = "账户币种")
    private String currency;

    @ApiModelProperty(value = "冻结金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "解冻账户类型 1-冻结户 2-保证金账户 ")
    private Byte accountType;

    @ApiModelProperty(value = "原因")
    private String remark;

}
