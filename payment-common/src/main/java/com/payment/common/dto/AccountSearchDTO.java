package com.payment.common.dto;

import com.payment.common.base.BasePageHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @description:
 * @author: YangXu
 * @create: 2019-03-05 16:07
 **/
@Data
@ApiModel(value = "账户查询DTO", description = "账户查询DTO")
public class AccountSearchDTO extends BasePageHelper {

    @ApiModelProperty(value = "机构号")
    private String institutionCode;

    @ApiModelProperty(value = "机构名称")
    private String institutionName;

    @ApiModelProperty(value = "账户Id")
    private String accountId;

    @ApiModelProperty(value = "账户币种")
    private String currency;

}
