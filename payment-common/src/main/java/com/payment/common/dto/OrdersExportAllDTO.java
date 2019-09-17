package com.payment.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 交易一览导出实体
 */
@Data
@ApiModel(value = "交易一览查询输入参数实体", description = "交易一览查询输入参数实体")
public class OrdersExportAllDTO {

    @ApiModelProperty(value = "订单id")
    private String id;

    @ApiModelProperty(value = "交易类型")
    private String tradeType;

    @ApiModelProperty(value = "交易方向")
    private Byte tradeDirection;

    @ApiModelProperty(value = "商户名称")
    private String institutionName;

    @ApiModelProperty(value = "商户编号")
    private String institutionCode;

    @ApiModelProperty(value = "语言")
    private String language;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "商户订单时间")
    private Date institutionOrderTime;

    @ApiModelProperty(value = "商户订单号")
    private String institutionOrderId;

    @ApiModelProperty(value = "订单金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "订单币种")
    private String orderCurrency;

    @ApiModelProperty(value = "交易币种")
    private String tradeCurrency;

    @ApiModelProperty(value = "产品编码")
    private Integer productCode;

    @ApiModelProperty(value = "产品名称")
    private String productName;

    @ApiModelProperty(value = "通道流水号")
    private String channelNumber;

    @ApiModelProperty(value = "交易状态")//交易状态:1-待支付 2-交易中 3-交易成功 4-交易失败 5-已过期
    private Byte tradeStatus;

    @ApiModelProperty(value = "撤销状态")//撤销状态：1-撤销中 2-撤销成功 3-撤销失败
    private Byte cancelStatus;

    @ApiModelProperty(value = "退款状态")//退款状态：1-退款中 2-部分退款成功 3-退款成功 4-退款失败
    private Byte refundStatus;

    @ApiModelProperty(value = "调账状态")//调账状态 1-待调账 2-调账成功 3-调账失败
    private Byte reconciliationStatus;

    @ApiModelProperty(value = "交易开始时间")
    private String startDate;

    @ApiModelProperty(value = "交易结束时间")
    private String endDate;

    @ApiModelProperty(value = "支付开始完成时间")
    private String startPayFinishTime;

    @ApiModelProperty(value = "支付结束完成时间")
    private String endPayFinishTime;

    @ApiModelProperty(value = "支付方式")
    private String payType;

    @ApiModelProperty(value = "产品id")
    private String productId;

}
