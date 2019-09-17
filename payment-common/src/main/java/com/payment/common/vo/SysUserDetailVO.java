package com.payment.common.vo;

import com.payment.common.response.ResPermissions;
import com.payment.common.response.ResRole;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @description:
 * @author: YangXu
 * @create: 2019-02-26 18:06
 **/
@Data
@ApiModel(value = "用户详细信息VO", description = "用户详细信息VO")
public class SysUserDetailVO {
    public String id;

    public String username;

    public String password;
    public String tradePassword;

    public String name;
    public String mobile;
    public String email;




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
    public String remark;
    public boolean enabled ;
    List<ResRole> role;
    Set<ResPermissions> permissions;

}
