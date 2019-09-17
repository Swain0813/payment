package com.payment.trade.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "AD3线上收单接口输出实体", description = "AD3线上收单接口输出实体")
public class AD3OnlineVO {
    @ApiModelProperty(value = "签名")//1中文 2英文
    @JsonIgnore
    private String sign;

    @ApiModelProperty(value = "返回结果")//T000表示成功，其余见响应码列表
    private String respCode;

    @ApiModelProperty(value = "返回结果描述")//success,成功或者错误提示
    @JsonIgnore
    private String respMsg;

    @ApiModelProperty(value = "线上返回的付款url")//线上返回的付款url
    private String code_url;

    @ApiModelProperty(value = "类型")//WECHAT(微信) ALIPAY(支付宝) NETBANK(网银) ITS
    private String type;

}
