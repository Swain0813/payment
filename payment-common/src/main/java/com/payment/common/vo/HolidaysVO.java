package com.payment.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@Data
@Entity
@Table(name = "holidays")
@ApiModel(value = "节假日输出实体", description = "节假日输出实体")
public class HolidaysVO {

    @ApiModelProperty(value = "节假日id")
    private String id;

    @ApiModelProperty(value = "国家")//从数据字典里获取
    private String country;

    @ApiModelProperty(value = "节假日名称")
    private String name;

    @ApiModelProperty(value = "创建者")
    private String creator;

    @ApiModelProperty(value = "修改者")
    private String modifier;

    @ApiModelProperty(value = "日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date date;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createTime;

    @ApiModelProperty(value = "修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date updateTime;

    @ApiModelProperty(value = "启用禁用")
    private boolean enabled;

    @ApiModelProperty(value = "备注")
    private String remark;
}
