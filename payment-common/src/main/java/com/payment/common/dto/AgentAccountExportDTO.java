package com.payment.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @description: 代理商账户查询VO
 * @author: shenxinran
 * @create: 2019-08-26
 **/
@Data
@ApiModel(value = "代理商账户查询VO", description = "代理商账户查询VO")
public class AgentAccountExportDTO {

//    @ApiModelProperty(value = "机构号")
//    private String institutionCode;
//
//    @ApiModelProperty(value = "机构名称")
//    private String institutionName;
//
//    @ApiModelProperty(value = "账户编号")
//    private String accountCode;

    @ApiModelProperty(value = "账户币种")
    private String currency;

    @ApiModelProperty(value = "账户余额")
    private BigDecimal balance = BigDecimal.ZERO;

    @ApiModelProperty(value = "保证金余额")
    private BigDecimal marginBalance = BigDecimal.ZERO;

//    @ApiModelProperty(value = "创建时间")
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
//    public Date createTime;
//
//    @ApiModelProperty(value = "修改时间")
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
//    private Date updateTime;
//
//    @ApiModelProperty(value = "创建者")
//    private String creator;
//
//    @ApiModelProperty(value = "更改者")
//    private String modifier;
//
//    @ApiModelProperty(value = "备注")
//    private String remark;
}
