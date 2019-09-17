package com.payment.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @description: 代理商分润查询VO
 * @author: XuWenQi
 * @create: 2019-08-23 16:44
 **/
@Data
@ApiModel(value = "代理商分润查询VO", description = "代理商分润查询VO")
public class QueryAgencyShareBenefitVO {

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createTime;

    @ApiModelProperty(value = "系统流水号")
    private String orderId;

    @ApiModelProperty(value = "分润金额")
    private Double shareBenefit;

    @ApiModelProperty(value = "分润状态")//1:待分润，2：已分润
    private Byte isShare;

    @ApiModelProperty(value = "机构编号")
    private String institutionCode;

    @ApiModelProperty(value = "机构名称")
    private String institutionName;

    @ApiModelProperty(value = "订单币种")
    private String tradeCurrency;

    @ApiModelProperty(value = "订单金额")
    private BigDecimal tradeAmount;

    @ApiModelProperty(value = "手续费")
    private Double fee;

    @ApiModelProperty(value = "备注")
    private String remark;

}
