package com.payment.common.dto;
import com.payment.common.constant.AD3Constant;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.constant.TradeConstant;
import com.payment.common.entity.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.math.BigDecimal;

/**
 * @author XuWenQi
 * @Date: 2019/3/7 15:13
 * @Description: 资金变动接口参数实体
 */
@Data
@ApiModel(value = "资金变动接口参数实体", description = "资金变动接口参数实体")
public class FundChangeDTO {

    @ApiModelProperty(value = "版本号")//固定v1.0
    private String version = "v1.0";

    @ApiModelProperty(value = "字符集")//1.utf-8 2.gbk
    private String inputCharset;

    @ApiModelProperty(value = "语言")//1中文 2英文
    private String language;

    @ApiModelProperty(value = "商户号")
    private String merchantid;

    @ApiModelProperty(value = "是否清算")//清结算类型，1：清算，2结算
    private Integer isclear;

    @ApiModelProperty(value = "参考业务流水号")//所属业务表,eg:退款表记录flow,提款表记录flow
    private String refcnceFlow;

    @ApiModelProperty(value = "交易类型")//交易类型，NT：收单，RF：退款，RV：撤销，WD：提款，
    private String tradetype;

    @ApiModelProperty(value = "商户订单号")
    private String merOrderNo;

    @ApiModelProperty(value = "交易币种")//3位大写
    private String txncurrency;

    @ApiModelProperty(value = "交易金额")//小数保留2位
    private String txnamount;

    @ApiModelProperty(value = "应结日期")//yyyy-MM-dd HH:mm:ss
    private String shouldDealtime;

    @ApiModelProperty(value = "系统订单号")//交易订单号
    private String sysorderid;

    @ApiModelProperty(value = "手续费")//小数保留2位
    private String fee;

    @ApiModelProperty(value = "通道成本")//小数保留2位
    private String channelCost;

    @ApiModelProperty(value = "资金类型")//1：正常资金，2：冻结资金
    private String balancetype;

    @ApiModelProperty(value = "交易描述")
    private String txndesc;

    @ApiModelProperty(value = "交易汇率")//保留五位小数
    private String txnexrate;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "结算币种")
    private String sltcurrency;

    @ApiModelProperty(value = "结算金额")//保留二位小数
    private String sltamount;

    @ApiModelProperty(value = "手续费币种")
    private String feecurrency;

    @ApiModelProperty(value = "通道成本币种")//保留二位小数
    private String channelCostcurrency;

    @ApiModelProperty(value = "网关手续费")//保留二位小数
    private String gatewayFee;

    @ApiModelProperty(value = "签名字符串")
    private String signMsg;

    public FundChangeDTO() {
    }


    /**
     * 退款用
     * @param tradetype
     * @param orderRefund
     */
    public FundChangeDTO(String tradetype, OrderRefund orderRefund) {
        this.inputCharset = AD3Constant.CHARSET_UTF_8;//编码
        this.language = AD3Constant.LANGUAGE_CN;//语言
        this.merchantid = orderRefund.getInstitutionCode();//商户号
        this.refcnceFlow = orderRefund.getId();//业务流水号
        this.tradetype = tradetype;//交易类型 NT：收单，RF：退款，RV：撤销，WD：提款，AA:调账，TA:转账
        this.merOrderNo = orderRefund.getInstitutionOrderId();//机构订单号
        this.txncurrency = orderRefund.getOrderCurrency();//订单币种
        this.txnamount = "-" + (orderRefund.getAmount().add(orderRefund.getRefundFee())).setScale(2, BigDecimal.ROUND_HALF_UP);//订单金额,2位  退款金额手续费
        this.sysorderid = orderRefund.getOrderId();//原订单id
        if (tradetype.equals(TradeConstant.RF)) {
            //退款
            this.balancetype = TradeConstant.FROZEN_FUND;//冻结资金
            this.isclear = TradeConstant.SETTLE;//是否清算
        } else if (tradetype.equals(TradeConstant.RV)) {
            //撤销
            this.balancetype = TradeConstant.NORMAL_FUND;//正常资金
            this.isclear = TradeConstant.CLEARING;//是否清算
        } else if (tradetype.equals(TradeConstant.AA)) {
            //调账
            this.balancetype = TradeConstant.NORMAL_FUND;//正常资金
            this.isclear = TradeConstant.SETTLE;//是否清算
        }
        if (orderRefund.getGoodsDescription() != null) {
            this.txndesc = orderRefund.getGoodsDescription();//商品描述
        } else {
            this.txndesc = "";
        }
        this.txnexrate = String.valueOf(orderRefund.getExchangeRate().setScale(5, BigDecimal.ROUND_HALF_UP));//汇率,2位
        if (orderRefund.getRemark() != null) {
            this.remark = orderRefund.getRemark();//备注
        } else {
            this.remark = "";
        }
        this.sltcurrency = orderRefund.getOrderCurrency();//结算币种
        this.sltamount = "-" + (orderRefund.getAmount().add(orderRefund.getRefundFee())).setScale(2, BigDecimal.ROUND_HALF_UP);//结算金额
        this.channelCostcurrency = orderRefund.getOrderCurrency();//通道成本币种
        this.gatewayFee = "0.00";//网关手续费
        this.fee = "0.00";//手续费,2位
        this.feecurrency = orderRefund.getOrderCurrency();//手续费币种
        this.channelCost = "0.00";//通道成本 2位
    }

    /**
     * 调账用
     *
     * @return
     * @Author YangXu
     * @Date 2019/3/14
     * @Descripate
     **/
    public FundChangeDTO(Reconciliation reconciliation) {
        this.inputCharset = AD3Constant.CHARSET_UTF_8;//编码
        this.language = AD3Constant.LANGUAGE_CN;//语言
        this.merchantid = reconciliation.getInstitutionCode();//商户号
        this.refcnceFlow = reconciliation.getId();//业务流水号
        this.tradetype = TradeConstant.AA;//交易类型 NT：收单，RF：退款，RV：撤销，WD：提款，AA:调账，TA:转账
        this.merOrderNo = reconciliation.getInstitutionOrderId() == null ? reconciliation.getId() : reconciliation.getInstitutionOrderId();//机构订单号
        this.txncurrency = reconciliation.getOrderCurrency();//订单币种
        if (reconciliation.getReconciliationType() == AsianWalletConstant.RECONCILIATION_IN) {
            //调入
            this.balancetype = TradeConstant.NORMAL_FUND;//正常资金
            this.txnamount = String.valueOf(reconciliation.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP));//订单金额,2位
            this.sltamount = String.valueOf(reconciliation.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP));//结算金额
        } else {
            //调出
            this.balancetype = TradeConstant.FROZEN_FUND;//冻结资金
            this.txnamount = "-" + reconciliation.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP);//订单金额,2位
            this.sltamount = "-" + reconciliation.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP);//结算金额
        }
        this.sysorderid = reconciliation.getOrderId() == null ? reconciliation.getId() : reconciliation.getOrderId();//原订单id
        this.isclear = TradeConstant.SETTLE;//是否清算
        this.txndesc = "调账";//交易描述
        if (reconciliation.getExchangeRate() == null) {
            this.txnexrate = "1.00000";//没有汇率的场合
        } else {
            this.txnexrate = String.valueOf(reconciliation.getExchangeRate().setScale(5, BigDecimal.ROUND_HALF_UP));//汇率,2位
        }
        this.remark = reconciliation.getRemark();//备注
        this.sltcurrency = reconciliation.getOrderCurrency();//结算币种
        this.channelCostcurrency = reconciliation.getOrderCurrency();//通道成本币种
        this.gatewayFee = "0.00";//网关手续费
        this.fee = "0.00";//手续费,2位
        this.feecurrency = reconciliation.getOrderCurrency();//手续费币种
        this.channelCost = "0.00";//通道成本 2位
    }


    /**
     * 根据订单信息调用清结算的资金变动接口
     *收单用
     * @param orders
     * @param tradeType
     */
    public FundChangeDTO(Orders orders, String tradeType, String institutionId) {
        this.inputCharset = AD3Constant.CHARSET_UTF_8;//编码
        this.language = AD3Constant.LANGUAGE_CN;//语言
        this.merchantid = institutionId;//商户号
        this.isclear = TradeConstant.CLEARING;//是否清算
        this.refcnceFlow = orders.getId();//业务流水号
        this.tradetype = tradeType;//交易类型 NT：收单，RF：退款，RV：撤销，WD：提款，AA:调账，TA:转账
        this.merOrderNo = orders.getInstitutionOrderId();//机构订单号
        this.txncurrency = orders.getOrderCurrency();//订单币种
        this.txnamount = String.valueOf(orders.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP));//订单金额,2位
        this.sysorderid = orders.getId();//订单id
        this.fee = String.valueOf(orders.getFee().setScale(2, BigDecimal.ROUND_HALF_UP));//手续费,2位
        this.channelCost = "0.00";//通道成本 2位
        this.balancetype = TradeConstant.NORMAL_FUND;//资金类型,正常资金
        this.txndesc = orders.getGoodsDescription();//商品描述
        this.txnexrate = String.valueOf(orders.getExchangeRate().setScale(5, BigDecimal.ROUND_HALF_UP));//汇率,2位
        this.remark = orders.getRemark();//备注
        this.sltcurrency = orders.getOrderCurrency();//结算币种
        this.sltamount = String.valueOf(orders.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP));//结算金额
        this.feecurrency = orders.getOrderCurrency();//手续费币种
        this.channelCostcurrency = orders.getOrderCurrency();//通道成本币种
        this.gatewayFee = "0.00";//网关手续费
        this.shouldDealtime = orders.getProductSettleCycle();//应结算日期
    }

    /**
     * 机构结算自动提款用
     * @param settleOrder
     */
    public FundChangeDTO(SettleOrder settleOrder) {
        this.inputCharset = AD3Constant.CHARSET_UTF_8;//编码
        this.language = AD3Constant.LANGUAGE_CN;//语言
        this.merchantid = settleOrder.getInstitutionCode();//商户号
        this.refcnceFlow = settleOrder.getId();//业务流水号
        this.tradetype = TradeConstant.WD;//交易类型 WD：提款
        this.merOrderNo = settleOrder.getId();//结算交易的流水号
        this.txncurrency = settleOrder.getTxncurrency();//交易币种
        this.txnamount = "-" + settleOrder.getTxnamount().setScale(2, BigDecimal.ROUND_HALF_UP);//订单金额,2位
        this.sltamount = "-" + settleOrder.getTxnamount().setScale(2, BigDecimal.ROUND_HALF_UP);//结算金额
        this.sysorderid =settleOrder.getId();//结算交易的流水号
        this.balancetype = TradeConstant.FROZEN_FUND;//冻结资金
        this.isclear = TradeConstant.SETTLE;//是否清算
        this.txndesc = "提款";//交易描述
        this.txnexrate = "1.00000";//没有汇率的场合
        this.remark = settleOrder.getRemark();//备注
        this.sltcurrency = settleOrder.getTxncurrency();//结算币种
        this.channelCostcurrency = settleOrder.getTxncurrency();//通道成本币种
        this.gatewayFee = "0.00";//网关手续费
        this.fee = "0.00";//手续费,2位
        this.feecurrency = settleOrder.getTxncurrency();//手续费币种
        this.channelCost = "0.00";//通道成本 2位
    }

    /**
     * 付款
     * @param orderPayment
     */
    public FundChangeDTO(OrderPayment orderPayment) {
        this.inputCharset = AD3Constant.CHARSET_UTF_8;//编码
        this.language = AD3Constant.LANGUAGE_CN;//语言
        this.merchantid = orderPayment.getInstitutionCode();//商户号
        this.refcnceFlow = orderPayment.getId();//业务流水号
        this.tradetype = TradeConstant.PM;//交易类型 PM：付款
        this.merOrderNo = orderPayment.getId();//结算交易的流水号
        this.txncurrency = orderPayment.getTradeCurrency();//交易币种
        this.txnamount = "-" + orderPayment.getTradeAmount().add(orderPayment.getFee()).setScale(2, BigDecimal.ROUND_HALF_UP);//订单金额,2位
        this.sltamount = "-" + orderPayment.getTradeAmount().add(orderPayment.getFee()).setScale(2, BigDecimal.ROUND_HALF_UP);//结算金额
        this.sysorderid =orderPayment.getId();//结算交易的流水号
        this.balancetype = TradeConstant.FROZEN_FUND;//冻结资金
        this.isclear = TradeConstant.SETTLE;//是否清算
        this.txndesc = "付款";//交易描述
        this.txnexrate = String.valueOf(orderPayment.getExchangeRate().setScale(5, BigDecimal.ROUND_HALF_UP));//汇率,2位
        this.remark = orderPayment.getRemark();//备注
        this.sltcurrency = orderPayment.getTradeCurrency();//结算币种
        this.channelCostcurrency = orderPayment.getTradeCurrency();//通道成本币种
        this.gatewayFee = "0.00";//网关手续费
        this.fee = "0.00";//手续费,2位
        this.feecurrency = orderPayment.getTradeCurrency();//手续费币种
        this.channelCost = "0.00";//通道成本 2位
        this.setShouldDealtime(orderPayment.getExtend1());//应结算日期
    }
}
