package com.payment.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @description: 代理商商户信息VO
 * @author: XuWenQi
 * @create: 2019-08-23 14:38
 **/
@Data
@ApiModel(value = "代理商商户信息VO", description = "代理商商户信息VO")
public class AgencyInstitutionVO {

    @ApiModelProperty(value = "机构编号")
    private String institutionCode;

    @ApiModelProperty(value = "机构名称")
    private String institutionName;

    @ApiModelProperty(value = "产品名称")
    private String productName;

    @ApiModelProperty(value = "费率类型")
    private String rateType;

    @ApiModelProperty(value = "费率")
    private BigDecimal rate;

    @ApiModelProperty(value = "附加值")
    private BigDecimal addValue;

    @ApiModelProperty(value = "审核状态")
    private Byte auditStatus;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createTime;

}
