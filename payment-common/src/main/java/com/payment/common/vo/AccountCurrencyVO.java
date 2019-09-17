package com.payment.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @description: 账户查询VO
 * @author: YangXu
 * @create: 2019-03-05 16:03
 **/
@Data
@ApiModel(value = "账户查询VO", description = "账户查询VO")
public class AccountCurrencyVO {

    @ApiModelProperty(value = "币种")
    private String currency;

}
