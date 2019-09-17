package com.payment.common.dto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 订单物流信息输入实体
 */
@Data
@ApiModel(value = "订单物流信息输入实体", description = "订单物流信息输入实体")
public class OrderLogisticsDTO {

    @ApiModelProperty(value = "订单物流id")
    private String id;

    @ApiModelProperty(value = "系统流水号")
    private String referenceNo;

    @ApiModelProperty(value = "机构编号")
    private String institutionCode;

    @ApiModelProperty(value = "机构名称")
    private String institutionName;

    @ApiModelProperty(value = "机构订单号")
    private String institutionOrderId;

    @ApiModelProperty(value = "商品名称")
    private String productDescription;

    @NotNull(message = "50002")
    @ApiModelProperty(value = "发货单号")
    private String invoiceNo;

    @NotNull(message = "50002")
    @ApiModelProperty(value = "服务商名称")
    private String providerName;

    @NotNull(message = "50002")
    @ApiModelProperty(value = "收货人地址")
    private String payerAddress;

    @ApiModelProperty(value = "收货人姓名")
    private String payerName;

    @ApiModelProperty(value = "收货人邮箱")
    private String payerEmail;

    @ApiModelProperty(value = "发货状态")//1-未发货 2-已发货
    private Byte deliveryStatus;

    @ApiModelProperty(value = "备注")
    private String remark;
}
