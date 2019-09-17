package com.payment.common.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author shenxinran
 * @Date: 2019/6/24 09:37
 * @Description: 账户查询冻结与保证金信息DTO
 */
@Data
@ApiModel(value = "账户查询冻结与保证金信息DTO", description = "账户查询冻结与保证金信息DTO")
public class FrozenMarginInfoDTO {

    @ApiModelProperty(value = "账户ID")
    private String accountId;

    @ApiModelProperty(value = "资金类型 1-冻结户 2-保证金户")
    private String accountType;

    @ApiModelProperty(value = "币种")
    private String currency;

    private Integer pageNum;

    private Integer pageSize;

}
