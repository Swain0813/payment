package com.payment.trade.dto;

import cn.hutool.core.date.DateUtil;
import com.payment.common.constant.AD3Constant;
import com.payment.common.entity.Orders;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author shenxinran
 * @Date: 2019/3/12 11:38
 * @Description: AD3线上收单接口参数实体
 */
@Data
@ApiModel(value = "AD3线上收单接口参数实体", description = "AD3线上收单接口参数实体")
public class AD3OnlineAcquireDTO {

    @ApiModelProperty(value = "版本")
    private String version = "v1.0";

    @ApiModelProperty(value = "字符集")
    private String inputCharset;

    @ApiModelProperty(value = "语言")
    private String language;

    @ApiModelProperty(value = "开放给商户的唯一编号")
    private String merchantId;

    @ApiModelProperty(value = "商户上送的商户订单号")
    private String merOrderNo;

    @ApiModelProperty(value = "订单时间, 日期格式：yyyyMMddHHmmss")
    private String merorderDatetime;

    @ApiModelProperty(value = "订单币种")
    private String merorderCurrency;

    @ApiModelProperty(value = "订单金额")
    private String merorderAmount;

    @ApiModelProperty(value = "浏览器返回地址")
    private String pickupUrl;

    @ApiModelProperty(value = "交易结果后台通知地址")
    private String receiveUrl;

    @ApiModelProperty(value = "支付方式")
    private String payType;

    @ApiModelProperty(value = "银行机构代码")
    private String issuerId;

    @ApiModelProperty(value = "备注信息1")
    private String ext1;

    @ApiModelProperty(value = "备注信息2")
    private String ext2;

    @ApiModelProperty(value = "备注信息3")
    private String ext3;

    @ApiModelProperty(value = "业务类型")
    private String businessType;

    @ApiModelProperty(value = "付款人姓名")
    private String payerName;

    @ApiModelProperty(value = "付款人邮箱")
    private String payerEmail;

    @ApiModelProperty(value = "付款人电话")
    private String payerTelephone;

    @ApiModelProperty(value = "商品ID")
    private String productId;

    @ApiModelProperty(value = "商品名称")
    private String productName;

    @ApiModelProperty(value = "商品数量")
    private String productNum;

    @ApiModelProperty(value = "商品单价")
    private String productPrice;

    @ApiModelProperty(value = "商品标价币种")
    private String productCurrency;

    @ApiModelProperty(value = "商户加密证书方式")//1为使用平台提供的密钥 2为使用自己生成的密钥
    private String merchantSignType;

    @ApiModelProperty(value = "商户签名")
    private String signMsg;

    public AD3OnlineAcquireDTO() {

    }

    public AD3OnlineAcquireDTO(Orders orders, String merchantOnlineCode, String payType, String issuerId) {
        this.inputCharset = AD3Constant.CHARSET_UTF_8;
        this.language = AD3Constant.LANGUAGE_CN;//语言
        this.merchantId = merchantOnlineCode;
        this.merOrderNo = orders.getId();
        this.merorderDatetime = DateUtil.format(new Date(), "yyyyMMddHHmmss");
        this.merorderCurrency = orders.getTradeCurrency();
        this.merorderAmount = String.valueOf(orders.getTradeAmount());
        this.payType = payType;
        this.issuerId = issuerId;
        this.ext1 = orders.getRemark1();
        this.ext2 = orders.getRemark2();
        this.ext3 = orders.getRemark3();
        this.businessType = AD3Constant.BUSINESS_OUT;
        this.payerName = orders.getDraweeName();
        this.payerEmail = orders.getDraweeEmail();
        this.payerTelephone = orders.getDraweePhone();
        this.productId = "";
        this.productName = orders.getCommodityName();
        this.productNum = "";
        this.productPrice = "";
        this.productCurrency = "";
        this.merchantSignType = "2";//1为使用平台提供的密钥 2为使用自己生成的密钥
    }
}
