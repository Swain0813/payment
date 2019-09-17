package com.payment.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import java.util.Date;

/**
 * @description: 账户余额详情导出实体
 * @author: YangXu
 * @create: 2019-04-04 13:37
 **/
@Data
@ApiModel(value = "账户余额变动记录表", description = "账户余额变动记录表")
public class TmMerChTvAcctBalanceVO {

    @ApiModelProperty(value = "交易的流水号")
    private String referenceflow;

    @ApiModelProperty(value = "币种")
    private String currency;


    @ApiModelProperty(value = "交易类型")
    private String tradetype;// NT:收单，DT：分账，RF:退款，WD：提款，CL:清算，ST:结算

    @ApiModelProperty(value = "交易金额")
    private Double txnamount;


    @ApiModelProperty(value = "加入金额")
    private Double income;

    @ApiModelProperty(value = "减少金额")
    private Double outcome;

    @ApiModelProperty(value = "收取手续费")
    private Double fee;

    @ApiModelProperty(value = "原账户余额")
    private Double balance;

    @ApiModelProperty(value = "变动后账户余额")
    private Double afterbalance;

    @ApiModelProperty(value = "交易状态手续费")
    private Double gatewayFee;

    @ApiModelProperty(value = "系统添加日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date sysAddDate;

    @ApiModelProperty(value = "变动时间")
    @Column(name = "balance_timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date balanceTimestamp;

    @ApiModelProperty(value = "备注")
    private String remark;


}
