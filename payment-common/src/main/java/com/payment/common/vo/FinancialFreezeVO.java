package com.payment.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author shenxinran
 * @Date: 2019/6/19 10:27
 * @Description: 清结算资金冻结解冻输出实体
 */
@Data
@ApiModel(value = "清结算资金冻结解冻输出实体", description = "清结算资金冻结解冻输出实体")
public class FinancialFreezeVO {

    @ApiModelProperty(value = "响应码")
    private String respCode;

    @ApiModelProperty(value = "返回结果描述")
    private String respMsg;

    @ApiModelProperty(value = "字符集")
    private String inputCharset;

    @ApiModelProperty(value = "版本号")
    private String version;

    @ApiModelProperty(value = "语言")
    private String language;

    @ApiModelProperty(value = "商户号")
    private String merchantId;

    @ApiModelProperty(value = "虚拟账户编号")
    private String mvaccountId;

    @ApiModelProperty(value = "备注信息")
    private String desc;

    @ApiModelProperty(value = "商户订单号")
    private String merOrderNo;

    @ApiModelProperty(value = "交易币种")
    private String txncurrency;

    @ApiModelProperty(value = "交易金额")
    private String txnamount;

    @ApiModelProperty(value = "状态")
    private String state;

    @ApiModelProperty(value = "系统编号")
    private String id;

    @ApiModelProperty(value = "签名文字符串")
    private String signMsg;
}
