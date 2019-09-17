package com.payment.common.entity;
import com.payment.common.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单物流信息表的实体
 */
@Data
@Entity
@Table(name = "order_logistics")
@ApiModel(value = "订单物流信息表", description = "订单物流信息表")
public class OrderLogistics extends BaseEntity {

    @ApiModelProperty(value = "系统流水号")
    @Column(name = "reference_no")
    private String referenceNo;

    @ApiModelProperty(value = "机构编号")
    @Column(name = "institution_code")
    private String institutionCode;

    @ApiModelProperty(value = "机构名称")
    @Column(name = "institution_name")
    private String institutionName;

    @ApiModelProperty(value = "机构订单号")
    @Column(name = "institution_order_id")
    private String institutionOrderId;

    @ApiModelProperty(value = "订单币种")
    @Column(name = "order_currency")
    private String orderCurrency;

    @ApiModelProperty(value = "订单金额")
    @Column(name = "amount")
    private BigDecimal amount;

    @ApiModelProperty(value = "请求ip或者请求网站url")
    @Column(name = "req_ip")
    private String reqIp;

    @ApiModelProperty(value = "支付完成时间")
    @Column(name = "channel_callback_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date channelCallbackTime;

    @ApiModelProperty(value = "银行机构号")
    @Column(name = "issuer_id")
    private String issuerId;

    @ApiModelProperty(value = "语言")
    @Column(name = "language")
    private String language;

    @ApiModelProperty(value = "商品名称")
    @Column(name = "product_description")
    private String productDescription;

    @ApiModelProperty(value = "发货单号")
    @Column(name = "invoice_no")
    private String invoiceNo;

    @ApiModelProperty(value = "服务商名称")
    @Column(name = "provider_name")
    private String providerName;

    @ApiModelProperty(value = "运输商简码")
    @Column(name = "courier_code")
    private String courierCode;

    @ApiModelProperty(value = "收货人地址")
    @Column(name = "payer_address")
    private String payerAddress;

    @ApiModelProperty(value = "收货人姓名")
    @Column(name = "payer_name")
    private String payerName;

    @ApiModelProperty(value = "收货人邮箱")
    @Column(name = "payer_email")
    private String payerEmail;

    @ApiModelProperty(value = "发货状态")//1-未发货 2-已发货
    @Column(name = "delivery_status")
    private Byte deliveryStatus;

    @ApiModelProperty(value = "签收状态")//1-未签收 2-已签收
    @Column(name = "received_status")
    private Byte receivedStatus;
}
