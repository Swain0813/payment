package com.payment.trade.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 订单物流信息输出实体
 */
@Data
@ApiModel(value = "订单物流信息输出实体", description = "订单物流信息输出实体")
public class OrderLogisticsVO {

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

    @ApiModelProperty(value = "发货单号")
    private String invoiceNo;

    @ApiModelProperty(value = "服务商名称")
    private String providerName;

    @ApiModelProperty(value = "收货人地址")
    private String payerAddress;

    @ApiModelProperty(value = "收货人姓名")
    private String payerName;

    @ApiModelProperty(value = "收货人邮箱")
    private String payerEmail;

    @ApiModelProperty(value = "发货状态")//1-未发货 2-已发货
    private Byte deliveryStatus;

    @ApiModelProperty(value = "签收状态")//1-未签收 2-已签收
    private Byte receivedStatus;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createTime;

    @ApiModelProperty(value = "修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @ApiModelProperty(value = "创建者")
    private String creator;

    @ApiModelProperty(value = "更改者")
    private String modifier;

    @ApiModelProperty(value = "备注")
    private String remark;
}
