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
@Table(name = "trade_check_account")
@ApiModel(value = "交易对账总表", description = "交易对账总表")
public class TradeCheckAccount extends BaseEntity {

    @ApiModelProperty(value = "机构编号")
    @Column(name = "institution_code")
    private String institutionCode;

    @ApiModelProperty("起始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "begin_time")
    private Date beginTime;

    @ApiModelProperty("结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "end_time")
    private Date endTime;

    @ApiModelProperty("币种")
    @Column(name = "currency")
    private String currency;

    @ApiModelProperty("总交易金额")
    @Column(name = "total_trade_amount")
    private BigDecimal totalTradeAmount;

    @ApiModelProperty("总交易笔数")
    @Column(name = "total_trade_count")
    private Integer totalTradeCount;

    @ApiModelProperty("总退款金额")
    @Column(name = "total_refund_amount")
    private BigDecimal totalRefundAmount;

    @ApiModelProperty("总退款笔数")
    @Column(name = "total_refund_count")
    private Integer totalRefundCount;

    @ApiModelProperty("总支出金额")
    @Column(name = "total_expenditure_amount")
    private BigDecimal totalExpenditureAmount;

    @ApiModelProperty("总支出笔数")
    @Column(name = "total_expenditure_count")
    private Integer totalExpenditureCount;

    @ApiModelProperty("手续费")
    @Column(name = "fee")
    private BigDecimal fee;

}
