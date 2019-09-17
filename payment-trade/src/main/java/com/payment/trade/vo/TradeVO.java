package com.payment.trade.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;


@Data
@ApiModel(value = "交易查询输出实体", description = "交易查询输出实体")
public class TradeVO {

    @ApiModelProperty(value = "订单id")
    private String id;

    @ApiModelProperty(value = "机构编号")
    private String institutionCode;

    @ApiModelProperty(value = "机构名称")
    private String institutionName;

    @ApiModelProperty(value = "机构订单号")
    private String institutionOrderId;

    @ApiModelProperty(value = "交易类型")
    private String tradeType;

    @ApiModelProperty(value = "订单金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "订单币种")
    private String orderCurrency;

    @ApiModelProperty(value = "交易金额")
    private BigDecimal tradeAmount;

    @ApiModelProperty(value = "交易币种")
    private String tradeCurrency;

    @ApiModelProperty(value = "付款方式")
    private BigDecimal exchangeRate;

    @ApiModelProperty(value = "设备编号")
    private String deviceCode;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "退款状态")
    private Byte refundStatus;

    @ApiModelProperty(value = "交易状态")
    private String payMethod;

    @ApiModelProperty(value = "交易状态")
    private Byte tradeStatus;

    @ApiModelProperty(value = "交易状态")
    private Byte cancelStatus;

    @ApiModelProperty(value = "通道回调时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date channelCallbackTime;
}
