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
@Table(name = "sys_role")
@ApiModel(value = "角色表", description = "角色表")
public class SysRole extends BaseEntity {


    @ApiModelProperty(value = "机构ID")
    @Column(name = "institution_id")
    private String institutionId;

    @ApiModelProperty(value = "权限ID")
    @Column(name = "type")
    private Integer type;//1.运维后台 2商户后台 3.pos机

    @ApiModelProperty(value = "角色名字")
    @Column(name = "role_name")
    private String roleName;

    @ApiModelProperty(value = "角色编号")
    @Column(name = "role_code")
    private String roleCode;

    @ApiModelProperty(value = "描述")
    @Column(name = "description")
    private String description;

    @ApiModelProperty(value = "启用禁用")
    @Column(name = "enabled")
    private boolean enabled = true;

    @ApiModelProperty(value = "优先级")
    @Column(name = "sort")
    private Integer sort = 0;

}
