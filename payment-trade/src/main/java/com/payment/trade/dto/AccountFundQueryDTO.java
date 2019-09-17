package com.payment.trade.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author XuWenQi
 * @Date: 2019/3/7 15:35
 * @Description: 账户资金查询接口参数实体
 */
@Data
@ApiModel(value = "账户资金查询接口参数实体", description = "账户资金查询接口参数实体")
public class AccountFundQueryDTO {

    @ApiModelProperty(value = "版本号")//固定v1.0
    private String version = "v1.0";

    @ApiModelProperty(value = "字符集")//1.utf-8 2.gbk
    private String inputCharset;

    @ApiModelProperty(value = "语言")//1中文 2英文
    private String language;

    @ApiModelProperty(value = "商户号")
    private String merchantId;

    @ApiModelProperty(value = "订单币种")//3位国际 ISO编码
    private String currency;

    @ApiModelProperty(value = "虚拟账户编号")
    private String vaccountNo;

    @ApiModelProperty(value = "签名字符串")
    private String signMsg;
}
