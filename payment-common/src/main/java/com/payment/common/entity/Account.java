package com.payment.common.entity;

import com.payment.common.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "account")
@ApiModel(value = "账户信息表", description = "账户信息表")
public class Account extends BaseEntity {

    @ApiModelProperty(value = "机构code")
    @Column(name = "institution_id")
    private String institutionId;

    @ApiModelProperty(value = "机构名称")
    @Column(name = "institution_name")
    private String institutionName;

    @ApiModelProperty(value = "账户编号")
    @Column(name = "account_code")
    private String accountCode;

    @ApiModelProperty(value = "币种")
    @Column(name = "currency")
    private String currency;

    @ApiModelProperty(value = "结算账户余额")
    @Column(name = "settle_balance")
    private BigDecimal settleBalance;

    @ApiModelProperty(value = "清算账户余额")
    @Column(name = "clear_balance")
    private BigDecimal clearBalance;

    @ApiModelProperty(value = "冻结账户余额")
    @Column(name = "freeze_balance")
    private BigDecimal freezeBalance;

    @ApiModelProperty(value = "版本号")
    @Column(name = "version")
    private Long version;

    @ApiModelProperty(value = "禁用启用")
    @Column(name = "enabled")
    private Boolean enabled;


}
