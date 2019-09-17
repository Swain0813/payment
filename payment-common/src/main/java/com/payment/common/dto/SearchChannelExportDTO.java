package com.payment.common.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @description:
 * @author: shenxinran
 * @create: 2019年8月28日10:42:56
 **/
@Data
@ApiModel(value = "产品通道管理导出查询实体", description = "产品通道管理导出查询实体")
public class SearchChannelExportDTO {

    @ApiModelProperty(value = "机构通道id")
    private String insChaId;

    @ApiModelProperty(value = "机构Code")
    private String InstitutionCode;

    @ApiModelProperty(value = "机构名称")
    private String InstitutionName;

    @ApiModelProperty(value = "产品id")
    private String productId;

    @ApiModelProperty(value = "支付类型")
    private Integer payType;

    @ApiModelProperty(value = "通道名称")
    private String channelName;

    @ApiModelProperty(value = "通道服务名称")
    private String channelEnName;

    @ApiModelProperty(value = "通道币种")
    private String currency;

    @ApiModelProperty(value = "语言")
    private String language;

    @ApiModelProperty(value = "启用禁用")
    private Boolean enabled;
}
