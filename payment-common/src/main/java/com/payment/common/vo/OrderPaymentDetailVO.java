package com.payment.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @description: 汇款单详细信息详情
 * @author: YangXu
 * @create: 2019-08-07 15:54
 **/
@Data
@ApiModel(value = "汇款单详细信息详情VO", description = "汇款单详细信息详情VO")
public class OrderPaymentDetailVO {


    @ApiModelProperty(value = "机构编号")
    private String institutionCode;

    @ApiModelProperty(value = "机构名称")
    private String institutionName;

    @ApiModelProperty(value = "机构上报时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date institutionOrderTime;

    @ApiModelProperty(value = "机构上报付款流水号")
    private String institutionOrderId;

    @ApiModelProperty(value = "机构上报付款批次号")
    private String institutionBatchNo;

    @ApiModelProperty(value = "通道付款批次号")
    private String channelBatchNo;

    @ApiModelProperty(value = "系统付款批次号")
    private String systemBatchNo;

    @ApiModelProperty(value = "订单币种")
    private String tradeCurrency;

    @ApiModelProperty(value = "订单金额")
    private BigDecimal tradeAmount;

    @ApiModelProperty(value = "汇款币种")
    private String paymentCurrency;

    @ApiModelProperty(value = "汇款金额")
    private BigDecimal paymentAmount;

    @ApiModelProperty(value = "汇款银行名称")
    private String bankAccountName;

    @ApiModelProperty(value = "汇款银行卡号")
    private String bankAccountNumber;

    @ApiModelProperty(value = "汇款国家")
    private String receiverCountry;

    @ApiModelProperty(value = "汇款地址")
    private String receiverAdress;

    @ApiModelProperty(value = "银行code")
    private String bankCode;

    @ApiModelProperty(value = "swiftCode")
    private String swiftCode;

    @ApiModelProperty(value = "旧汇率")
    private BigDecimal oldExchangeRate;

    @ApiModelProperty(value = "汇率")
    private BigDecimal exchangeRate;

    @ApiModelProperty(value = "换汇时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date exchangeTime;

    @ApiModelProperty(value = "换汇状态:1-换汇成功 2-换汇失败")
    private Byte exchangeStatus;

    @ApiModelProperty(value = "产品编号")
    private Integer productCode;

    @ApiModelProperty(value = "产品名称")
    private String productName;

    @ApiModelProperty(value = "通道编号")
    private String channelCode;

    @ApiModelProperty(value = "通道名称")
    private String channelName;

    @ApiModelProperty(value = "通道流水号")
    private String channelNumber;

    @ApiModelProperty(value = "汇款状态:1-汇款中 2-汇款成功 3-汇款失败")
    private Byte payoutStatus;

    @ApiModelProperty(value = "费率类型 (dic_7_1-单笔费率,dic_7_2-单笔定额)")
    private String rateType;

    @ApiModelProperty(value = "费率 根据订单币种")
    private BigDecimal rate;

    @ApiModelProperty(value = "手续费")
    private BigDecimal fee;

    @ApiModelProperty(value = "手续费付款方 1:内扣 2:外扣")
    private Byte feePayer;

    @ApiModelProperty(value = "计费状态：1-计费成功，2-计费失败")
    private Byte chargeStatus;

    @ApiModelProperty(value = "计费时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date chargeTime;

    @ApiModelProperty(value = "付款方式即产品表中的支付方式")
    private String payMethod;

    @ApiModelProperty(value = "请求ip")
    private String reqIp;

    @ApiModelProperty(value = "上报通道时间即付款请求时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date reportChannelTime;

    @ApiModelProperty(value = "通道回调时间即付款完成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date channelCallbackTime;

    @ApiModelProperty(value = "浮动率 必填")
    private BigDecimal floatRate;

    @ApiModelProperty(value = "附加值 必填")
    private BigDecimal addValue;

    @ApiModelProperty(value = "服务器回调地址")
    private String serverUrl;

    @ApiModelProperty(value = "浏览器返回地址")
    private String browserUrl;

    @ApiModelProperty(value = "md5key")
    private String md5key;

    @ApiModelProperty(value = "签名")
    private String sign;

    @ApiModelProperty(value = "应结算时间")
    private String extend1;

    @ApiModelProperty(value = "开户人名称")
    private String extend2;

    @ApiModelProperty(value = "通道服务名")
    private String extend3;

    @ApiModelProperty(value = "是否为人工汇款")
    private boolean extend4;

    @ApiModelProperty(value = "订单ID")
    public String id;

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
