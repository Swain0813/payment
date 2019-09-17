package com.payment.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 交易对账详细表输出实体的英文输入实体
 */
@Data
@ApiModel(value = "交易对账详细表输出实体的英文输入实体", description = "交易对账详细表输出实体的英文输入实体")
public class TradeCheckAccountDetailEnVO {

    @ApiModelProperty("AW Order Id")
    private String orderId;

    @ApiModelProperty("Institution Id")
    private String institutionCode;

    @ApiModelProperty("Order Creation Time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date orderCreateTime;

    @ApiModelProperty("Device Code")
    private String deviceCode;

    @ApiModelProperty("Institution Order Id")
    private String institutionOrderId;

    @ApiModelProperty("Payment Method")
    private String payType;

    @ApiModelProperty("Currency")
    private String orderCurrency;

    @ApiModelProperty("Order Amount")
    private BigDecimal amount;

    @ApiModelProperty("Order Type")
    private Byte tradeType;

    @ApiModelProperty("Order Status")
    private Byte tradeStatus;

    @ApiModelProperty("Order Completion Time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date payFinishTime;

    @ApiModelProperty("Fee Type")
    private String rateType;

    @ApiModelProperty("Rate")
    private BigDecimal rate;

    @ApiModelProperty("Fee")
    private BigDecimal fee;
}
