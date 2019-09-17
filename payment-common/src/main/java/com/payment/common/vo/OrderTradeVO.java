package com.payment.common.vo;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 交易一览查询输出实体
 */
@Data
@ApiModel(value = "交易一览查询输出实体", description = "交易一览查询输出实体")
public class OrderTradeVO {

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createTime;

    @ApiModelProperty(value = "支付完成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date channelCallbackTime;

    @ApiModelProperty(value = "订单id")
    private String orderId;

    @ApiModelProperty(value = "机构订单号")
    private String institutionOrderId;

    @ApiModelProperty(value = "机构编号")
    private String institutionCode;

    @ApiModelProperty(value = "机构名称")
    private String institutionName;

    @ApiModelProperty(value = "通道名称")
    private String channelName;

    @ApiModelProperty(value = "产品名称")
    private String productName;

    @ApiModelProperty(value = "订单币种")
    private String orderCurrency;

    @ApiModelProperty(value = "订单金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "手续费")
    private BigDecimal fee;

    @ApiModelProperty(value = "换汇汇率")
    private BigDecimal exchangeRate;

    @ApiModelProperty(value = "交易币种")
    private String tradeCurrency;

    @ApiModelProperty(value = "交易金额")
    private BigDecimal tradeAmount;

    @ApiModelProperty(value = "交易状态")//交易状态:1-待支付 2-交易中 3-交易成功 4-交易失败 5-已过期
    private Byte tradeStatus;

    @ApiModelProperty(value = "撤销状态")//撤销状态：1-撤销中 2-撤销成功 3-撤销失败
    private Byte cancelStatus;

    @ApiModelProperty(value = "退款状态")//退款状态：1-退款中 2-部分退款成功 3-退款成功 4-退款失败
    private Byte refundStatus;

    @ApiModelProperty(value = "通道流水号")
    private String channelNumber;

    @ApiModelProperty(value = "交易方向")
    private Byte tradeDirection;

    @ApiModelProperty(value = "交易类型")
    private String tradeType;

    @ApiModelProperty(value = "请求ip或者网站url")
    private String reqIp;

    @ApiModelProperty(value = "设备编号")
    private String deviceCode;

    @ApiModelProperty(value = "支付方式")
    private String payMethod;

    @ApiModelProperty(value = "商品名称")
    private String goodsName;

    @ApiModelProperty(value = "付款人名称")
    private String draweeName;

    @ApiModelProperty(value = "付款人邮箱")
    private String draweeEmail;

    @ApiModelProperty(value = "备注")
    private String remark;
}
