package com.payment.trade.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author shenxinran
 * @Date: 2019/3/19 16:58
 * @Description: 线上订单状态查询实体
 */
@Data
@ApiModel(value = "线上订单状态查询实体", description = "线上订单状态查询实体")
public class OnlineOrderQueryRSADTO {

    @NotNull(message = "50002")
    @ApiModelProperty(value = "机构订单号")
    private String orderNo;

    @NotNull(message = "50002")
    @ApiModelProperty(value = "机构号")
    private String institutionId;

    @NotNull(message = "50002")
    @ApiModelProperty(value = "签名")
    private String sign;
}
