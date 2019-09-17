package com.payment.trade.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author XuWenQi
 * @Date: 2019/3/6 14:46
 * @Description: 退款订单输出实体
 */
@Data
@ApiModel(value = "退款订单输出实体", description = "退款订单输出实体")
public class OrderRefundVO {

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @ApiModelProperty(value = "退款流水号")
    private String id;

    @ApiModelProperty(value = "机构退款金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "退款汇率")
    private BigDecimal exchangeRate;

    @ApiModelProperty(value = "退款金额")
    private BigDecimal tradeAmount;

    @ApiModelProperty(value = "退款币种")
    private String tradeCurrency;

    @ApiModelProperty(value = "退款通道")
    private String channelName;

    @ApiModelProperty(value = "退款状态 1：退款中 2：退款成功 3：退款失败")
    private Byte refundStatus;

    @ApiModelProperty(value = "退款完成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @ApiModelProperty(value = "备注")
    private String remark;
}
