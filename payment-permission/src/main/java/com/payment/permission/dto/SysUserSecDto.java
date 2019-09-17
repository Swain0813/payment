package com.payment.permission.dto;

import com.payment.common.base.BasePageHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * @description: 用户查询实体
 * @author: YangXu
 * @create: 2019-01-25 17:23
 **/
@Data
@ApiModel(value = "用户查询实体", description = "用户查询实体")
public class SysUserSecDto extends BasePageHelper {

    @ApiModelProperty(value = "机构编号")
    private String institutionCode;

    @ApiModelProperty(value = "手机")
    private String mobile;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "用户账户")
    private String username;

    @ApiModelProperty(value = "用户名称")
    private String name;

    @ApiModelProperty(value = "用户类型")//1:运维 2:商户 3:pos 4 代理商
    private Integer type;

    @ApiModelProperty(value = "角色id")
    private String roleId;

    @ApiModelProperty(value = "启用禁用")
    private Boolean enabled;


}
