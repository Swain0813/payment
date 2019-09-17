package com.payment.trade.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @description: help2Pay汇款回调参数
 * @author: YangXu
 * @create: 2019-07-22 16:11
 **/
@Data
@ApiModel(value = "help2Pay汇款回调参数", description = "help2Pay汇款回调参数")
public class Help2PayOutCallbackDTO {

    @ApiModelProperty(value = "商户号")
    private String MerchantCode;

    @ApiModelProperty(value = "系统流水号")
    private String TransactionID;

    @ApiModelProperty(value = "币种")
    private String CurrencyCode;

    @ApiModelProperty(value = "金额")
    private String Amount;

    @ApiModelProperty(value = "时间")
    private String TransactionDatetime;

    @ApiModelProperty(value = "Key")
    private String Key;

    @ApiModelProperty(value = "付款状态")
    private String Status;

    @ApiModelProperty(value = "Message")
    private String Message;

    @ApiModelProperty(value = "商户的商户号")
    private String MemberCode;

    @ApiModelProperty(value = "交易类型")
    private String ID;

}
