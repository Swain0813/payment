package com.payment.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @description:
 * @author: YangXu
 * @create: 2019-01-29 15:01
 **/
@Data
@ApiModel(value = "InstitutionProductVO", description = "机构产品输出实体")
public class InstitutionProductVO {

    @ApiModelProperty(value = "产品id")
    private String id;

    @ApiModelProperty(value = "机构code")
    private String institutionCode;

    @ApiModelProperty(value = "机构名称")
    private String institutionName;

    @ApiModelProperty(value = "机构类型")
    private String institutionType;

    @ApiModelProperty(value = "机构id")
    private String institutionId;

    @ApiModelProperty(value = "产品Id")
    private String productId;

    @ApiModelProperty(value = "产品编号")
    private Integer productCode;

    @ApiModelProperty(value = "交易类型")
    private Byte transType;

    @ApiModelProperty(value = "支付类型")
    private String payType;

    @ApiModelProperty(value = "分润比例")
    private BigDecimal dividedRatio;

    @ApiModelProperty(value = "分润模式 1-分成 2-费用差")
    private Byte dividedMode;

    @ApiModelProperty(value = "币种")
    private String currency;

    @ApiModelProperty(value = "浮动率")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal floatRate;

    @ApiModelProperty(value = "计费方式")
    private String rateType; // dic_7_1-单笔费率,dic_7_2-单笔定额

    @ApiModelProperty(value = "百分比费率最大值")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal maxRate;

    @ApiModelProperty(value = "百分比费率最小值")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal minRate;

    @ApiModelProperty(value = "费率")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal rate;

    @ApiModelProperty(value = "结算周期")
    private String settleCycle;


    @ApiModelProperty(value = "附加值")
    private BigDecimal addValue;

    @ApiModelProperty(value = "单笔限额")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal limitAmount;

    @ApiModelProperty(value = "启用禁用")
    private Boolean enabled;

    @ApiModelProperty(value = "审核信息状态")//审核限额状态 1-待审核 2-审核通过 3-审核不通过
    private Byte auditInfoStatus;

    @ApiModelProperty(value = "日交易总笔数限额")
    private Integer dailyTradingCount;

    @ApiModelProperty(value = "日交易总金额限额")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal dailyTotalAmount;

    @ApiModelProperty(value = "审核限额备注")
    private String auditLimitRemark;

    @ApiModelProperty(value = "审核信息备注")
    private String auditInfoRemark;

    @ApiModelProperty(value = "产品支付限额审核状态")//审核限额状态 1-待审核 2-审核通过 3-审核不通过
    private Byte auditLimitStatus;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createTime;

    @ApiModelProperty(value = "修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @ApiModelProperty(value = "创建者")
    private String creator;

    @ApiModelProperty(value = "更改者")
    private String modifier;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "手续费付款方，1：内扣 2：外扣")
    private Integer feePayer;

    @ApiModelProperty(value = "退款费率类型")
    private String refundRateType;

    @ApiModelProperty(value = "退款手续费最小值")
    private BigDecimal refundMinTate;

    @ApiModelProperty(value = "退款手续费最大值")
    private BigDecimal refundMaxTate;

    @ApiModelProperty(value = "退款费率")
    private BigDecimal refundRate;

    @ApiModelProperty(value = "退款附加值")
    private BigDecimal refundAddValue;

}
