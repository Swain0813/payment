package com.payment.trade.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author shenxinran
 * @Date: 2019/4/2 16:55
 * @Description: 订单查询返回实体
 */
@Data
public class QueryOrdersVO {

    @ApiModelProperty(value = "版本号")
    private String versoin = "v1.0";

    @ApiModelProperty(value = "机构编号")
    private String institutionCode;

    @ApiModelProperty(value = "机构订单号")
    private String institutionOrderId;

    @ApiModelProperty(value = "订单币种")
    private String orderCurrency;


}
