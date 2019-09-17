package com.payment.common.vo;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.persistence.Column;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.Date;


/**
 * @description: 退款订单导出实体
 * @author: YangXu
 * @create: 2019-04-09 10:54
 **/
@Data
@ApiModel(value = "退款订单导出实体", description = "退款订单导出实体")
public class OrderRefundExportVO {

    // 创建时间
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createTime;

    @ApiModelProperty(value = "机构号")
    private String institutionCode;

    @ApiModelProperty(value = "机构名称")
    private String institutionName;

    @Id
    @ApiModelProperty(value = "退款订单号")
    public String id;

    @ApiModelProperty(value = "机构退款金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "机构退款币种")
    private String orderCurrency;

    @ApiModelProperty(value = "商户订单号")
    private String institutionOrderId;

    @ApiModelProperty(value = "通道名称")
    private String channelName;

    @ApiModelProperty(value = "通道退款金额")
    private BigDecimal tradeAmount;

    @ApiModelProperty(value = "通道退款币种")
    private String tradeCurrency;

    @ApiModelProperty(value = "退款完成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @ApiModelProperty(value = "交易方向")
    private Byte tradeDirection;

//    @ApiModelProperty(value = "创建者")
//    private String creator;
//
//    @ApiModelProperty(value = "更改者")
//    private String modifier;

    @ApiModelProperty(value = "退款状态")
    @Column(name = "refund_status")
    private Byte refundStatus;

    @ApiModelProperty(value = "退款类型")
    private Byte refundType;

    @ApiModelProperty(value = "退款方式")
    @Column(name = "refund_mode")
    private Byte refundMode;

    @ApiModelProperty(value = "备注")
    private String remark;

//    @ApiModelProperty(value = "商户所在地的时区记录")
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
//    private Date institutionOrderTime;

//    @ApiModelProperty(value = "商户的请求商品名称")
//    private String commodityName;


//    @ApiModelProperty(value = "设备编号")
//    private String deviceCode;
//
//    @ApiModelProperty(value = "设备操作员")
//    private String deviceOperator;
//
//    @ApiModelProperty(value = "原订单交易流水号")
//    private String orderId;
//
//    @ApiModelProperty(value = "原订单通道流水号")
//    private String channelNumber;
//
//    @ApiModelProperty(value = "退款单通道流水号")
//    private String refundChannelNumber;

}
