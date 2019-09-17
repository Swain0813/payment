package com.payment.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @description: 分润报表VO
 * @author: XuWenQi
 * @create: 2019-08-26 11:51
 **/
@Data
@ApiModel(value = "分润报表VO", description = "分润报表VO")
public class ShareBenefitReportEnVO {

    @ApiModelProperty(value = "Creation Time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createTime;

    @ApiModelProperty(value = "Agency Code")
    private String agencyCode;

    @ApiModelProperty(value = "Agency Name")
    private String agencyName;

    @ApiModelProperty(value = "Order Id")
    private String orderId;

    @ApiModelProperty(value = "Order Currency")
    private String orderCurrency;

    @ApiModelProperty(value = "Order Amount")
    private Double orderAmount;

    @ApiModelProperty(value = "Fee")
    private Double fee;

    @ApiModelProperty(value = "Divided Ratio")
    private BigDecimal dividedRatio;

    @ApiModelProperty(value = "Share Benefit")
    private Double shareBenefit;

    @ApiModelProperty(value = "Share Benefit Status")//1:待分润，2：已分润
    private Byte isShare;

    @ApiModelProperty(value = "Institution Name")
    private String institutionName;

    @ApiModelProperty(value = "Institution Code")
    private String institutionCode;

    @ApiModelProperty(value = "Receipt Type")//1 收单 2,付款
    private String extend1;

    @ApiModelProperty(value = "remark2")
    private String extend2;
}
