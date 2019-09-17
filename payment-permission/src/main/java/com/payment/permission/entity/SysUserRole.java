package com.payment.permission.entity;

import com.payment.common.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "sys_user_role")
@ApiModel(value = "用户角色中间表", description = "用户角色中间表")
public class SysUserRole extends BaseEntity {


    private String userId;

    private String roleId;


}
