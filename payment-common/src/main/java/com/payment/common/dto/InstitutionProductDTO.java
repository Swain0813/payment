package com.payment.common.dto;

import com.payment.common.base.BasePageHelper;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @description:
 * @author: YangXu
 * @create: 2019-01-29 14:20
 **/
@Data
@ApiModel(value = "机构添加产品实体", description = "机构添加产品实体")
public class InstitutionProductDTO extends BasePageHelper {

    @ApiModelProperty(value = "id")
    private String insProductId;

    @ApiModelProperty(value = "产品id")
    private String productId;

    @ApiModelProperty(value = "机构id")
    private String institutionId;

    @ApiModelProperty(value = "机构Code")
    private String institutionCode;

    @ApiModelProperty(value = "机构名称")
    private String institutionName;

    @ApiModelProperty(value = "币种")
    private String currency;

    @ApiModelProperty(value = "分润比例")
    private BigDecimal dividedRatio;

    @ApiModelProperty(value = "分润模式")
    private Byte dividedMode;

    @ApiModelProperty(value = "交易类型")
    private Byte transType;

    @ApiModelProperty(value = "支付类型")
    private String payType;

    @ApiModelProperty(value = "费率类型")
    private String rateType;

    @ApiModelProperty(value = "费率最大值")
    private BigDecimal maxRate = BigDecimal.valueOf(999999999);

    @ApiModelProperty(value = "费率最小值")
    private BigDecimal minRate = BigDecimal.ZERO;

    @ApiModelProperty(value = "费率")
    private BigDecimal rate;

    @ApiModelProperty(value = "浮动率")
    private BigDecimal floatRate;

    @ApiModelProperty(value = "结算周期")
    private String settleCycle;

    @ApiModelProperty(value = "附加值")
    private BigDecimal addValue;

    @ApiModelProperty(value = "单笔限额")
    private BigDecimal limitAmount;

    @ApiModelProperty(value = "日交易总笔数限额")
    private Integer dailyTradingCount;

    @ApiModelProperty(value = "日交易总金额限额")
    private BigDecimal dailyTotalAmount;

    @ApiModelProperty(value = "启用禁用")
    private Boolean enabled;

    @ApiModelProperty(value = "审核信息状态")
    private Byte auditInfoStatus;

    @ApiModelProperty(value = "产品支付限额审核状态")
    private Byte auditLimitStatus;

    @ApiModelProperty(value = "手续费付款方，1：内扣 2：外扣")
    private Byte feePayer;

    @ApiModelProperty(value = "生效时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date productEffectTime;

    @ApiModelProperty(value = "语言")
    private String language;

    @ApiModelProperty(value = "退款费率类型")
    private String refundRateType;

    @ApiModelProperty(value = "退款手续费最小值")
    private BigDecimal refundMinTate = BigDecimal.ZERO;

    @ApiModelProperty(value = "退款手续费最大值")
    private BigDecimal refundMaxTate = BigDecimal.valueOf(999999999);

    @ApiModelProperty(value = "退款费率")
    private BigDecimal refundRate;

    @ApiModelProperty(value = "退款附加值")
    private BigDecimal refundAddValue = BigDecimal.ZERO;

}
