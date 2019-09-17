package com.payment.trade.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author XuWenQi
 * @Date: 2019/3/7 15:53
 * @Description: 指定结算接口参数实体
 */
@Data
@ApiModel(value = "指定结算接口参数实体", description = "指定结算接口参数实体")
public class AssignSettleDTO {

    @ApiModelProperty(value = "版本号")//固定v1.0
    private String version = "v1.0";

    @ApiModelProperty(value = "字符集")//1.utf-8 2.gbk
    private String inputCharset;

    @ApiModelProperty(value = "语言")//1中文 2英文
    private String language;

    @ApiModelProperty(value = "商户号")
    private String merchantid;

    @ApiModelProperty(value = "业务订单编号")
    private String refcnceFlow;

    @ApiModelProperty(value = "商户订单号")
    private String merOrderNo;

    @ApiModelProperty(value = "应该清结算时间")
    private String shouldDealtime;

    @ApiModelProperty(value = "结算状态")//1、指定时间结算，2、立即结算
    private String state;

    @ApiModelProperty(value = "签名文字符串")
    private String signMsg;
}
