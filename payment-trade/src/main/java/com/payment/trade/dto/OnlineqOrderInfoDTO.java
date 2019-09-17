package com.payment.trade.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author shenxinran
 * @Date: 2019/5/13 16:23
 * @Description:
 */
@Data
@ApiModel(value = "线上订单查询实体", description = "线上订单查询实体")
public class OnlineqOrderInfoDTO {

    @NotNull(message = "50002")
    @ApiModelProperty(value = "商户编号")
    private String institutionId;

    @NotNull(message = "50002")
    @ApiModelProperty(value = "签名")
    private String sign;

    @ApiModelProperty(value = "机构订单号")
    private String orderNo; //商户订单号-由商户上送

    @ApiModelProperty(value = "订单币种")
    private String orderCurrency;

    @ApiModelProperty(value = "交易状态")//1-待支付 2-交易中 3-交易成功 4-交易失败 5-已过期
    private Byte txnstatus;

    @ApiModelProperty(value = "交易开始时间")
    private String startDate;

    @ApiModelProperty(value = "交易结束时间")
    private String endDate;

    @ApiModelProperty(value = "语言")
    private String language;

    @ApiModelProperty(value = "页码")
    private Integer pageNum;

    @ApiModelProperty(value = "签名方式")
    private String signType;

    @ApiModelProperty(value = "每页条数")
    private Integer pageSize;
}
