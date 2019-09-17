package com.payment.permission.entity;

import com.payment.common.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "sys_user_menu")
@ApiModel(value = "用户权限中间表", description = "用户权限中间表")
public class SysUserMenu extends BaseEntity {

    private String userId;

    private String menuId;


}
