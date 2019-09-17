package com.payment.institution.entity;

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
@Table(name = "institution_product")
@ApiModel(value = "机构产品中间表", description = "机构产品中间表")
public class InstitutionProduct extends BaseEntity {

    @ApiModelProperty(value = "机构id （uuid）")
    @Column(name = "institution_id")
    private String institutionId;

    @ApiModelProperty(value = "产品id （uuid）")
    @Column(name = "product_id")
    private String productId;

    @ApiModelProperty(value = "费率类型")
    @Column(name = "rate_type")
    private String rateType;

    @ApiModelProperty(value = "费率最小值")
    @Column(name = "min_tate")
    private BigDecimal minTate;

    @ApiModelProperty(value = "费率最大值")
    @Column(name = "max_tate")
    private BigDecimal maxTate;

    @ApiModelProperty(value = "费率")
    @Column(name = "rate")
    private BigDecimal rate;

    @ApiModelProperty(value = "浮动率")
    @Column(name = "float_rate")
    private BigDecimal floatRate;

    @ApiModelProperty(value = "附加值")
    @Column(name = "add_value")
    private BigDecimal addValue;

    @ApiModelProperty(value = "单笔交易限额")
    @Column(name = "limit_amount")
    private BigDecimal limitAmount;

    @ApiModelProperty(value = "日累计交易笔数")
    @Column(name = "daily_trading_count")
    private Integer dailyTradingCount;

    @ApiModelProperty(value = "日累计交易总额")
    @Column(name = "daily_total_amount")
    private BigDecimal dailyTotalAmount;

    @ApiModelProperty(value = "手续费付款方 1 ：内扣 2 ：外扣")
    @Column(name = "fee_payer")
    private Byte feePayer;

    @ApiModelProperty(value = "分润比例")
    @Column(name = "divided_ratio")
    private BigDecimal dividedRatio;

    @ApiModelProperty(value = "分润模式 1-分成 2-费用差")
    @Column(name = "divided_mode")
    private Byte dividedMode;

    @ApiModelProperty(value = "审核限额状态")
    @Column(name = "audit_limit_status")
    private Byte auditLimitStatus;//1-待审核 2-审核通过 3-审核不通过

    @ApiModelProperty(value = "审核信息状态")
    @Column(name = "audit_info_status")
    private Byte auditInfoStatus;//1-待审核 2-审核通过 3-审核不通过

    @ApiModelProperty(value = "审核限额备注")
    @Column(name = "audit_limit_remark")
    private String auditLimitRemark;

    @ApiModelProperty(value = "审核信息备注")
    @Column(name = "audit_info_remark")
    private String auditInfoRemark;

    @ApiModelProperty(value = "结算周期 (T+0,T+3,...)")
    @Column(name = "settle_cycle")
    private String settleCycle;

    @ApiModelProperty(value = "启用禁用")
    @Column(name = "enabled")
    private Boolean enabled;

    @ApiModelProperty(value = "退款费率类型")
    @Column(name = "refund_rate_type")
    private String refundRateType;

    @ApiModelProperty(value = "退款手续费最小值")
    @Column(name = "refund_min_tate")
    private BigDecimal refundMinTate;

    @ApiModelProperty(value = "退款手续费最大值")
    @Column(name = "refund_max_tate")
    private BigDecimal refundMaxTate;

    @ApiModelProperty(value = "退款费率")
    @Column(name = "refund_rate")
    private BigDecimal refundRate;

    @ApiModelProperty(value = "退款附加值")
    @Column(name = "refund_add_value")
    private BigDecimal refundAddValue;

}
