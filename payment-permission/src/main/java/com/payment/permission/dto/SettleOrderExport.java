package com.payment.permission.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel(value = "机构结算表导出实体", description = "机构结算表导出实体")
public class SettleOrderExport {

    @ApiModelProperty(value = "批次号")
    private String batchNo;

    @ApiModelProperty(value = "机构编号")
    private String institutionCode;

    @ApiModelProperty(value = "机构名称")
    private String institutionName;

    @ApiModelProperty(value = "交易币种")
    private String txncurrency;

    @ApiModelProperty(value = "交易金额")
    private BigDecimal txnamount;

    @ApiModelProperty(value = "结算账户")
    private String accountCode;

    @ApiModelProperty(value = "账户名")
    private String accountName;

    @ApiModelProperty(value = "银行名称")
    private String bankName;

    @ApiModelProperty(value = "Swift Code")
    private String swiftCode;

    @ApiModelProperty(value = "Iban")
    private String iban;

    @ApiModelProperty(value = "bank code")
    private String bankCode;

    @ApiModelProperty(value = "结算币种")
    private String bankCurrency;

    @ApiModelProperty(value = "银行卡币种")
    private String bankCodeCurrency;

    @ApiModelProperty(value = "结算状态")//交易状态：1-结算中 2-结算成功 3-结算失败
    private Byte tradeStatus;

    @ApiModelProperty(value = "批次交易手续费")
    private BigDecimal tradeFee;

    @ApiModelProperty(value = "手续费币种")
    private String feeCurrency;

    @ApiModelProperty(value = "交易汇率")
    private BigDecimal rate;

//    @ApiModelProperty(value = "结算金额")//结算金额=交易金额*交易汇率
//    private BigDecimal settleAmount;

    @ApiModelProperty(value = "批次总结算金额")//
    private BigDecimal totalSettleAmount;

    @ApiModelProperty(value = "结算通道")
    private String settleChannel;

    @ApiModelProperty(value = "中间行银行编码")
    private String intermediaryBankCode;

    @ApiModelProperty(value = "中间行银行名称")
    private String intermediaryBankName;

    @ApiModelProperty(value = "中间行银行地址")
    private String intermediaryBankAddress;

    @ApiModelProperty(value = "中间行银行账户")
    private String intermediaryBankAccountNo;

    @ApiModelProperty(value = "中间行银行城市")
    private String intermediaryBankCountry;

    @ApiModelProperty(value = "中间行银行其他code")
    private String intermediaryOtherCode;

    @ApiModelProperty(value = "机构地址")
    private String institutionAdress;

    @ApiModelProperty(value = "机构邮政")
    private String institutionPostalCode;

    @ApiModelProperty(value = "审核时间")
    public Date updateTime;

    @ApiModelProperty(value = "审核人")
    private String modifier;
}
