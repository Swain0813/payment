package com.payment.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;


/**
 * @description: 产品通道管理导出实体
 * @author: shenxinran
 * @create: 2019年8月28日14:05:43
 **/
@Data
@ApiModel(value = "产品通道管理导出实体", description = "产品通道管理导出实体")
public class ProductChannelExportVO {

    @ApiModelProperty(value = "机构号")
    private String institutionCode;

    @ApiModelProperty(value = "机构名称")
    private String institutionName;

    @ApiModelProperty(value = "产品名称")
    private String payType;

    @ApiModelProperty(value = "通道名称")
    private String channelName;

    @ApiModelProperty(value = "通道服务名称")
    private String channelEnName;

    @ApiModelProperty(value = "支持银行")
    private String bankName;

    @ApiModelProperty(value = "通道币种")
    private String currency;

    @ApiModelProperty(value = "优先级")
    private String sort;

    @ApiModelProperty(value = "银行机构代码")
    private String issuerId;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @ApiModelProperty(value = "创建人")
    private String creator;

    @ApiModelProperty(value = "修改人")
    private String modifier;

    @ApiModelProperty(value = "启用禁用")
    private Boolean enabled;


}
