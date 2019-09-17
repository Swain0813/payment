package com.payment.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @description:
 * @author: YangXu
 * @create: 2019-01-10 17:05
 **/
@Data
public class SysRoleVO {
    public String id;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createTime;

    @ApiModelProperty(value = "修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date updateTime;

    @ApiModelProperty(value = "创建者")
    public String creator;

    @ApiModelProperty(value = "更改者")
    public String modifier;
    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "角色名字")
    private String roleName;

    @ApiModelProperty(value = "角色编号")
    private String roleCode;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "状态")
    private boolean enabled = true;

    @ApiModelProperty(value = "是否选中")
    private boolean flag = false;

    @ApiModelProperty(value = "优先级")
    private Integer sort;

    @ApiModelProperty(value = "权限")
    List<SysMenuVO> menus;




}
