package com.payment.trade.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "币种输出实体", description = "币种输出实体")
public class CurrencyVO {

    @ApiModelProperty(value = "币种")
    private String currency;

    @ApiModelProperty(value = "币种默认值")
    private String defaultValue;

}
