package com.payment.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;


/**
 * @description: 产品通道管理输出实体
 * @author: YangXu
 * @create: 2019-02-13 11:17
 **/
@Data
@ApiModel(value = "产品通道管理实体", description = "产品通道管理实体")
public class ProductChannelVO {

    @ApiModelProperty(value = "机构通道Id")
    private String insChaId;

    @ApiModelProperty(value = "通道银行id")
    private String chaBankId;

    @ApiModelProperty(value = "银行id")
    private String bankId;

    @ApiModelProperty(value = "银行名称")
    private String bankName;

    @ApiModelProperty(value = "银行优先级")
    private String sort;

    @ApiModelProperty(value = "产品Id")
    private String productId;

    @ApiModelProperty(value = "通道Id")
    private String channelId;

    @ApiModelProperty(value = "机构号")
    private String institutionCode;

    @ApiModelProperty(value = "机构名称")
    private String institutionName;

    @ApiModelProperty(value = "币种")
    private String currency;

    @ApiModelProperty(value = "银行机构号：微信填写wechat支付宝填写alipay")
    private String issuerId;

    @ApiModelProperty(value = "支付类型")
    private String payType;

    @ApiModelProperty(value = "通道名称")
    private String channelName;

    @ApiModelProperty(value = "通道服务名称")
    private String channelEnName;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @ApiModelProperty(value = "操作员")
    private String creator;

    @ApiModelProperty(value = "操作员")
    private String modifier;

    @ApiModelProperty(value = "启用禁用")
    private Boolean enabled;


}
