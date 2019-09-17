package com.payment.common.dto;

import com.payment.common.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "银行Issuerid对照表", description = "银行Issuerid对照表")
public class BankIssueridExportDTO extends BaseEntity {

    @ApiModelProperty(value = "银行名称")
    private String bankName;

    @ApiModelProperty(value = "issuerId")
    private String issuerId;

    @ApiModelProperty(value = "currency")
    private String currency;

    @ApiModelProperty(value = "通道编号")
    private String channelCode;

    @ApiModelProperty(value = "启用禁用")
    private Boolean enabled;

}
