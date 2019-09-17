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
@ApiModel(value = "角色权限关联实体", description = "角色权限关联实体")
public class SysRoleMenuDto {

    @ApiModelProperty(value = "角色Id")
    String roleId;

    @ApiModelProperty(value = "角色名字")
    private String roleName;

    @ApiModelProperty(value = "机构ID")
    private String institutionId;

    @ApiModelProperty(value = "权限类型")
    private int type;

    @ApiModelProperty(value = "角色编号")
    private String roleCode;

    @ApiModelProperty(value = "角色描述")
    private String description;

    @ApiModelProperty(value = "权限Id")
    List<String> menuId;
}
