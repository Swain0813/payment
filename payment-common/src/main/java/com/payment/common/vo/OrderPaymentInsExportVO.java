package com.payment.common.vo;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 机构系统汇款一览导出
 */
@Data
@ApiModel(value = "机构系统汇款一览导出", description = "机构系统汇款一览导出")
public class OrderPaymentInsExportVO {

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createTime;

    @ApiModelProperty(value = "汇款时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @ApiModelProperty(value = "商户批次号")
    private String institutionBatchNo;

    @ApiModelProperty(value = "机构编号")
    private String institutionCode;

    @ApiModelProperty(value = "机构名称")
    private String institutionName;

    @ApiModelProperty(value = "交易流水号")
    private String id;

    @ApiModelProperty(value = "机构上报时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date institutionOrderTime;

    @ApiModelProperty(value = "机构上报付款流水号")
    private String institutionOrderId;

    @ApiModelProperty(value = "订单币种")
    private String tradeCurrency;

    @ApiModelProperty(value = "订单金额")
    private BigDecimal tradeAmount;

    @ApiModelProperty(value = "汇款币种")
    private String paymentCurrency;

    @ApiModelProperty(value = "汇款金额")
    private BigDecimal paymentAmount;

    @ApiModelProperty(value = "汇率")
    private BigDecimal exchangeRate;

    @ApiModelProperty(value = "手续费")
    private BigDecimal fee;

    @ApiModelProperty(value = "收款人姓名")
    private String extend2;

    @ApiModelProperty(value = "收款人账号")
    private String bankAccountNumber;

    @ApiModelProperty(value = "收款人银行")
    private String bankAccountName;

    @ApiModelProperty(value = "汇款国家")
    private String receiverCountry;

    @ApiModelProperty(value = "汇款地址")
    private String receiverAdress;

    @ApiModelProperty(value = "银行code")
    private String bankCode;

    @ApiModelProperty(value = "swiftCode")
    private String swiftCode;

    @ApiModelProperty(value = "产品编号")
    private Integer productCode;

    @ApiModelProperty(value = "产品名称")
    private String productName;

    @ApiModelProperty(value = "通道流水号")
    private String channelNumber;

    @ApiModelProperty(value = "汇款状态")//1-待汇款 2-汇款中 3-汇款成功 4-汇款失败
    private Byte payoutStatus;

    @ApiModelProperty(value = "备注")
    private String remark;
}
