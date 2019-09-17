package com.payment.trade.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "线下查询产品输出实体", description = "线下查询产品输出实体")
public class OfflineProductVO {

    @ApiModelProperty(value = "产品编号")
    private Integer productCode;

    @ApiModelProperty(value = "交易类型")
    private String dealType;

    @ApiModelProperty(value = "支付方式名称")
    private String payTypeName;

    @ApiModelProperty(value = "产品支付方式logo1 带字的(支付方式表)")
    private String payTypeImgOne;

    @ApiModelProperty(value = "产品支付方式logo2 圆的(字典表)")
    private String payTypeImgTwo;

    @ApiModelProperty(value = "支付方式标记")
    private String flag;
}
