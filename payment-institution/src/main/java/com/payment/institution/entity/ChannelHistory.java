package com.payment.institution.entity;

import com.payment.common.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;
@Data
@Entity
@Table(name = "channel_history")
@ApiModel(value = "通道历史配置", description = "通道历史配置")
public class ChannelHistory  extends BaseEntity {

    @ApiModelProperty(value = "通道Id")
    @Column(name = "channel_id")
    private String channelId;

    @ApiModelProperty(value = "通道编号")
    @Column(name = "channel_code")
    private String channelCode;

    @ApiModelProperty(value = "通道英文名称")
    @Column(name = "channel_en_name")
    private String channelEnName;

    @ApiModelProperty(value = "通道中文名称")
    @Column(name = "channel_cn_name")
    private String channelCnName;

    @ApiModelProperty(value = "通道图片")
    @Column(name = "channel_img")
    private String channelImg;

    @ApiModelProperty(value = "国家")
    @Column(name = "country")
    private String country;

    @ApiModelProperty(value = "国家类别")
    @Column(name = "country_type")
    private Byte countryType;

    @ApiModelProperty(value = "交易类型")
    @Column(name = "trans_type")
    private Byte transType;

    @ApiModelProperty(value = "支付方式")
    @Column(name = "pay_type")
    private String payType;

    @ApiModelProperty(value = "币种")
    @Column(name = "currency")
    private String currency;

    @ApiModelProperty(value = "银行机构号：微信填写wechat支付宝填写alipay")
    @Column(name = "issuerId")
    private String issuerId;

    @ApiModelProperty(value = "结算周期")
    @Column(name = "settle_cycle")
    private String settleCycle;

    @ApiModelProperty(value = "通道url")
    @Column(name = "channel_url")
    private String channelUrl;

    @ApiModelProperty(value = "通道限额")
    @Column(name = "limit_amount")
    private BigDecimal limitAmount;

    @ApiModelProperty(value = "启用禁用")
    @Column(name = "enabled")
    private Boolean enabled;

    @ApiModelProperty(value = "是否支持退款")
    @Column(name = "support_refund_state")
    private Boolean supportRefundState;

}
