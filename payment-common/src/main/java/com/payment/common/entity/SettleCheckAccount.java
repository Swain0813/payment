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
@Table(name = "settle_check_account")
@ApiModel(value = "机构对账结算单", description = "机构对账结算单")
public class SettleCheckAccount extends BaseEntity {

    @ApiModelProperty(value = "对账区间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Column(name = "check_time")
    private Date checkTime;

    @ApiModelProperty(value = "币种")
    @Column(name = "currency")
    private String currency;

    @ApiModelProperty(value = "机构编号")
    @Column(name = "institution_code")
    private String institutionCode;

    @ApiModelProperty(value = "笔数")
    @Column(name = "count")
    private int count;

    @ApiModelProperty(value = "金额")
    @Column(name = "amount")
    private BigDecimal amount = BigDecimal.ZERO;

    @ApiModelProperty(value = "手续费")
    @Column(name = "fee")
    private BigDecimal fee = BigDecimal.ZERO;

    @ApiModelProperty(value = "期初金额")
    @Column(name = "initial_amount")
    private BigDecimal initialAmount = BigDecimal.ZERO;

    @ApiModelProperty(value = "期末金额")
    @Column(name = "final_amount")
    private BigDecimal finalAmount = BigDecimal.ZERO;


}
