package com.payment.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @description: 代理商商户信息英文VO
 * @author: XuWenQi
 * @create: 2019-08-23 14:38
 **/
@Data
@ApiModel(value = "代理商商户信息英文VO", description = "代理商商户信息英文VO")
public class AgencyInstitutionEnVO {

    @ApiModelProperty(value = "Institution Number")
    private String institutionCode;

    @ApiModelProperty(value = "Institution Name")
    private String institutionName;

    @ApiModelProperty(value = "Product Name")
    private String productName;

    @ApiModelProperty(value = "Rate Type")
    private String rateType;

    @ApiModelProperty(value = "Rate")
    private BigDecimal rate;

    @ApiModelProperty(value = "Additional Value")
    private BigDecimal addValue;

    @ApiModelProperty(value = "Audit Status")
    private Byte auditStatus;

    @ApiModelProperty(value = "Creation Time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createTime;

}
