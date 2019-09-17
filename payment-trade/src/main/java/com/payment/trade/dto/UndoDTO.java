package com.payment.trade.dto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotNull;

/**
 * 撤销订单的输入参数
 */
@Data
@ApiModel(value = "撤销订单请求参数", description = "撤销订单请求参数")
public class UndoDTO {

    @NotNull(message = "50002")
    @ApiModelProperty(value = "商户编号")
    private String institutionId;

    @NotNull(message = "50002")
    @ApiModelProperty(value = "商户订单号")
    private String orderNo; //商户订单号-由商户上送

    @NotNull(message = "50002")
    @ApiModelProperty(value = "设备编号")
    private String terminalId;

    @NotNull(message = "50002")
    @ApiModelProperty(value = "设备操作员")
    private String operatorId;

    @NotNull(message = "50002")
    @ApiModelProperty(value = "签名")
    private String sign;
}
