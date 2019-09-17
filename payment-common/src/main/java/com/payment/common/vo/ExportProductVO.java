package com.payment.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @description: 产品实体
 * @author: XuWenQi
 * @create: 2019-05-24 10:49
 **/
@Data
@ApiModel(value = "ExportProductVO", description = "导出产品输出实体")
public class ExportProductVO {

    @ApiModelProperty(value = "产品名称")
    private String productName;

    @ApiModelProperty(value = "产品编号")
    private Integer productCode;

    @ApiModelProperty(value = "交易类型")
    private Byte transType;

    @ApiModelProperty(value = "支付类型")
    private String payType;

    @ApiModelProperty(value = "交易场景")
    private Byte tradeDirection;

    @ApiModelProperty(value = "币种")
    private String currency;

    @ApiModelProperty(value = "单笔交易限额")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal limitAmount;

    @ApiModelProperty(value = "日累计交易笔数")
    private Integer dailyTradingCount;

    @ApiModelProperty(value = "日累计交易总额")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal dailyTotalAmount;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createTime;

    @ApiModelProperty(value = "备注")
    private String remark;

}
