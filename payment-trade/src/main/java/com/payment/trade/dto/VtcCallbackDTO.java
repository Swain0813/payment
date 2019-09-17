package com.payment.trade.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author XuWenQi
 * @Date: 2019/5/29 16:08
 * @Description: vtc
 */
@Data
@ApiModel(value = "vtc回调浏览器输入实体", description = "vtc回调浏览器输入实体")
public class VtcCallbackDTO {

    @ApiModelProperty(value = "订单金额")
    private String amount;

    @ApiModelProperty(value = "message")
    private String message;

    @ApiModelProperty(value = "银行机构号")
    private String payment_type;

    @ApiModelProperty(value = "订单id")
    private String reference_number;

    @ApiModelProperty(value = "订单状态")
    private String status;

    @ApiModelProperty(value = "通道流水号")
    private String trans_ref_no;

    @ApiModelProperty(value = "网站id")
    private String website_id;

    @ApiModelProperty(value = "签名")
    private String signature;

}
