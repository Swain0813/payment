package com.payment.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @description: 代理商分润英文查询VO
 * @author: XuWenQi
 * @create: 2019-08-23 16:44
 **/
@Data
@ApiModel(value = "代理商分润英文查询VO", description = "代理商分润英文查询VO")
public class QueryAgencyShareBenefitEnVO {

    @ApiModelProperty(value = "Creation Time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createTime;

    @ApiModelProperty(value = "Order Id")
    private String orderId;

    @ApiModelProperty(value = "Share Benefit Amount")
    private Double shareBenefit;

    @ApiModelProperty(value = "Share Benefit Status")//1:待分润，2：已分润
    private Byte isShare;

    @ApiModelProperty(value = "Institution Number")
    private String institutionCode;

    @ApiModelProperty(value = "Institution Name")
    private String institutionName;

    @ApiModelProperty(value = "Order Currency")
    private String tradeCurrency;

    @ApiModelProperty(value = "Order Amount")
    private BigDecimal tradeAmount;

    @ApiModelProperty(value = "Fee")
    private Double fee;

    @ApiModelProperty(value = "Remark")
    private String remark;

}
