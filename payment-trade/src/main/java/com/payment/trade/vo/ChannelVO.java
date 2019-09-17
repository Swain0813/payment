package com.payment.trade.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel(value = "通道输出实体", description = "通道输出实体")
public class ChannelVO {

    @ApiModelProperty(value = "通道id")
    private String channelId;

    @ApiModelProperty(value = "ad3的支付code")
    private String payCode;//ad3的支付code


    @ApiModelProperty(value = "产品id")
    private String channelProductId;

    @ApiModelProperty(value = "通道币种")
    private String channelCurrency;

    @ApiModelProperty(value = "通道币种图标")
    private String channelCurrencyIcon;

    @ApiModelProperty(value = "通道中文名称")
    private String channelCnName;

    @ApiModelProperty(value = "通道编码")
    private String channelCode;

    @ApiModelProperty(value = "通道logo")
    private String channelImg;

    @ApiModelProperty(value = "银行机构代码")
    private String issuerId;

    @ApiModelProperty(value = "通道url")
    private String channelUrl;

    @ApiModelProperty(value = "国家")
    private String country;

    @ApiModelProperty(value = "通道支付方式CODE")
    private String channelPayType;

    @ApiModelProperty(value = "通道支付方式名称")
    private String channelPayTypeName;

    @ApiModelProperty(value = "结算周期")
    private String settleCycle;

    @ApiModelProperty(value = "通道手续费")
    private BigDecimal channelFee;

    @ApiModelProperty(value = "通道费率")
    private BigDecimal channelRate;

    @ApiModelProperty(value = "通道手续费类型")////dic_7_1-单笔费率,dic_7_2-单笔定额
    private String channelFeeType;

    @ApiModelProperty(value = "通道费率最小值")
    private BigDecimal channelMinRate;

    @ApiModelProperty(value = "通道费率最大值")
    private BigDecimal channelMaxRate;

    @ApiModelProperty(value = "通道网关费率")
    private BigDecimal channelGatewayRate;

    @ApiModelProperty(value = "通道网关手续费")
    private BigDecimal channelGatewayFee;

    @ApiModelProperty(value = "通道网关费率最小值")
    private BigDecimal channelGatewayMinRate;

    @ApiModelProperty(value = "通道网关费率最大值")
    private BigDecimal channelGatewayMaxRate;

    @ApiModelProperty(value = "通道网关手续费类型")
    private String channelGatewayFeeType;////dic_7_1-单笔费率,dic_7_2-单笔定额

    @ApiModelProperty(value = "通道网关是否收取")
    private Byte channelGatewayCharge;//1-收 2-不收

    @ApiModelProperty(value = "通道网关收取状态")
    private Byte channelGatewayStatus;// 1-成功时收取 2-失败时收取 3-全收

    @ApiModelProperty(value = "权重")
    private String sort;

    @ApiModelProperty(value = "不同币种的默认值")
    private String defaultValue;// 1-成功时收取 2-失败时收取 3-全收
}
