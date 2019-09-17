package com.payment.trade.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 机构批量查询未发货的订单物流信息输入实体
 */
@Data
@ApiModel(value = "机构批量查询未发货的订单物流信息输入实体", description = "机构批量查询未发货的订单物流信息输入实体")
public class OrderLogisticsBatchQueryDTO {

    @NotNull(message = "50002")
    @ApiModelProperty(value = "机构编号")
    private String institutionId;

    @NotNull(message = "50002")
    @ApiModelProperty(value = "签名")
    private String sign;

    @NotNull(message = "50002")
    @ApiModelProperty(value = "签名方式")//1为RSA 2为MD5
    private String signType;
}
