package com.payment.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @description: 代理商交易英文查询VO
 * @author: XuWenQi
 * @create: 2019-08-23 11:33
 **/
@Data
@ApiModel(value = "代理商交易英文查询VO", description = "代理商交易英文查询VO")
public class QueryAgencyTradeEnVO {

    @ApiModelProperty(value = "Order Creation Time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createTime;

    @ApiModelProperty(value = "Order Id")
    private String id;

    @ApiModelProperty(value = "Agency Code")
    private String agencyCode;

    @ApiModelProperty(value = "Institution Id")
    private String institutionCode;

    @ApiModelProperty(value = "Institution Name")
    private String institutionName;

    @ApiModelProperty(value = "Order Currency")
    private String orderCurrency;

    @ApiModelProperty(value = "Order Amount")
    private BigDecimal amount;

    @ApiModelProperty(value = "Fee")
    private BigDecimal fee;

    @ApiModelProperty(value = "Trade Status")
    private Byte tradeStatus;

    @ApiModelProperty(value = "Remark")
    private String remark;

}
