package com.payment.common.dto;

import com.payment.common.base.BasePageHelper;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @description: 查询产品实体
 * @author: YangXu
 * @create: 2019-03-04 14:36
 **/
@Data
public class ProductSearchDTO extends BasePageHelper {

    @ApiModelProperty(value = "支付方式")
    private String payType;

    @ApiModelProperty(value = "交易类型")
    private Byte transType;

    @ApiModelProperty(value = "交易场景")
    private Byte tradeDirection;

    @ApiModelProperty(value = "语言")
    private String language;
}
