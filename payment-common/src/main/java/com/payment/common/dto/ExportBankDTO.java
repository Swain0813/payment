package com.payment.common.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @description:
 * @author: 银行导出DTO
 * @create: 2019-07-04 16:39
 **/
@Data
@ApiModel(value = "银行导出DTO", description = "银行导出DTO")
public class ExportBankDTO {

    @ApiModelProperty(value = "银行名称")
    private String bankName;

    @ApiModelProperty(value = "国家")
    private String bankCountry;

    @ApiModelProperty(value = "银行id")
    private String id;

    @ApiModelProperty(value = "银行机构号")
    private String issuerId;

    @ApiModelProperty(value = "银行币种")
    private String bankCurrency;

    @ApiModelProperty(value = "币种")
    private String currency;

    @ApiModelProperty(value = "启用禁用")
    private Boolean enabled;

    @ApiModelProperty(value = "银行编号")
    private String bankCode;
}
