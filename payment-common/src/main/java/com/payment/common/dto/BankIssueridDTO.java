package com.payment.common.dto;

import com.payment.common.base.BasePageHelper;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;

/**
 * @description:
 * @author: YangXu
 * @create: 2019-07-04 16:34
 **/
@Data
public class BankIssueridDTO extends BasePageHelper {

    @ApiModelProperty(value = "银行名称 （通道名）")
    private String bankName;

    @ApiModelProperty(value = "currency")
    private String currency;

    @ApiModelProperty(value = "通道编号")
    private String channelCode;
    private String issuerId;
    private Boolean enabled;
}
