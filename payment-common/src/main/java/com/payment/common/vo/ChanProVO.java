package com.payment.common.vo;

import com.payment.common.entity.Bank;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import java.math.BigDecimal;
import java.util.List;

/**
 * @description:
 * @author: YangXu
 * @create: 2019-02-22 14:12
 **/
@Data
@ApiModel(value = "通道实体", description = "通道实体")
public class ChanProVO {

    @ApiModelProperty(value = "通道编号")
    private String channelCode;

    @ApiModelProperty(value = "ad3支付编号")
    private String payCode;

    @ApiModelProperty(value = "通道英文名称")
    private String channelEnName;

    @ApiModelProperty(value = "通道中文名称")
    private String channelCnName;

    @ApiModelProperty(value = "通道图片")
    private String channelImg;

    @ApiModelProperty(value = "通道最小限额")
    private BigDecimal limitMinAmount;

    @ApiModelProperty(value = "通道最大限额")
    private BigDecimal limitMaxAmount;

    @ApiModelProperty(value = "国家")
    private String country;

    @ApiModelProperty(value = "国家类别")
    private Byte countryType;

    @ApiModelProperty(value = "是否直连")
    private Byte channelConnectMethod;

    @ApiModelProperty(value = "交易类型")
    private Byte transType;

    @ApiModelProperty(value = "支付方式")
    private String payType;

    @ApiModelProperty(value = "币种")
    private String currency;

    @ApiModelProperty(value = "银行机构号：微信填写wechat支付宝填写alipay")
    private String issuerId;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "结算周期")
    private String settleCycle;

    @ApiModelProperty(value = "通道url")
    private String channelUrl;

    @ApiModelProperty(value = "退款url")
    private String refundUrl;

    @ApiModelProperty(value = "启用禁用")
    private Boolean enabled;

    @ApiModelProperty(value = "是否支持退款")
    private Boolean supportRefundState ;

    @ApiModelProperty(value = "通道关联产品实体")
    private List<ProVO> proVOS;

    @ApiModelProperty(value = "通道关联银行实体")
    private List<BankVO> bankVOS;

    @ApiModelProperty(value = "通道费率")
    private BigDecimal channelRate;

    @ApiModelProperty(value = "通道费率最小值")
    private BigDecimal channelMinRate;

    @ApiModelProperty(value = "通道费率最大值")
    private BigDecimal channelMaxRate;

    @ApiModelProperty(value = "通道手续费类型")
    private String channelFeeType;

    @ApiModelProperty(value = "通道网关费率")
    private BigDecimal channelGatewayRate;

    @ApiModelProperty(value = "通道网关费率最小值")
    private BigDecimal channelGatewayMinRate;

    @ApiModelProperty(value = "通道网关费率最大值")
    private BigDecimal channelGatewayMaxRate;

    @ApiModelProperty(value = "通道网关手续费")
    private String channelGatewayFee;//1-单笔费率,2-单笔定额

    @ApiModelProperty(value = "通道网关手续费类型")
    private String channelGatewayFeeType;//1-单笔费率,2-单笔定额

    @ApiModelProperty(value = "通道网关是否收取")
    private BigDecimal channelGatewayCharge;//1-收 2-不收

    @ApiModelProperty(value = "通道网关收取状态")
    private BigDecimal channelGatewayStatus;// 1-成功时收取 2-失败时收取 3-全收

    @ApiModelProperty(value = "通道商户号")
    private String channelMerchantId ;

    @ApiModelProperty(value = "通道加密MD5key")
    private String md5KeyStr ;

    @ApiModelProperty(value = "权重")
    private String sort;
}
