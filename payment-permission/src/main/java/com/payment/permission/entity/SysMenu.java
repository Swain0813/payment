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
@Table(name = "sys_menu")
@ApiModel(value = "权限", description = "权限")
public class SysMenu extends BaseEntity {

    @ApiModelProperty(value = "上级Id")
    @Column(name = "parent_id")
    private String parentId;

    @ApiModelProperty(value = "英文名称")
    @Column(name = "en_name")
    private String enName;

    @ApiModelProperty(value = "中文名称")
    @Column(name = "cn_name")
    private String cnName;

    @ApiModelProperty(value = "路径")
    @Column(name = "url")
    private String url;

    @ApiModelProperty(value = "描述")
    @Column(name = "description")
    private String description;

    @ApiModelProperty(value = "层级")
    @Column(name = "level")
    private Integer level;

    @ApiModelProperty(value = "启用禁用")
    @Column(name = "enabled")
    private Boolean enabled = true;

    @ApiModelProperty(value = "排序")
    @Column(name = "sort")
    private Integer sort;

    @ApiModelProperty(value = "类型")
    @Column(name = "type")
    private Integer type; //1-运维 2-机构 3-pos

}
