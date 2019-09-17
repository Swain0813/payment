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
public class ShareBenefitReportVO {

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createTime;

    @ApiModelProperty(value = "代理商编号")
    private String agencyCode;

    @ApiModelProperty(value = "代理商名称")
    private String agencyName;

    @ApiModelProperty(value = "系统流水号")
    private String orderId;

    @ApiModelProperty(value = "订单币种")
    private String orderCurrency;

    @ApiModelProperty(value = "订单金额")
    private Double orderAmount;

    @ApiModelProperty(value = "手续费")
    private Double fee;

    @ApiModelProperty(value = "分润比例")
    private BigDecimal dividedRatio;

    @ApiModelProperty(value = "分润金额")
    private Double shareBenefit;

    @ApiModelProperty(value = "分润状态")//1:待分润，2：已分润
    private Byte isShare;

    @ApiModelProperty(value = "机构名称")
    private String institutionName;

    @ApiModelProperty(value = "机构编号")
    private String institutionCode;

    @ApiModelProperty(value = "收单类型")//1 收单 2,付款
    private String extend1;

    @ApiModelProperty(value = "备注2")
    private String extend2;
}
