package com.payment.institution.entity;

import com.payment.common.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Table;

/**
 * 支付方式管理
 */
@Data
@Table(name = "payment_mode")
@ApiModel(value = "支付方式管理", description = "支付方式")
public class PaymentMode extends BaseEntity {

    @ApiModelProperty(value = "支付方式")
    @Column(name = "pay_type")
    private String payType;

    @ApiModelProperty(value = "交易方式")
    @Column(name = "deal_type")
    private String dealType;

    @ApiModelProperty(value = "图标")
    @Column(name = "icon")
    private String icon;

    @ApiModelProperty(value = "启用禁用")
    @Column(name = "enabled")
    private Boolean enabled;

    @ApiModelProperty(value = "语种")
    @Column(name = "language")
    private String language;

}
