package com.payment.common.vo;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 机构系统汇款一览导出英文版
 */
@Data
@ApiModel(value = "机构系统汇款一览导出英文版", description = "机构系统汇款一览导出英文版")
public class OrderPaymentInsExportEnVO {

    @ApiModelProperty(value = "Creation Time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createTime;

    @ApiModelProperty(value = "Remittance Time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @ApiModelProperty(value = "Batch Number")
    private String institutionBatchNo;

    @ApiModelProperty(value = "Institution Id")
    private String institutionCode;

    @ApiModelProperty(value = "Institution Name")
    private String institutionName;

    @ApiModelProperty(value = "Remittance Order Number")
    private String id;

    @ApiModelProperty(value = "Order Time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date institutionOrderTime;

    @ApiModelProperty(value = "Institution Order Number")
    private String institutionOrderId;

    @ApiModelProperty(value = "Order Currency")
    private String tradeCurrency;

    @ApiModelProperty(value = "Order Amount")
    private BigDecimal tradeAmount;

    @ApiModelProperty(value = "Remittance Currency")
    private String paymentCurrency;

    @ApiModelProperty(value = "Remittance Amount")
    private BigDecimal paymentAmount;

    @ApiModelProperty(value = "Exchange Rate")
    private BigDecimal exchangeRate;

    @ApiModelProperty(value = "Fee")
    private BigDecimal fee;

    @ApiModelProperty(value = "Beneficiary Name")
    private String extend2;

    @ApiModelProperty(value = "Beneficiary Account")
    private String bankAccountNumber;

    @ApiModelProperty(value = "Beneficiary Bank")
    private String bankAccountName;

    @ApiModelProperty(value = "Remittance Country")
    private String receiverCountry;

    @ApiModelProperty(value = "Remittance Address")
    private String receiverAdress;

    @ApiModelProperty(value = "Bank code")
    private String bankCode;

    @ApiModelProperty(value = "swiftCode")
    private String swiftCode;

    @ApiModelProperty(value = "Product Code")
    private Integer productCode;

    @ApiModelProperty(value = "Product Name")
    private String productName;

    @ApiModelProperty(value = "Channel Number")
    private String channelNumber;

    @ApiModelProperty(value = "Remittance States")//1-待汇款 2-汇款中 3-汇款成功 4-汇款失败
    private Byte payoutStatus;

    @ApiModelProperty(value = "Remark")
    private String remark;

}
