package com.payment.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 公告查询结果输出参数
 */
@Data
public class NoticeVO {
    @ApiModelProperty(value = "公告id")
    private String id;

    @ApiModelProperty(value = "公告类别")
    private String category;

    @ApiModelProperty(value = "公告标题")
    private String title;

    @ApiModelProperty(value = "语言")
    private String language;

    @ApiModelProperty(value = "公告排序")
    private int rank;

    @ApiModelProperty(value = "公告生效开始时间")
    private Date startDate;

    @ApiModelProperty(value = "公告生效结束时间")
    private Date endDate;

    @ApiModelProperty(value = "启用禁用")
    private Boolean enabled;

    @ApiModelProperty(value = "公告内容")
    private String context;

    @ApiModelProperty(value = "公告类别名称")
    private String name;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createTime;

    @ApiModelProperty(value = "修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @ApiModelProperty(value = "创建者")
    private String creator;

    @ApiModelProperty(value = "更改者")
    private String modifier;

    @ApiModelProperty(value = "备注")
    private String remark;
}
