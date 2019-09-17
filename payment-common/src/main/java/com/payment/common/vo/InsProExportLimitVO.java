package com.payment.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @description: 机构产品限额导出实体
 * @author: YangXu
 * @create: 2019-03-11 16:28
 **/
@Data
@ApiModel(value = "机构产品限额导出实体", description = "机构产品限额导出实体")
public class InsProExportLimitVO {



    @ApiModelProperty(value = "机构code")
    private String institutionCode;

    @ApiModelProperty(value = "机构名称")
    private String institutionName;

    @ApiModelProperty(value = "产品")
    private String payType;

    @ApiModelProperty(value = "单笔交易限额")
    private BigDecimal limitAmount;

    @ApiModelProperty(value = "日累计交易笔数")
    private Integer dailyTradingCount;

    @ApiModelProperty(value = "日累计交易总额")
    private BigDecimal dailyTotalAmount;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createTime;

    @ApiModelProperty(value = "修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
