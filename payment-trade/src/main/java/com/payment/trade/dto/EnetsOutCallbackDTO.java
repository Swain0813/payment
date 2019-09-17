package com.payment.trade.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author XuWenQi
 * @Date: 2019/5/29 16:08
 * @Description: AD3回调信息实体
 */
@Data
@ApiModel(value = "eghl回调浏览器输入实体", description = "eghl回调浏览器输入实体")
public class EnetsOutCallbackDTO {

    @ApiModelProperty(value = "默认为1")
    private String ss;

    @ApiModelProperty(value = "")
    private EnetsCallbackDTO msg;

}
