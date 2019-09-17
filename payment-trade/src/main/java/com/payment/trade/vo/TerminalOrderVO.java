package com.payment.trade.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "终端查询订单状态输出实体", description = "终端查询订单状态输出实体")
public class TerminalOrderVO {

    @ApiModelProperty(value = "机构订单号")
    private String orderNo;

    @ApiModelProperty(value = "交易状态")//订单交易状态(1-待支付 2-交易中 3-交易成功 4-交易失败 5-已过期)
    private Byte txnstatus;
}
