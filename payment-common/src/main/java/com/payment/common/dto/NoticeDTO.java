package com.payment.common.dto;
import com.payment.common.base.BasePageHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 公告的输入参数
 */
@Data
@ApiModel(value = "公告输入参数的实体", description = "公告输入参数的实体")
public class NoticeDTO extends BasePageHelper {

    @ApiModelProperty(value = "公告id")
    private String id;

    @ApiModelProperty(value = "公告类别")
    private String category;

    @ApiModelProperty(value = "标题")
    private String title;

    @ApiModelProperty(value = "公告内容")
    private String context;

    @ApiModelProperty(value = "语言")
    private String language;

    @ApiModelProperty(value = "公告排序")
    private int rank;

    @ApiModelProperty(value = "公告生效时间")
    private String startDate;

    @ApiModelProperty(value = "公告结束时间")
    private String endDate;

    @ApiModelProperty(value = "启用禁用")
    private Boolean enabled;

    @ApiModelProperty(value = "备注")
    private String remark;
}
