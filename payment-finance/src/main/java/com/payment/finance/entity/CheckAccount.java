package com.payment.finance.entity;

import com.payment.common.base.BaseEntity;
import com.payment.common.entity.OrderRefund;
import com.payment.common.entity.Orders;
import com.payment.common.utils.DateToolUtils;
import com.payment.finance.dto.AD3CheckAccountDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;

@Data
@Entity
@Table(name = "check_account")
@ApiModel(value = "对账表", description = "对账表")
public class CheckAccount extends BaseEntity {


    @ApiModelProperty(value = "机构编号")
    @Column(name = "institution_code")
    private String institutionCode;

    @ApiModelProperty(value = "产品编号")
    @Column(name = "product_code")
    private Integer productCode;

    @ApiModelProperty(value = "机构名称")
    @Column(name = "institution_name")
    private String institutionName;

    @ApiModelProperty(value = "通道编号")
    @Column(name = "channel_code")
    private String channelCode;

    @ApiModelProperty(value = "平台订单id")
    @Column(name = "u_order_id")
    private String uOrderId;

    @ApiModelProperty(value = "平台通道流水号")
    @Column(name = "u_channel_number")
    private String uChannelNumber;

    @ApiModelProperty(value = "上游订单id")
    @Column(name = "c_order_id")
    private String cOrderId;

    @ApiModelProperty(value = "上游通道流水号")
    @Column(name = "c_channel_number")
    private String cChannelNumber;

    @ApiModelProperty(value = "交易类型 1：收单 2:退款")
    @Column(name = "trade_type")
    private Integer tradeType;

    @ApiModelProperty(value = "平台交易金额")
    @Column(name = "u_trade_amount")
    private BigDecimal uTradeAmount;

    @ApiModelProperty(value = "平台交易币种")
    @Column(name = "u_trade_currency")
    private String uTradeCurrency;

    @ApiModelProperty(value = "平台手续费")
    @Column(name = "u_fee")
    private BigDecimal uFee;

    @ApiModelProperty(value = "平台订单状态")
    @Column(name = "u_status")
    private Byte uStatus;

    @ApiModelProperty(value = "系统订单状态")
    @Column(name = "c_status")
    private Byte cStatus;

    @ApiModelProperty(value = "上游交易金额")
    @Column(name = "c_trade_amount")
    private BigDecimal cTradeAmount;

    @ApiModelProperty(value = "上游交易币种")
    @Column(name = "c_trade_currency")
    private String cTradeCurrency;

    @ApiModelProperty(value = "上游手续费")
    @Column(name = "c_fee")
    private BigDecimal cFee;

    @ApiModelProperty(value = "错误类型")
    @Column(name = "error_type")
    private Integer errorType; //1,待对账；2，差错处理 3，补单  4，对账成功

    @ApiModelProperty(value = "交易时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "trade_time")
    private Date tradeTime;

    @ApiModelProperty(value = "差错处理备注")
    @Column(name = "remark1")
    private String remark1;

    @ApiModelProperty(value = "差错复核备注")
    @Column(name = "remark2")
    private String remark2;


    public CheckAccount() {
    }

    public CheckAccount(Orders orders) {
        this.uOrderId = orders.getId();
        this.uChannelNumber = orders.getChannelNumber();
        this.cOrderId = null;
        this.cChannelNumber = null;
        this.tradeType = 1;
        this.uTradeAmount = orders.getTradeAmount()== null ? BigDecimal.ZERO : orders.getTradeAmount();
        this.uTradeCurrency = orders.getTradeCurrency();
        this.uFee = orders.getChannelFee()== null ? BigDecimal.ZERO : orders.getChannelFee();
        this.cTradeAmount = BigDecimal.ZERO;
        this.cTradeCurrency = null;
        this.cFee =  BigDecimal.ZERO;
        this.tradeTime = orders.getUpdateTime();
        this.uStatus = orders.getTradeStatus();
        this.institutionName = orders.getInstitutionName();
        this.institutionCode = orders.getInstitutionCode();
        this.productCode = orders.getProductCode();
    }

    public CheckAccount(int tradeType, AD3CheckAccountDTO ad3CheckAccountDTO) {
        this.uOrderId = null;
        this.uChannelNumber = null;
        this.cOrderId = ad3CheckAccountDTO.getOrderId();
        this.cChannelNumber = ad3CheckAccountDTO.getChannelNumber();
        this.tradeType = tradeType;
        this.uTradeAmount = BigDecimal.ZERO;
        this.uTradeCurrency =null;
        this.uFee = BigDecimal.ZERO;
        this.cTradeAmount = ad3CheckAccountDTO.getTradeAmount()== null ? BigDecimal.ZERO : ad3CheckAccountDTO.getTradeAmount();
        this.cTradeCurrency = ad3CheckAccountDTO.getTradeCurrency();
        this.cFee =  ad3CheckAccountDTO.getChannelRate()== null ? BigDecimal.ZERO : ad3CheckAccountDTO.getChannelRate();
        try {
            this.tradeTime = DateToolUtils.parseDate(ad3CheckAccountDTO.getTradeTime(),DateToolUtils.DATE_FORMAT_DATETIME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CheckAccount(Orders orders, AD3CheckAccountDTO ad3CheckAccountDTO) {
        this.uOrderId = orders.getId();
        this.uChannelNumber = orders.getChannelNumber();
        this.cOrderId = ad3CheckAccountDTO.getOrderId();
        this.cChannelNumber = ad3CheckAccountDTO.getChannelNumber();
        this.tradeType = 1;
        this.uTradeAmount = orders.getTradeAmount()== null ? BigDecimal.ZERO : orders.getTradeAmount();
        this.uTradeCurrency = orders.getTradeCurrency();
        this.uFee = orders.getChannelFee()== null ? BigDecimal.ZERO : orders.getChannelFee();
        this.cTradeAmount = ad3CheckAccountDTO.getTradeAmount()== null ? BigDecimal.ZERO : ad3CheckAccountDTO.getTradeAmount();
        this.cTradeCurrency = ad3CheckAccountDTO.getTradeCurrency();
        this.cFee =  ad3CheckAccountDTO.getChannelRate()== null ? BigDecimal.ZERO : ad3CheckAccountDTO.getChannelRate();
        this.tradeTime = orders.getUpdateTime();
        this.uStatus = orders.getTradeStatus();
        this.institutionName = orders.getInstitutionName();
        this.institutionCode = orders.getInstitutionCode();
        this.productCode = orders.getProductCode();
    }

    public CheckAccount( OrderRefund orderRefund) {
        this.uOrderId = orderRefund.getId();
        this.uChannelNumber = orderRefund.getChannelNumber();
        this.cOrderId = null;
        this.cChannelNumber = null;
        this.tradeType = 2;
        this.uTradeAmount = orderRefund.getTradeAmount()== null ? BigDecimal.ZERO : orderRefund.getTradeAmount();
        this.uTradeCurrency = orderRefund.getTradeCurrency();
        this.uFee =  BigDecimal.ZERO;
        this.cTradeAmount = BigDecimal.ZERO;
        this.cTradeCurrency = null;
        this.cFee =  BigDecimal.ZERO;
        this.tradeTime = orderRefund.getUpdateTime();
        this.uStatus = orderRefund.getRefundStatus();
        this.institutionName = orderRefund.getInstitutionName();
        this.institutionCode = orderRefund.getInstitutionCode();
        this.productCode = orderRefund.getProductCode();
    }
    public CheckAccount( OrderRefund orderRefund, AD3CheckAccountDTO ad3CheckAccountDTO) {
        this.uOrderId = orderRefund.getId();
        this.uChannelNumber = orderRefund.getChannelNumber();
        this.cOrderId = ad3CheckAccountDTO.getOrderId();
        this.cChannelNumber = ad3CheckAccountDTO.getChannelNumber();
        this.tradeType = 2;
        this.uTradeAmount = orderRefund.getTradeAmount()== null ? BigDecimal.ZERO : orderRefund.getTradeAmount();
        this.uTradeCurrency = orderRefund.getTradeCurrency();
        this.uFee = orderRefund.getChannelFee()== null ? BigDecimal.ZERO : orderRefund.getChannelFee();
        this.cTradeAmount = ad3CheckAccountDTO.getTradeAmount()== null ? BigDecimal.ZERO : ad3CheckAccountDTO.getTradeAmount();
        this.cTradeCurrency = ad3CheckAccountDTO.getTradeCurrency();
        this.cFee =  ad3CheckAccountDTO.getChannelRate() == null ? BigDecimal.ZERO : ad3CheckAccountDTO.getChannelRate();
        this.tradeTime = orderRefund.getUpdateTime();
        this.uStatus = orderRefund.getRefundStatus();
        this.institutionName = orderRefund.getInstitutionName();
        this.institutionCode = orderRefund.getInstitutionCode();
        this.productCode = orderRefund.getProductCode();
    }
}
