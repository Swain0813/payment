package com.payment.common.dto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @description: 付款请求DTO
 * @author: YangXu
 * @create: 2019-07-23 10:15
 **/
@Data
@ApiModel(value = "付款请求DTO", description = "付款请求DTO")
public class PayOutRequestDTO {

    @ApiModelProperty(value = "机构上报付款批次号")
    private String institutionBatchNo;

    @ApiModelProperty(value = "机构编号")
    private String institutionId;

    @ApiModelProperty(value = "机构上报时间")
    private String orderTime;

    @ApiModelProperty(value = "机构上报付款流水号")
    private String orderNo;

    @ApiModelProperty(value = "订单币种")
    private String orderCurrency;

    @ApiModelProperty(value = "订单金额")
    private BigDecimal orderAmount;

    @ApiModelProperty(value = "汇款币种")
    private String paymentCurrency;

    @ApiModelProperty(value = "汇款金额")
    private BigDecimal paymentAmount;

    @ApiModelProperty(value = "汇款银行名称")
    private String bankAccountName;

    @ApiModelProperty(value = "汇款银行卡号")
    private String bankAccountNumber;

    @ApiModelProperty(value = "持卡人")
    private String cardholder;

    @ApiModelProperty(value = "服务器回调地址")
    private String serverUrl;

    @ApiModelProperty(value = "浏览器返回地址")
    private String browserUrl;

    @ApiModelProperty(value = "银行机构代码")
    private String issuerId;

    @ApiModelProperty(value = "汇款国家")
    private String country;

    @ApiModelProperty(value = "汇款地址")
    private String adress;

    @ApiModelProperty(value = "汇款用途")
    private String description;
}
