package com.payment.common.dto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotNull;

/**
 * 订单物流查询输入条件
 */
@Data
@ApiModel(value = "订单物流查询输入条件", description = "订单物流查询输入条件")
public class OrderLogisticsQueryDTO {

    @ApiModelProperty(value = "系统流水号")
    private String referenceNo;

    @ApiModelProperty(value = "订单物流id")
    private String id;

    @NotNull(message = "50002")
    @ApiModelProperty(value = "机构编号")
    private String institutionCode;

    @ApiModelProperty(value = "机构名称")
    private String institutionName;

    @NotNull(message = "50002")
    @ApiModelProperty(value = "机构订单号")
    private String institutionOrderId;

    @ApiModelProperty(value = "发货单号")
    private String invoiceNo;

    @ApiModelProperty(value = "发货状态")//1-未发货 2-已发货
    private Byte deliveryStatus;

    @ApiModelProperty(value = "签收状态")//1-未签收 2-已签收
    private Byte receivedStatus;

    @ApiModelProperty(value = "修改时间")
    private String updateTime;
}
