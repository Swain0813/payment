package com.payment.common.dto;


import com.payment.common.base.BasePageHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @description: 运营后台分润报表查询DTO
 * @author: XuWenQi
 * @create: 2019-08-26 11:44
 **/
@Data
@ApiModel(value = "运营后台分润报表查询DTO", description = "运营后台分润报表查询DTO")
public class QueryShareBenefitReportDTO extends BasePageHelper {

    @ApiModelProperty(value = "订单id")
    private String orderId;

    @ApiModelProperty(value = "代理商名称")
    private String agencyName;

    @ApiModelProperty(value = "商户编号")
    private String institutionCode;

    @ApiModelProperty(value = "商户名称")
    private String institutionName;

    @ApiModelProperty(value = "分润状态")//1:待分润，2：已分润
    private Byte isShare;

    @ApiModelProperty(value = "起始时间")
    private String startDate;

    @ApiModelProperty(value = "结束时间")
    private String endDate;

    @ApiModelProperty(value = "代理商编号")
    private String agencyCode;

    @ApiModelProperty(value = "订单币种")
    private String orderCurrency;

}
