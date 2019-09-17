package com.payment.common.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @description: 付款List请求
 * @author: YangXu
 * @create: 2019-07-23 10:30
 **/
@Data
@ApiModel(value = "付款List请求", description = "付款List请求")
public class PayOutDTO {

    @ApiModelProperty(value = "付款List请求")
    private List<PayOutRequestDTO> payOutRequestDTOs;

    @ApiModelProperty(value = "签名方式")//1为RSA 2为MD5
    private String signType;

    @ApiModelProperty(value = "签名")
    private String sign;

    @ApiModelProperty(value = "请求IP")
    private String reqIp;

    @ApiModelProperty(value = "交易密码")
    private String tradePwd;
}
