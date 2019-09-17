package com.payment.finance.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value = "机构交易对账输出实体", description = "机构交易对账输出实体")
public class CheckAccountVO {

    @ApiModelProperty(value = "机构编号")
    private String institutionCode;

    @ApiModelProperty(value = "订单币种")
    private String orderCurrency;

    @ApiModelProperty(value = "机构交易对账输出集合实体")
    private List<CheckAccountListVO> checkAccountListVOS;
}
