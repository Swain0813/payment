package com.payment.common.entity;

import com.payment.common.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@Table(name = "trade_check_account_detail")
@ApiModel(value = "交易对账详细表", description = "交易对账详细表")
public class TradeCheckAccountDetail extends BaseEntity {

    @ApiModelProperty("订单流水号")
    @Column(name = "order_id")
    private String orderId;

    @ApiModelProperty("机构编号")
    @Column(name = "institution_code")
    private String institutionCode;

    @ApiModelProperty("订单创建时间")
    @Column(name = "order_create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date orderCreateTime;

    @ApiModelProperty("设备编号")
    @Column(name = "device_code")
    private String deviceCode;

    @ApiModelProperty("机构订单号")
    @Column(name = "institution_order_id")
    private String institutionOrderId;

    @ApiModelProperty("支付方式")
    @Column(name = "pay_type")
    private String payType;

    @ApiModelProperty("订单币种")
    @Column(name = "order_currency")
    private String orderCurrency;

    @ApiModelProperty("订单金额")
    @Column(name = "amount")
    private BigDecimal amount;

    @ApiModelProperty("交易类型")
    @Column(name = "trade_type")
    private Byte tradeType;

    @ApiModelProperty("交易状态")
    @Column(name = "trade_status")
    private Byte tradeStatus;

    @ApiModelProperty("撤销状态")
    @Column(name = "cancel_status")
    private Byte cancelStatus;

    @ApiModelProperty("退款状态")
    @Column(name = "refund_status")
    private Byte refundStatus;

    @ApiModelProperty("支付完成时间")
    @Column(name = "pay_finish_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date payFinishTime;

    @ApiModelProperty("费率类型")
    @Column(name = "rate_type")
    private String rateType;

    @ApiModelProperty("费率")
    @Column(name = "rate")
    private BigDecimal rate;

    @ApiModelProperty("手续费")
    @Column(name = "fee")
    private BigDecimal fee;

    @ApiModelProperty("附加值")
    @Column(name = "add_value")
    private BigDecimal addValue;

}
