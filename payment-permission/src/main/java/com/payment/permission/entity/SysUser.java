package com.payment.permission.entity;

import com.payment.common.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "sys_user")
@ApiModel(value = "用户", description = "用户")
public class SysUser extends BaseEntity {

    @ApiModelProperty(value = "用户账户")
    @Column(name = "username")
    private String username;

    @ApiModelProperty(value = "密码")
    @Column(name = "password")
    private String password;

    @ApiModelProperty(value = "交易密码")
    @Column(name = "trade_password")
    private String tradePassword;

    @ApiModelProperty(value = "用户名称")
    @Column(name = "name")
    private String name;

    @ApiModelProperty(value = "用户类型")
    @Column(name = "type")
    private Integer type; //1:运维 2：商户后台 3：pos机操作人员

    @ApiModelProperty(value = "手机")
    @Column(name = "mobile")
    private String mobile;

    @ApiModelProperty(value = "邮箱")
    @Column(name = "email")
    private String email;

    @ApiModelProperty(value = "语言")
    @Column(name = "language")
    private String language;

    @ApiModelProperty(value = "机构id")
    @Column(name = "institution_id")
    private String institutionId;

    @ApiModelProperty(value = "启用禁用")
    @Column(name = "enabled")
    private Boolean enabled = true;
}
