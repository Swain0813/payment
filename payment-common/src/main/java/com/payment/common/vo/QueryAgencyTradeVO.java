package com.payment.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @description: 代理商交易查询VO
 * @author: XuWenQi
 * @create: 2019-08-23 11:33
 **/
@Data
@ApiModel(value = "代理商交易查询VO", description = "代理商交易查询VO")
public class QueryAgencyTradeVO {

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createTime;

    @ApiModelProperty(value = "系统流水号")
    private String id;

    @ApiModelProperty(value = "代理商编号")
    private String agencyCode;

    @ApiModelProperty(value = "机构编号")
    private String institutionCode;

    @ApiModelProperty(value = "机构名称")
    private String institutionName;

    @ApiModelProperty(value = "订单币种")
    private String orderCurrency;

    @ApiModelProperty(value = "订单金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "手续费")
    private BigDecimal fee;

    @ApiModelProperty(value = "交易状态")
    private Byte tradeStatus;

    @ApiModelProperty(value = "备注")
    private String remark;

}
