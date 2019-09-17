package com.payment.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @description: 机构产品关联所有通道实体
 * @author: YangXu
 * @create: 2019-02-22 09:52
 **/
@Data
@ApiModel(value = "机构产品关联所有通道实体", description = "机构产品关联所有通道实体")
public class ProChannelVO {

    @ApiModelProperty(value = "机构产品中间表id")
    private String insProductId;

    @ApiModelProperty(value = "产品id")
    private String productId;
    @ApiModelProperty(value = "产品编号")
    private String productCode;
    @ApiModelProperty(value = "支付类型")
    private String payType;
    @ApiModelProperty(value = "币种")
    private String currency;
    @ApiModelProperty(value = "关联的通道")
    private List<ChannelVO> channelVOS;
}
