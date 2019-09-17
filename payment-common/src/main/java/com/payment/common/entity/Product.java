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
@Table(name = "product")
@ApiModel(value = "产品", description = "产品")
public class Product extends BaseEntity {

    @ApiModelProperty(value = "产品编号")
    @Column(name = "product_code")
    private Integer productCode;

    @ApiModelProperty(value = "交易类型（1-收、2-付）")
    @Column(name = "trans_type")
    private Byte transType;

    @ApiModelProperty(value = "支付方式(银联，网银，...）")
    @Column(name = "pay_type")
    private String payType;

    @ApiModelProperty(value = "交易场景(1-线上pc端 2-线上移动端 3-线下移动端）")
    @Column(name = "trade_direction")
    private Byte tradeDirection;

    @ApiModelProperty(value = "支付币种")
    @Column(name = "currency")
    private String currency;

    @ApiModelProperty(value = "单笔交易限额")
    @Column(name = "limit_amount")
    private BigDecimal limitAmount;

    @ApiModelProperty(value = "日累计交易笔数")
    @Column(name = "daily_trading_count")
    private Integer dailyTradingCount;

    @ApiModelProperty(value = "日累计交易总额")
    @Column(name = "daily_total_amount")
    private BigDecimal dailyTotalAmount;

    @ApiModelProperty(value = "启用禁用")
    @Column(name = "enabled")
    private Boolean enabled;

    @ApiModelProperty(value = "产品图标")
    @Column(name = "product_img")
    private String productImg;

}
