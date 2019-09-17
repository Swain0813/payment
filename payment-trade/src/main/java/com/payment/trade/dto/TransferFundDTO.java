package com.payment.trade.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author XuWenQi
 * @Date: 2019/3/7 15:38
 * @Description: 转账接口参数实体
 */
@Data
@ApiModel(value = "转账接口参数实体", description = "转账接口参数实体")
public class TransferFundDTO {

    @ApiModelProperty(value = "版本号")//固定v1.0
    private String version = "v1.0";

    @ApiModelProperty(value = "字符集")//1.utf-8 2.gbk
    private String inputCharset;

    @ApiModelProperty(value = "语言")//1中文 2英文
    private String language;

    @ApiModelProperty(value = "出款商户号")
    private String fromMerchantId;

    @ApiModelProperty(value = "转出账户编号")//清结算类型，1：清算，2结算
    private String fromVAccountNo;

    @ApiModelProperty(value = "账户类型")//1清算户（不可用），2结算户
    private String type;

    @ApiModelProperty(value = "转入商户编号")
    private String toMerchantId;

    @ApiModelProperty(value = "转入账户编号")
    private String toAccountNo;

    @ApiModelProperty(value = "参考的交易类型流水号")//3位大写
    private String refcnceFlow;

    @ApiModelProperty(value = "交易类型")//交易类型（NT,DT,RF,RV,WD）
    private String tradetype;

    @ApiModelProperty(value = "商户上送的商户订单号")//并且只能由数字，字母，”-”,”_”组成，并以字母或者数字开头,商户订单号不论交易成功或失败，不允许重复"
    private String merOrderNo;

    @ApiModelProperty(value = "出款币种")//订单币种，三位ISO代码
    private String outtxncurrency;

    @ApiModelProperty(value = "出款金额")//订单金额，以元为单位，最多保留小数后2位
    private String outtxnamount;

    @ApiModelProperty(value = "交易汇率")//保留五位小数
    private String txnexRate;

    @ApiModelProperty(value = "入款币种")
    private String intxncurrency;

    @ApiModelProperty(value = "入款金额")//保留两位小数
    private String intxnamount;

    @ApiModelProperty(value = "清算状态")//1待清算，2已清算
    private String state;

    @ApiModelProperty(value = "应清(结)算时间")
    private String shouldDealtime;

    @ApiModelProperty(value = "系统订单号")
    private String sysorderid;

    @ApiModelProperty(value = "交易手续费")//保留两位小数
    private String fee;

    @ApiModelProperty(value = "交易通道成本")//保留两位小数
    private String channelCost;

    @ApiModelProperty(value = "资金类型")//1正常资金，2冻结资金
    private String balancetype;

    @ApiModelProperty(value = "手续费承担方")//1表示转出方承担，2表示接收方承担
    private String feeParty;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "签名文字符串")
    private String signMsg;

}
