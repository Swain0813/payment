package com.payment.common.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 批量更新订单物流信息输入参数
 */
@Data
@ApiModel(value = "批量更新订单物流信息输入参数", description = "批量更新订单物流信息输入参数")
public class OrderLogisticsBachDTO {

  @NotNull(message = "50002")
  @ApiModelProperty(value = "批量更新订单物流信息实体")
  List<LogisticsBachDTO>  logisticsBachDTOs;//批量更新物流信息

  @NotNull(message = "50002")
  @ApiModelProperty(value = "签名方式")//1为RSA 2为MD5
  private String signType;

  @NotNull(message = "50002")
  @ApiModelProperty(value = "机构号")
  private String institutionId;

  @NotNull(message = "50002")
  @ApiModelProperty(value = "签名")
  private String sign;
}
