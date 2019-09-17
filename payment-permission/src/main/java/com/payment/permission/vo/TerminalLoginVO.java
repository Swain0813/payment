package com.payment.permission.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 终端登录输出参数实体
 */
@Data
@ApiModel(value = "终端登录输出参数实体", description = "终端登录输出参数实体")
public class TerminalLoginVO {

    @ApiModelProperty(value = "密钥令牌")
    private String token;
}
