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
@Table(name = "share_benefit_logs")
@ApiModel(value = "分润流水表", description = "分润流水表")
public class ShareBenefitLogs extends BaseEntity {

    @ApiModelProperty(value = "原订单id")
    @Column(name = "order_id")
    private String orderId;

    @ApiModelProperty(value = "商户编号")
    @Column(name = "institution_code")
    private String institutionCode;

    @ApiModelProperty(value = "商户名称")
    @Column(name = "institution_name")
    private String institutionName;

    @ApiModelProperty(value = "代理商编号")
    @Column(name = "agent_code")
    private String agentCode;

    @ApiModelProperty(value = "代理商名称")
    @Column(name = "agent_name")
    private String agentName;

    @ApiModelProperty(value = "原订单币种")
    @Column(name = "trade_currency")
    private String tradeCurrency;

    @ApiModelProperty(value = "原订单今额")
    @Column(name = "trade_amount")
    private Double tradeAmount;

    @ApiModelProperty(value = "代理商手续费")
    @Column(name = "fee")
    private Double fee;

    @ApiModelProperty(value = "分润金额")
    @Column(name = "share_benefit")
    private Double shareBenefit;

    @ApiModelProperty(value = "分润状态")//1:待分润，2：已分润
    @Column(name = "is_share")
    private Byte isShare;

    @ApiModelProperty(value = "分润模式")//分润模式 1-分成 2-费用差
    @Column(name = "divided_mode")
    private Byte dividedMode;

    @ApiModelProperty(value = "分润比例")
    @Column(name = "divided_ratio")
    private BigDecimal dividedRatio;

    @ApiModelProperty(value = "订单类型")//1 收单 2,付款
    @Column(name = "extend1")
    private String extend1;

    @ApiModelProperty(value = "备注2")
    @Column(name = "extend2")
    private String extend2;

    @ApiModelProperty(value = "备注3")
    @Column(name = "extend3")
    private String extend3;

    @ApiModelProperty(value = "备注4")
    @Column(name = "extend4")
    private String extend4;

    @ApiModelProperty(value = "备注5")
    @Column(name = "extend5")
    private String extend5;

    @ApiModelProperty(value = "备注6")
    @Column(name = "extend6")
    private String extend6;



}
