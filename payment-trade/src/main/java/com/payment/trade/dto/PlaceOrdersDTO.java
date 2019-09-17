package com.payment.trade.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @description: 线下下单输入实体
 * @author: XuWenQi
 * @create: 2019-03-29 10:41
 **/
@Data
@ApiModel(value = "下单输入实体", description = "下单输入实体")
public class PlaceOrdersDTO {

    @NotNull(message = "50002")
    @ApiModelProperty(value = "机构编号")
    private String institutionId;

    @NotNull(message = "50002")
    @ApiModelProperty(value = "订单币种")
    private String orderCurrency;

    @NotNull(message = "50002")
    @ApiModelProperty(value = "机构订单时间")
    private String orderTime;

    @NotNull(message = "50002")
    @ApiModelProperty(value = "机构订单号")
    private String orderNo;

    @NotNull(message = "50002")
    @ApiModelProperty(value = "订单金额")
    private BigDecimal orderAmount;

    @NotNull(message = "50002")
    @ApiModelProperty(value = "签名")
    private String sign;

    @ApiModelProperty(value = "产品编号")
    private Integer productCode;

    @ApiModelProperty(value = "支付方式")
    private String payMethod;

    @ApiModelProperty(value = "token")
    private String token;

    @ApiModelProperty(value = "银行机构号")
    private String issuerId;

    @ApiModelProperty(value = "付款码")
    private String authCode;

    @ApiModelProperty(value = "设备编号")
    private String terminalId;

    @ApiModelProperty(value = "设备操作员")
    private String operatorId;

    @ApiModelProperty(value = "回调地址")
    private String serverUrl;

    @ApiModelProperty(value = "浏览器返回地址")
    private String browserUrl;

    @ApiModelProperty(value = "交易币种")
    private String tradeCurrency;

    @ApiModelProperty(value = "签名方式")//1为RSA 2为MD5
    private String signType;

    /* 以下是非必填的下单参数 */

    @ApiModelProperty(value = "机构名称")
    private String institutionName;

    @ApiModelProperty(value = "二级机构名称")
    private String subInstitutionName;

    @ApiModelProperty(value = "二级机构编号")
    private String subInstitutionCode;

    @ApiModelProperty(value = "商品名称")
    private String productName;

    @ApiModelProperty(value = "商品描述")
    private String productDescription;

    @ApiModelProperty(value = "付款人姓名")
    private String payerName;

    @ApiModelProperty(value = "付款人账户")
    private String payerAccount;

    @ApiModelProperty(value = "付款人银行")
    private String payerBank;

    @ApiModelProperty(value = "付款人邮箱")
    private String payerEmail;

    @ApiModelProperty(value = "付款人电话")
    private String payerPhone;

    @ApiModelProperty(value = "备注1")
    private String remark1;

    @ApiModelProperty(value = "备注2")
    private String remark2;

    @ApiModelProperty(value = "备注3")
    private String remark3;

    @ApiModelProperty(value = "语言")
    private String language;

    public PlaceOrdersDTO() {
    }

    public PlaceOrdersDTO(CashierDTO cashierDTO) {
        this.institutionId = cashierDTO.getInstitutionCode();
        this.payMethod = cashierDTO.getPayType();
        this.orderCurrency = cashierDTO.getOrderCurrency();
        this.tradeCurrency = cashierDTO.getTradeCurrency();
    }
}
