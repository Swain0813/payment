package com.payment.common.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@ApiModel(value = "机构产品输入实体", description = "机构产品输入实体")
public class InstProdDTO {

    @NotNull(message = "50002")
    @ApiModelProperty(value = "机构id")
    private String institutionId;

    @NotNull(message = "50002")
    @ApiModelProperty(value = "产品")
    List<ProdChannelDTO> productList;
}
