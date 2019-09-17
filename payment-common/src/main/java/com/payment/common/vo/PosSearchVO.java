package com.payment.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @description: pos机交易打印查询输出实体
 * @author: YangXu
 * @create: 2019-04-08 14:20
 **/
@Data
@ApiModel(value = "pos机交易打印查询输出实体", description = "pos机交易打印查询输出实体")
public class PosSearchVO {

    @ApiModelProperty(value = "订单号")
    private String id;

    @ApiModelProperty(value = "机构订单号")
    private String institutionOrderId;

    @ApiModelProperty(value = "机构编号")
    private String institutionCode;

    @ApiModelProperty(value = "设备编号")
    private String deviceCode;

    @ApiModelProperty(value = "设备操作员")
    private String deviceOperator;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createTime;

    @ApiModelProperty(value = "交易类型")
    private String tradeType;//1-订单 2-退款单

    @ApiModelProperty(value = "付款方式")
    @Column(name = "pay_method")
    private String payMethod;

    @ApiModelProperty(value = "订单金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "机构的请求收款币种")
    private String orderCurrency;

    @ApiModelProperty(value = "订单笔数")
    private int count;
}
