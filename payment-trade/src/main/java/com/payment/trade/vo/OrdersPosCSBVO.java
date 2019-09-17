package com.payment.trade.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 线下下单CSB响应实体
 *
 * @author: XuWenQi
 * @create: 2019-05-07 11:46
 **/

@Data
@ApiModel(value = "线下下单CSB响应实体", description = "线下下单CSB响应实体")
public class OrdersPosCSBVO {

    @ApiModelProperty(value = "机构订单号")
    private String institutionOrderId;

    @ApiModelProperty(value = "收款二维码url")
    private String url;

    @ApiModelProperty(value = "解码标记")//0不解码,1解码
    private String decodeFlag = "0";
}
