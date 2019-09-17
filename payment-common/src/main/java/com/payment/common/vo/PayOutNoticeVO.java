package com.payment.common.vo;

import com.payment.common.entity.OrderPayment;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @description: 汇款通知‘
 * @author: YangXu
 * @create: 2019-08-14 13:58
 **/
@Data
@ApiModel(value = "汇款通知VO", description = "汇款通知VO")
public class PayOutNoticeVO {

    @ApiModelProperty(value = "系统流水号")
    private String referenceNo;

    @ApiModelProperty(value = "机构编号")
    private String institutionId;

    @ApiModelProperty(value = "机构名称")
    private String institutionName;

    @ApiModelProperty(value = "机构上报付款批次号")
    private String institutionBatchNo;

    @ApiModelProperty(value = "机构上报付款流水号")
    private String orderNo;

    @ApiModelProperty(value = "订单币种")
    private String orderCurrency;

    @ApiModelProperty(value = "订单金额")
    private BigDecimal orderAmount;

    @ApiModelProperty(value = "汇款币种")
    private String paymentCurrency;

    @ApiModelProperty(value = "汇款金额")
    private BigDecimal paymentAmount;

    @ApiModelProperty(value = "汇款银行名称")
    private String bankAccountName;

    @ApiModelProperty(value = "汇款银行卡号")
    private String bankAccountNumber;

    @ApiModelProperty(value = "汇款国家")
    private String receiverCountry;

    @ApiModelProperty(value = "汇款地址")
    private String receiverAdress;

    @ApiModelProperty(value = "银行code")
    private String bankCode;

    @JsonIgnore
    @ApiModelProperty(value = "服务器回调地址")
    private String serverUrl;

    @ApiModelProperty(value = "汇款状态:1-待汇款 2-汇款中 3-汇款成功 4-汇款失败")
    private Byte payoutStatus;

    @ApiModelProperty(value = "签名")
    private String sign;


    public PayOutNoticeVO() {
    }

    public PayOutNoticeVO(OrderPayment orderPayment) {
        this.referenceNo = orderPayment.getId();
        this.institutionId = orderPayment.getInstitutionCode();
        this.institutionName = orderPayment.getInstitutionName();
        this.institutionBatchNo = orderPayment.getInstitutionBatchNo();
        this.orderNo = orderPayment.getInstitutionOrderId();
        this.orderCurrency = orderPayment.getTradeCurrency();
        this.orderAmount = orderPayment.getTradeAmount();
        this.paymentCurrency = orderPayment.getPaymentCurrency();
        this.paymentAmount = orderPayment.getPaymentAmount();
        this.bankAccountName = orderPayment.getBankAccountName();
        this.bankAccountNumber = orderPayment.getBankAccountNumber();
        this.receiverCountry = orderPayment.getReceiverCountry();
        this.receiverAdress = orderPayment.getReceiverAdress();
        this.bankCode = orderPayment.getBankCode();
        this.serverUrl = orderPayment.getServerUrl();
        this.payoutStatus = orderPayment.getPayoutStatus();
        //this.sign = sign;
    }
}
