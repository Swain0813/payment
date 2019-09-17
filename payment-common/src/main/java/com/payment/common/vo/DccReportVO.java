package com.payment.common.vo;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @description: DCC报表查询输出实体
 * @author: XuWenQi
 * @create: 2019-07-26 14:37
 **/
@Data
@ApiModel(value = "DCC报表查询输出实体", description = "DCC报表查询输出实体")
public class DccReportVO {

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @ApiModelProperty(value = "商户编号")
    private String institutionCode;

    @ApiModelProperty(value = "商户名称")
    private String institutionName;

    @ApiModelProperty(value = "商户订单号")
    private String institutionOrderId;

    @ApiModelProperty(value = "订单id")
    private String id;

    @ApiModelProperty(value = "订单币种")
    private String orderCurrency;

    @ApiModelProperty(value = "订单金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "交易币种")
    private String tradeCurrency;

    @ApiModelProperty(value = "原始汇率")
    private String oldExchangeRate;

    @ApiModelProperty(value = "交易金额")
    private BigDecimal tradeAmount;

    @ApiModelProperty(value = "换汇汇率")
    private BigDecimal exchangeRate;

    @ApiModelProperty(value = "浮动率")
    private BigDecimal floatRate;

    @ApiModelProperty(value = "浮动金额")
    private BigDecimal floatAmount;

    @ApiModelProperty(value = "通道名称")
    private String channelName;

    @ApiModelProperty(value = "支付完成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date channelCallbackTime;

    @ApiModelProperty(value = "交易状态")
    private Byte tradeStatus;
}
