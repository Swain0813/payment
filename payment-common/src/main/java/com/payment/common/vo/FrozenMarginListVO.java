package com.payment.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author shenxinran
 * @Date: 2019/6/24 08:50
 * @Description:
 */
@Data
@ApiModel(value = "保证金冻结金查询List", description = "保证金冻结金查询List")
public class FrozenMarginListVO {

    @ApiModelProperty(value = "金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "币种")
    private String order_currency;

    @ApiModelProperty(value = "账户ID")
    private String institution_order_id;

    @ApiModelProperty(value = "类型  1-冻结户 2-保证金户")
    private String account_type;
}
