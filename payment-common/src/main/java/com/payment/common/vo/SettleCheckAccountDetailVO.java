package com.payment.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

/**
 * @description:
 * @author: YangXu
 * @create: 2019-08-08 15:35
 **/
@Data
@ApiModel(value = "机构对账结算单详情", description = "机构对账结算单详情")
public class SettleCheckAccountDetailVO {

    @Id
    public String id;

    // 创建时间
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createTime;

    @ApiModelProperty(value = "修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @ApiModelProperty(value = "创建者")
    private String creator;

    @ApiModelProperty(value = "更改者")
    private String modifier;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "结算流水号")
    private String STFlow;

    @ApiModelProperty(value = "关联pgworder，退款表，提款表等流水号")
    private String refcnceFlow;

    @ApiModelProperty(value = "交易类型")
    private String tradetype;// NT:收单，DT：分账，RF:退款，WD：提款，CL:清算，ST:结算

    @ApiModelProperty(value = "商户编号")
    private String merchantid;

    @ApiModelProperty(value = "商户订单号")
    private String merOrderNo;

    @ApiModelProperty(value = "交易币种")
    private String txncurrency;

    @ApiModelProperty(value = "交易金额")
    private Double txnamount;

    @ApiModelProperty(value = "交易手续费")
    private Double fee;

    @ApiModelProperty(value = "手续费币种")
    private String feecurrency;

    @ApiModelProperty(value = "渠道成本")
    private Double channelCost;

    @ApiModelProperty(value = "渠道成本币种")
    private String channelcostcurrency;

    @ApiModelProperty(value = "revokemount")
    private Double revokemount;

    @ApiModelProperty(value = "业务类型要和业务账户类型一致")
    private Integer businessType;

    @ApiModelProperty(value = "资金类型")
    private Integer balancetype;//：1正常资金，2冻结资金

    @ApiModelProperty(value = "所属业务账户编号")
    private String mbuaccountId;

    @ApiModelProperty(value = "结算账户")
    private String accountNo;

    @ApiModelProperty(value = "结算状态")
    private Integer STstate;// 1未结算，2已结算

    @ApiModelProperty(value = "应结算时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date shouldSTtime;

    @ApiModelProperty(value = "实际结算时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date actualSTtime;

    @ApiModelProperty(value = "系统订单号")
    private String sysorderid;

    @ApiModelProperty(value = "系统添加时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date addDatetime;

    @ApiModelProperty(value = "交易说明")
    private String txndesc;

    @ApiModelProperty(value = "结算金额")
    private Double sltamount;

    @ApiModelProperty(value = "结算币种")
    private String sltcurrency;

    @ApiModelProperty(value = "交易当时汇率，国际业务")
    private Double txnexrate;

    @ApiModelProperty(value = "交易状态手续费")
    private Double gatewayFee;

    @ApiModelProperty(value = "机构编号")
    private String organId;

    @ApiModelProperty(value = "是否需要处理清除")
    private Integer needClear;//：1不需要，2需要

    private double balance;
    private double afterBalance;

}
