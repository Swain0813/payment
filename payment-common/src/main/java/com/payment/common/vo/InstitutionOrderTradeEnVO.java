package com.payment.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 机构交易一览导出实体英文的场合
 */
@Data
@ApiModel(value = "机构交易一览导出实体", description = "机构交易一览导出实体")
public class InstitutionOrderTradeEnVO {

    @ApiModelProperty(value = "Order Creation Time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createTime;

    @ApiModelProperty(value = "AW Order Id")
    private String orderId;

    @ApiModelProperty(value = "Institution Id")
    private String institutionCode;

    @ApiModelProperty(value = "Institution Name")
    private String institutionName;

    @ApiModelProperty(value = "Institution Order Id")
    private String institutionOrderId;

    @ApiModelProperty(value = "Payment Method")
    private String payMethod;

    @ApiModelProperty(value = "Order Currency")
    private String orderCurrency;

    @ApiModelProperty(value = "Order Amount")
    private BigDecimal amount;

    @ApiModelProperty(value = "Fee")
    private BigDecimal fee;

    @ApiModelProperty(value = "Product Name")
    private String productName;

    @ApiModelProperty(value = "Channel Order Id")
    private String channelNumber;

    @ApiModelProperty(value = "Order Type")
    private String tradeType;

    @ApiModelProperty(value = "Order Direction")
    private Byte tradeDirection;

    @ApiModelProperty(value = "Order Status")//交易状态:1-待支付 2-交易中 3-交易成功 4-交易失败 5-已过期
    private Byte tradeStatus;

    @ApiModelProperty(value = "Void Status")//撤销状态：1-撤销中 2-撤销成功 3-撤销失败
    private Byte cancelStatus;

    @ApiModelProperty(value = "Refund Status")//退款状态：1-退款中 2-部分退款成功 3-退款成功 4-退款失败
    private Byte refundStatus;

    @ApiModelProperty(value = "Device Code")
    private String deviceCode;

    @ApiModelProperty(value = "Order Completion Time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date channelCallbackTime;

}
