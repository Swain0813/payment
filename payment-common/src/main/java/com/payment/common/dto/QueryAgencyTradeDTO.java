package com.payment.common.dto;


import com.payment.common.base.BasePageHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @description: 代理商交易查询DTO
 * @author: XuWenQi
 * @create: 2019-08-23 11:33
 **/
@Data
@ApiModel(value = "代理商交易查询DTO", description = "代理商交易查询DTO")
public class QueryAgencyTradeDTO extends BasePageHelper {

    @ApiModelProperty(value = "订单id")
    private String id;

    @ApiModelProperty(value = "商户编号")
    private String institutionCode;

    @ApiModelProperty(value = "商户名称")
    private String institutionName;

    @ApiModelProperty(value = "起始时间")
    private String startDate;

    @ApiModelProperty(value = "结束时间")
    private String endDate;

    @NotNull(message = "50002")
    @ApiModelProperty(value = "代理商编号")
    private String agencyCode;

}
