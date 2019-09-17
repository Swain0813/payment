package com.payment.permission.dto;

import com.payment.common.base.BasePageHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * @description:
 * @author: YangXu
 * @create: 2019-02-22 15:22
 **/
@Data
@ApiModel(value = "角色查询实体", description = "角色查询实体")
public class SysRoleSecDto  extends BasePageHelper {

    @ApiModelProperty(value = "角色名字")
    private String roleName;

    @ApiModelProperty(value = "机构id")
    private String institutionId;

    @ApiModelProperty(value = "权限类型")// 1,运维 2 商户后台 3 pos机 4 代理商
    private Integer type;

    @ApiModelProperty(value = "描述")
    private String description;

}
