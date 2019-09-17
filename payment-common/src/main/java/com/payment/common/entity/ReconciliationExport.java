package com.payment.common.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@Table(name = "reconciliation")
@ApiModel(value = "调账记录导出", description = "调账记录导出")
public class ReconciliationExport {

    @ApiModelProperty(value = "机构号")
    private String institutionCode;

    @ApiModelProperty(value = "机构名称")
    private String institutionName;

    @ApiModelProperty(value = "变动类型")
    private int reconciliationType;//1-调入,2-调出,3-冻结,4-解冻

    @ApiModelProperty(value = "状态")//调账状态 1-待调账 2-调账成功 3-调账失败, 4-待冻结 5-冻结成功 6-冻结失败, 7-待解冻 8-解冻成功 9-解冻失败
    private int status;

    @ApiModelProperty(value = "流水号")
    private String id;

    @ApiModelProperty(value = "变动币种")
    private String orderCurrency;

    @ApiModelProperty(value = "变动金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "变动原因")
    private String remark;

    @ApiModelProperty(value = "申请人")
    private String creator;

    @ApiModelProperty(value = "申请时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createTime;

    @ApiModelProperty(value = "审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @ApiModelProperty(value = "操作员")
    private String modifier;
}
