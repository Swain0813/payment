package com.payment.permission.dto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel(value = "机构后台机构结算表导出英文版实体", description = "机构后台机构结算表导出英文版实体")
public class SettleOrderInsEnExport {

    @ApiModelProperty(value = "Batch No")
    private String batchNo;

    @ApiModelProperty(value = "Institution Id")
    private String institutionCode;

    @ApiModelProperty(value = "Institution Name")
    private String institutionName;

    @ApiModelProperty(value = "Withdraw Currency")
    private String txncurrency;

    @ApiModelProperty(value = "Withdraw Account")
    private BigDecimal txnamount;

    @ApiModelProperty(value = "Settle Account")
    private String accountCode;

    @ApiModelProperty(value = "Account Name")
    private String accountName;

    @ApiModelProperty(value = "Bank Name")
    private String bankName;

    @ApiModelProperty(value = "Swift Code")
    private String swiftCode;

    @ApiModelProperty(value = "Settle Currency")
    private String bankCurrency;

    @ApiModelProperty(value = "Bank Currency")
    private String bankCodeCurrency;

    @ApiModelProperty(value = "Settle Status")//交易状态：1-结算中 2-结算成功 3-结算失败
    private Byte tradeStatus;

    @ApiModelProperty(value = "Batch Transaction Fee")
    private BigDecimal tradeFee;

    @ApiModelProperty(value = "Fee Currency")
    private String feeCurrency;

    @ApiModelProperty(value = "Exchange Rate")
    private BigDecimal rate;

    @ApiModelProperty(value = "Total Settle Amount")//总结算金额=交易金额*交易汇率-批次交易手续费
    private BigDecimal totalSettleAmount;

    @ApiModelProperty(value = "Settle Completion Time")
    public Date updateTime;
}
