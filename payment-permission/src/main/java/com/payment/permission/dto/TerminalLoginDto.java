package com.payment.permission.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 终端登录输入参数实体
 */
@Data
@ApiModel(value = "终端登录输入参数实体", description = "终端登录输入参数实体")
public class TerminalLoginDto {

    @ApiModelProperty(value = "机构编号")
    private String institutionId;

    @ApiModelProperty(value = "设备编号")
    private String terminalId;

    @ApiModelProperty(value = "操作员ID")
    private String operatorId;

    @ApiModelProperty(value = "登录密码")
    private String password;

    @ApiModelProperty(value = "语言")
    private String language;
}
