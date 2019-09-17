package com.payment.permission.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: YangXu
 * @create: 2019-01-23 15:01
 **/
@Data
@ApiModel(value = "用户权限关联实体", description = "用户权限关联实体")
public class SysUserMenuDto {

    @ApiModelProperty(value = "用户Id")
    String userId;

    @ApiModelProperty(value = "权限Id")
    List<String> menuId;
}
