package com.payment.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import java.util.Date;

/**
 * @description: 冻结资金记录详情
 * @author: YangXu
 * @create: 2019-04-04 13:40
 **/
@Data
@ApiModel(value = "系统冻结资金记录", description = "系统冻结资金记录")
public class TcsFrozenFundsLogsVO {


    @ApiModelProperty(value = "商户订单号")
    private String merOrderNo;

    @ApiModelProperty(value = "交易币种")
    private String txncurrency;

    @ApiModelProperty(value = "交易金额")
    private Double txnamount;


    @ApiModelProperty(value = "状态")
    private Integer state;//1已冻结，2已解冻

    @ApiModelProperty(value = "冻结时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date frozenDatetime;


    @ApiModelProperty(value = "解冻时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date unfreezeDatetime;

    @ApiModelProperty(value = "冻结备注")
    private String frozenDesc;

    @ApiModelProperty(value = "解冻备注")
    private String unfrozenDesc;
}
