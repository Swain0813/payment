package com.payment.trade.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value = "机构关联信息输出实体", description = "机构关联信息输出实体")
public class InstitutionRelevantVO {

    @ApiModelProperty(value = "机构id")
    private String institutionId;

    @ApiModelProperty(value = "机构名称")
    private String institutionName;

    @ApiModelProperty(value = "产品关联信息")
    private List<ProductRelevantVO> productRelevantVOS;
}
