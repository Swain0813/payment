package com.payment.common.entity;

import com.payment.common.base.BaseEntity;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.constant.TradeConstant;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "reconciliation")
@ApiModel(value = "调账记录", description = "调账记录")
public class Reconciliation extends BaseEntity {

    @ApiModelProperty(value = "原订单id")
    @Column(name = "order_id")
    private String orderId;

    @ApiModelProperty(value = "退款单id")
    @Column(name = "refund_order_id")
    private String refundOrderId;

    @ApiModelProperty(value = "通道流水号")
    @Column(name = "channel_number")
    private String channelNumber;

    @ApiModelProperty(value = "商户订单号")
    @Column(name = "institution_order_id")
    private String institutionOrderId;

    @ApiModelProperty(value = "商户编号")
    @Column(name = "institution_code")
    private String institutionCode;

    @ApiModelProperty(value = "商户名称")
    @Column(name = "institution_name")
    private String institutionName;

    @ApiModelProperty(value = "请求金额")
    @Column(name = "amount")
    private BigDecimal amount;

    @ApiModelProperty(value = "请求币种")
    @Column(name = "order_currency")
    private String orderCurrency;

    @ApiModelProperty(value = "换汇汇率")
    @Column(name = "exchange_rate")
    private BigDecimal exchangeRate;

    @ApiModelProperty(value = "交易币种")
    @Column(name = "trade_currency")
    private String tradeCurrency;

    @ApiModelProperty(value = "交易金额")
    @Column(name = "trade_amount")
    private BigDecimal tradeAmount;

    @ApiModelProperty(value = "调账类型")
    @Column(name = "reconciliation_type")
    private int reconciliationType; //1; //调入 2;//调出 3;//冻结 4;//解冻

    @ApiModelProperty(value = "签名")
    @Column(name = "sign")
    private String sign;

    @ApiModelProperty(value = "调账状态")//调账状态 1-待调账 2-调账成功 3-调账失败, 4-待冻结 5-冻结成功 6-冻结失败, 7-待解冻 8-解冻成功 9-解冻失败
    @Column(name = "status")
    private int status;

    @Column(name = "remark1")
    private String remark1;

    @Column(name = "remark2")
    private String remark2;

    @Column(name = "remark3")
    private String remark3;

    @ApiModelProperty(value = "资金变动类型 1-调账 2-资金冻结 3-资金解冻")
    @Column(name = "change_type")
    private Byte changeType;

    @ApiModelProperty(value = "冻结类型 1-冻结 2-预约冻结")
    @Column(name = "freeze_type")
    private Byte freezeType;

    @ApiModelProperty(value = "入账类型 1-冻结户 2-保证金户")
    @Column(name = "account_type")
    private Byte accountType;


    public Reconciliation() {
    }

    public Reconciliation(OrderPayment orderPayment) {
        //this.orderId = orderPayment.getId();
        this.refundOrderId = orderPayment.getId();
        this.channelNumber = orderPayment.getChannelNumber();
        this.institutionOrderId = orderPayment.getInstitutionOrderId();
        this.institutionCode = orderPayment.getInstitutionCode();
        this.institutionName = orderPayment.getInstitutionName();
        this.amount = orderPayment.getTradeAmount();
        this.orderCurrency = orderPayment.getTradeCurrency();
        this.exchangeRate = orderPayment.getExchangeRate();
        this.tradeCurrency = orderPayment.getTradeCurrency();
        this.tradeAmount = orderPayment.getTradeAmount();
        this.reconciliationType = AsianWalletConstant.RECONCILIATION_IN;
        //this.sign = sign;
        this.status = TradeConstant.RECONCILIATION_WAIT;
        //this.remark1 = remark1;
        //this.remark2 = remark2;
        //this.remark3 = remark3;
        //this.changeType = changeType;
        //this.freezeType = freezeType;
        //this.accountType = accountType;
    }
}
