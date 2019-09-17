package com.payment.common.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @description: 批量修改机构通道优先级DTO
 * @author: XuWenQi
 * @create: 2019-08-27 10:26
 **/
@Data
@ApiModel(value = "批量修改机构通道优先级DTO", description = "批量修改机构通道优先级DTO")
public class BatchUpdateSortDTO {

    @ApiModelProperty(value = "机构通道id")
    private String insChaId;

    @ApiModelProperty(value = "权重")
    private String sort;

    @ApiModelProperty(value = "启用禁用")
    private Boolean enabled;
}
