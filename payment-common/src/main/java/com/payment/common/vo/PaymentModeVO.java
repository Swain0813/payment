package com.payment.common.vo;

import com.payment.common.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 支付方式管理
 */
@Data
@ApiModel(value = "支付方式管理", description = "支付方式管理")
public class PaymentModeVO extends BaseEntity {

    @ApiModelProperty(value = "支付方式ID")
    private String id;

    @ApiModelProperty(value = "数据字典ID")
    private String dictionaryId;

    @ApiModelProperty(value = "支付方式CODE")
    private String payType;

    @ApiModelProperty(value = "交易类型CODE")
    private String dealType;

    @ApiModelProperty(value = "支付方式名称")
    private String payTypeName;

    @ApiModelProperty(value = "字典-支付方式图标")
    private String dIcon;

    @ApiModelProperty(value = "支付-支付方式图标")
    private String pIcon;

    @ApiModelProperty(value = "启用禁用")
    private Boolean enabled;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @ApiModelProperty(value = "创建者")
    private String creator;

    @ApiModelProperty(value = "支付方式名称")
    private String name;

    @ApiModelProperty(value = "更新者")
    private String modifier;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "语种code")
    private String language;

}
