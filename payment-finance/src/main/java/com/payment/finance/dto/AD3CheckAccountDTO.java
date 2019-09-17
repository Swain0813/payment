package com.payment.finance.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @description: AD3对账单实体
 * @author: YangXu
 * @create: 2019-03-28 14:07
 **/
@Data
public class AD3CheckAccountDTO {

    @ApiModelProperty(value = "交易类型")
    private String type;

    @ApiModelProperty(value = "系统订单号")
    private String orderId;

    @ApiModelProperty(value = "通道流水号")
    private String channelNumber;

    @ApiModelProperty(value = "交易币种")
    private String tradeCurrency;

    @ApiModelProperty(value = "交易金额")
    private BigDecimal tradeAmount;

    @ApiModelProperty(value = "通道手续费")
    private BigDecimal channelRate;


    @ApiModelProperty(value = "交易完成时间")
    private String tradeTime;

    public AD3CheckAccountDTO(String[] s) {
        this.type = s[1];
        this.channelNumber = s[2];
        this.orderId = s[3];
        this.tradeCurrency = s[4];
        this.tradeAmount = BigDecimal.valueOf(Double.parseDouble(s[5].trim()));
        this.channelRate = BigDecimal.valueOf(Double.parseDouble(s[6].trim()));
        this.tradeTime = s[7];
    }
}
