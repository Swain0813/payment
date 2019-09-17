package com.payment.common.dto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 汇款单导出输入实体
 */
@Data
@ApiModel(value = "汇款单导出输入实体", description = "汇款单导出输入实体")
public class OrderPaymentExportDTO{

    @ApiModelProperty(value = "机构订单号")
    private String institutionOrderId;

    @ApiModelProperty(value = "机构编号")
    private String institutionCode;

    @ApiModelProperty(value = "机构名称")
    private String institutionName;

    @ApiModelProperty(value = "汇款银行名称")
    private String bankAccountName;

    @ApiModelProperty(value = "订单币种")
    private String tradeCurrency;

    @ApiModelProperty(value = "通道名称")
    private String channelName;

    @ApiModelProperty(value = "交易开始时间")
    private String startDate;

    @ApiModelProperty(value = "交易结束时间")
    private String endDate;

    @ApiModelProperty(value = "汇款状态")
    private String payoutStatus;

    @ApiModelProperty(value = "机构上报付款批次号")
    private String institutionBatchNo;

    @ApiModelProperty(value = "收款人")
    private String cardholder;

    @ApiModelProperty(value = "是否人工退款")
    private Boolean isArtificial;

}
