package com.payment.permission.dto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;

/**
 * @description:
 * @author: YangXu
 * @create: 2019-01-23 15:00
 **/
@Data
@ApiModel(value = "用户角色关联实体", description = "用户角色关联实体")
public class SysUserRoleDto {
    @ApiModelProperty(value = "用户Id")
    String userId;

    @ApiModelProperty(value = "机构Id")
    String institutionId;

    @ApiModelProperty(value = "用户账户")
    private String username;

    @ApiModelProperty(value = "登录密码")
    private String password;

    @ApiModelProperty(value = "交易密码")
    private String tradePassword;

    @ApiModelProperty(value = "用户名称")
    private String name;

    @ApiModelProperty(value = "用户类型")//1:运维 2:商户 3:pos 4 代理商
    private Integer type;

    @ApiModelProperty(value = "手机")
    private String mobile;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "启用禁用")
    private Boolean enabled;

    @ApiModelProperty(value = "角色Id")
    List<String> roleId;

    @ApiModelProperty(value = "权限Id")
    List<String> menuId;

    @ApiModelProperty(value = "新增修改标记 1-新增 2-修改")
    private String flag;
}
