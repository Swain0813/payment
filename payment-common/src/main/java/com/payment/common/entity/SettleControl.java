package com.payment.common.entity;

import com.payment.common.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Table;
import java.math.BigDecimal;

@Data
@Table(name = "settle_control")
@ApiModel(value = "结算控制", description = "结算控制")
public class SettleControl extends BaseEntity {

    @ApiModelProperty(value = "账户ID")
    @Column(name = "account_id")
    private String accountId;

    @ApiModelProperty(value = "最小起结金额")
    @Column(name = "min_settle_amount")
    private BigDecimal minSettleAmount;

    @ApiModelProperty(value = "自动结算 0关闭 1启用")
    @Column(name = "settle_switch")
    private Boolean settleSwitch;

    @ApiModelProperty(value = "启用禁用")
    @Column(name = "enabled")
    private Boolean enabled;
}
