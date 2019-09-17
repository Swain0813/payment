package com.payment.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;


/**
 * @description: 机构关联所有通道
 * @author: YangXu
 * @create: 2019-02-22 09:55
 **/
@Data
@ApiModel(value = "机构产品关联所有通道实体", description = "机构产品关联所有通道实体")
public class ChannelVO {

    @ApiModelProperty(value = "通道id")
    private String channelId;

    @ApiModelProperty(value = "通道编号")
    private String channelCode;

    @ApiModelProperty(value = "通道英文名称")
    private String channelEnName;

    @ApiModelProperty(value = "通道中文名称")
    private String channelCnName;

}
