package com.payment.trade.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;


/**
 * 终端查询订单详情接口输入实体
 */
@Data
@ApiModel(value = "线下查询订单接口输入实体", description = "线下查询订单接口输入实体")
public class PosGetOrdersDTO {

    @NotNull(message = "50002")
    @ApiModelProperty(value = "商户编号")
    private String institutionId;

    @NotNull(message = "50002")
    @ApiModelProperty(value = "设备编号")
    private String terminalId;

    @NotNull(message = "50002")
    @ApiModelProperty(value = "操作员ID")
    private String operatorId;

    @NotNull(message = "50002")
    @ApiModelProperty(value = "签名")
    private String sign;

    @ApiModelProperty(value = "系统订单流水号")
    private String referenceNo;

    @ApiModelProperty(value = "交易开始时间")
    private String startDate;

    @ApiModelProperty(value = "交易结束时间")
    private String endDate;

    @ApiModelProperty(value = "交易状态")//1-待支付 2-交易中 3-交易成功 4-交易失败 5-已过期
    private Byte txnstatus;

    @ApiModelProperty(value = "退款状态")//1-退款中 2-部分退款成功 3-退款成功 4-退款失败
    private Byte refundStatus;

    @ApiModelProperty(value = "语言")
    private String language;

    @ApiModelProperty(value = "页码")
    public Integer pageNum;

    @ApiModelProperty(value = "每页条数")
    public Integer pageSize;
}
