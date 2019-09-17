package com.payment.permission.dto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel(value = "机构后台机构结算表导出实体", description = "机构后台机构结算表导出实体")
public class SettleOrderInsExport {

    @ApiModelProperty(value = "批次号")
    private String batchNo;

    @ApiModelProperty(value = "机构编号")
    private String institutionCode;

    @ApiModelProperty(value = "机构名称")
    private String institutionName;

    @ApiModelProperty(value = "提款币种")
    private String txncurrency;

    @ApiModelProperty(value = "提款金额")
    private BigDecimal txnamount;

    @ApiModelProperty(value = "结算账户")
    private String accountCode;

    @ApiModelProperty(value = "账户名")
    private String accountName;

    @ApiModelProperty(value = "银行名称")
    private String bankName;

    @ApiModelProperty(value = "Swift Code")
    private String swiftCode;

    @ApiModelProperty(value = "结算币种")
    private String bankCurrency;

    @ApiModelProperty(value = "银行卡币种")
    private String bankCodeCurrency;

    @ApiModelProperty(value = "交易状态")//交易状态：1-结算中 2-结算成功 3-结算失败
    private Byte tradeStatus;

    @ApiModelProperty(value = "批次交易手续费")
    private BigDecimal tradeFee;

    @ApiModelProperty(value = "手续费币种")
    private String feeCurrency;

    @ApiModelProperty(value = "交易汇率")
    private BigDecimal rate;

    @ApiModelProperty(value = "批次总结算金额")//总结算金额=交易金额*交易汇率-批次交易手续费
    private BigDecimal totalSettleAmount;

    @ApiModelProperty(value = "结算完成时间")
    public Date updateTime;
}
