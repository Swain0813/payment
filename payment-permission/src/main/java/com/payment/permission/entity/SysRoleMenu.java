package com.payment.permission.entity;

import com.payment.common.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "sys_role_menu")
@ApiModel(value = "角色权限中间表", description = "角色权限中间表")
public class SysRoleMenu extends BaseEntity {

    private String menuId;

    private String roleId;


}
