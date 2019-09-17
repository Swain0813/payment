package com.payment.common.dto;


import com.payment.common.base.BasePageHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @description: DCC报表查询输入实体
 * @author: XuWenQi
 * @create: 2019-07-26 14:24
 **/
@Data
@ApiModel(value = "DCC报表查询输入实体", description = "DCC报表查询输入实体")
public class DccReportDTO extends BasePageHelper {

    @ApiModelProperty(value = "订单id")
    private String id;

    @ApiModelProperty(value = "商户名称")
    private String institutionName;

    @ApiModelProperty(value = "商户编号")
    private String institutionCode;

    @ApiModelProperty(value = "商户订单号")
    private String institutionOrderId;

    @ApiModelProperty(value = "订单币种")
    private String orderCurrency;

    @ApiModelProperty(value = "起始时间")
    private String startDate;

    @ApiModelProperty(value = "结束时间")
    private String endDate;

}
