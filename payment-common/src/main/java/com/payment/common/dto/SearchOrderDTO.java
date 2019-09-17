package com.payment.common.dto;

import com.payment.common.base.BasePageHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @description: 查询订单实体
 * @author: YangXu
 * @create: 2019-03-04 15:33
 **/
@Data
@ApiModel(value = "订单输入实体", description = "订单输入实体")
public class SearchOrderDTO extends BasePageHelper {

    @ApiModelProperty(value = "退款订单号")
    private String id;

    @ApiModelProperty(value = "商户订单号")
    private String institutionOrderId;

    @ApiModelProperty(value = "交易方向")
    private Byte tradeDirection;

    @ApiModelProperty(value = "订单币种")
    private String orderCurrency;

    @ApiModelProperty(value = "交易订单号")
    private String orderId;

    @ApiModelProperty(value = "退款金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "机构号")
    private String institutionCode;

    @ApiModelProperty(value = "机构名称")
    private String institutionName;

    @ApiModelProperty(value = "退款方式 1：系统退款 2：人工退款")
    private Byte refundMode;

    @ApiModelProperty(value = "退款状态 1：退款中 2：退款成功 3：退款失败")
    private Byte refundStatus;

    @ApiModelProperty(value = "开始时间")
    private String startDate;

    @ApiModelProperty(value = "结束时间")
    private String endDate;


}
