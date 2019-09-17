package com.payment.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @description: 代理商账户查询EN VO
 * @author: shenxinran
 * @create: 2019-08-26
 **/
@Data
@ApiModel(value = "代理商账户查询EN VO", description = "代理商账户查询EN VO")
public class AgentAccountExportENDTO {

//    @ApiModelProperty(value = "Institution id")
//    private String institutionCode;
//
//    @ApiModelProperty(value = "Institution Name")
//    private String institutionName;
//
//    @ApiModelProperty(value = "account Code")
//    private String accountCode;

    @ApiModelProperty(value = "Account currency")
    private String currency;

    @ApiModelProperty(value = "Account balance")
    private BigDecimal balance = BigDecimal.ZERO;

    @ApiModelProperty(value = "margin balance")
    private BigDecimal marginBalance = BigDecimal.ZERO;

//    @ApiModelProperty(value = "Creation time")
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
//    public Date createTime;
//
//    @ApiModelProperty(value = "Modification time")
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
//    private Date updateTime;
//
//    @ApiModelProperty(value = "creator")
//    private String creator;
//
//    @ApiModelProperty(value = "Changer")
//    private String modifier;
//
//    @ApiModelProperty(value = "Remarks")
//    private String remark;
}
