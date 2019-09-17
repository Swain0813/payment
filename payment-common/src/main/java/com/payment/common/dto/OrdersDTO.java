package com.payment.common.dto;


import com.payment.common.base.BasePageHelper;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @description: 订单输入实体
 * @author: XuWenQi
 * @create: 2019-02-12 15:41
 **/
@Data
@ApiModel(value = "订单输入实体", description = "订单输入实体")
public class OrdersDTO extends BasePageHelper {

    @ApiModelProperty(value = "订单id")
    private String id;

    @ApiModelProperty(value = "交易类型")
    private Byte tradeType;

    @ApiModelProperty(value = "交易方向")
    private Byte tradeDirection;

    @ApiModelProperty(value = "商户名称")
    private String institutionName;

    @ApiModelProperty(value = "商户编号")
    private String institutionCode;

    @ApiModelProperty(value = "二级商户名称")
    private String secondInstitutionName;

    @ApiModelProperty(value = "二级商户编号")
    private String secondInstitutionCode;

    @ApiModelProperty(value = "语言")
    private String language;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "商户订单时间")
    private Date institutionOrderTime;

    @ApiModelProperty(value = "商户订单号")
    private String institutionOrderId;

    @ApiModelProperty(value = "订单金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "订单币种")
    private String orderCurrency;

    @ApiModelProperty(value = "交易币种")
    private String tradeCurrency;

    @ApiModelProperty(value = "商品名称")
    private String commodityName;

    @ApiModelProperty(value = "产品编码")
    private Integer productCode;

    @ApiModelProperty(value = "产品名称")
    private String productName;

    @ApiModelProperty(value = "通道流水号")
    private String channelNumber;

    @ApiModelProperty(value = "设备编号")
    private String deviceCode;

    @ApiModelProperty(value = "设备操作员")
    private String deviceOperator;

    @ApiModelProperty(value = "换汇汇率")
    private BigDecimal exchangeRate;

    @ApiModelProperty(value = "请求ip")
    private String reqIp;

    @ApiModelProperty(value = "付款方式")
    private String payMethod;

    @ApiModelProperty(value = "交易状态")//交易状态:1-待支付 2-交易中 3-交易成功 4-交易失败 5-已过期
    private Byte tradeStatus;

    @ApiModelProperty(value = "撤销状态")//撤销状态：1-撤销中 2-撤销成功 3-撤销失败
    private Byte cancelStatus;

    @ApiModelProperty(value = "退款状态")//退款状态：1-退款中 2-部分退款成功 3-退款成功 4-退款失败
    private Byte refundStatus;

    @ApiModelProperty(value = "交易开始时间")
    private String startDate;

    @ApiModelProperty(value = "交易结束时间")
    private String endDate;

    @ApiModelProperty(value = "商品描述")
    private String goodsDescription;

    @ApiModelProperty(value = "付款人名称")
    private String draweeName;

    @ApiModelProperty(value = "付款人账户")
    private String draweeAccount;

    @ApiModelProperty(value = "付款人银行")
    private String draweeBank;

    @ApiModelProperty(value = "付款人邮箱")
    private String draweeEmail;

    @ApiModelProperty(value = "付款人电话")
    private String draweePhone;

    @ApiModelProperty(value = "签名")
    private String sign;

    @ApiModelProperty(value = "备注1")
    private String remark1;

    @ApiModelProperty(value = "备注2")
    private String remark2;

    @ApiModelProperty(value = "备注3")
    private String remark3;

    @ApiModelProperty(value = "token")
    private String token;

    @ApiModelProperty(value = "银行机构号")
    private String issuerId;

    @ApiModelProperty(value = "支付方式")
    private String payType;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "产品id")
    private String productId;

}
