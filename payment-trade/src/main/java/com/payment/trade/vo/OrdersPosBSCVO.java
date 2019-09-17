package com.payment.trade.vo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 线下下单BSC响应实体
 *
 * @author: XuWenQi
 * @create: 2019-05-07 11:46
 **/

@Data
@ApiModel(value = "线下下单BSC响应实体", description = "线下下单BSC响应实体")
public class OrdersPosBSCVO {

    @ApiModelProperty(value = "机构编号")
    private String institutionId;

    @ApiModelProperty(value = "机构订单号")
    private String orderNo;

    @ApiModelProperty(value = "系统流水号")
    private String referenceNo;

    @ApiModelProperty(value = "订单币种")
    private String orderCurrency;

    @ApiModelProperty(value = "订单金额")
    private BigDecimal orderAmount;

    @ApiModelProperty(value = "机构订单时间")
    public String orderTime;

    @ApiModelProperty(value = "订单状态")//1-待支付 2-交易中 3-交易成功 4-交易失败 5-已过期
    public Byte txnstatus;

    @ApiModelProperty(value = "设备编号")
    private String terminalId;

    @ApiModelProperty(value = "操作员ID")
    private String operatorId;

}
