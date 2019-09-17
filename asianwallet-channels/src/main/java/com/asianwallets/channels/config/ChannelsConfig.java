package com.asianwallets.channels.config;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * 支付服务配置参数读取类
 */
@Data
@Configuration("channelsConfig")
public class ChannelsConfig {

    @ApiModelProperty("eghl系统的url地址")//该上游通道已经被暂停使用
    @Value("${custom.eghl.pay-url}")
    private String payUrl;

    @ApiModelProperty("megaPayTHBUrl系统的url地址")
    @Value("${custom.megaPay.megaPayTHBUrl}")
    private String megaPayTHBUrl;

    @ApiModelProperty("megaPayIDRUrl系统的url地址")
    @Value("${custom.megaPay.megaPayIDRUrl}")
    private String megaPayIDRUrl;

    @ApiModelProperty("nextPosUrl系统的url地址")
    @Value("${custom.megaPay.nextPosUrl}")
    private String nextPosUrl;

    @ApiModelProperty("nextPosUrl查询的url地址")
    @Value("${custom.megaPay.nextPosQueryUrl}")
    private String nextPosQueryUrl;

    @ApiModelProperty("nextPosUrl退款的url地址")
    @Value("${custom.megaPay.nextPosRefundUrl}")
    private String nextPosRefundUrl;

    @ApiModelProperty("vtcPay系统的url地址")
    @Value("${custom.vtcPay.vtcPayUrl}")
    private String vtcPayUrl;

    @ApiModelProperty("eNetsDebit系统的url地址")
    @Value("${custom.eNets.eNetsDebit}")
    private String eNetsDebitUrl;

    @ApiModelProperty("eNetsSM系统的url地址")
    @Value("${custom.eNets.eNetsSM}")
    private String eNetsSMUrl;

    @ApiModelProperty("eNetsPOS系统的url地址")
    @Value("${custom.eNets.eNetsPOS}")
    private String eNetsPOSUrl;

    @ApiModelProperty("eNetsPOS系统的url地址")
    @Value("${custom.local.eNets-jumpurl}")
    private String eNetsJumpUrl;

    @ApiModelProperty("help2Pay系统的url地址")
    @Value("${custom.help2Pay.help2PayUrl}")
    private String help2PayUrl;

    @ApiModelProperty("help2Pay系统的提款url地址")
    @Value("${custom.help2Pay.help2PayOutUrl}")
    private String help2PayOutUrl;

    @ApiModelProperty("help2Pay系统的汇款白名单IP")
    @Value("${custom.help2Pay.help2PayOutIP}")
    private String help2PayOutIP;

    @ApiModelProperty("aliPay系统的线下BSC地址")
    @Value("${custom.aliPay.offlineBSC}")
    private String aliPayOfflineBSC;

    @ApiModelProperty("aliPay系统的CSB地址")
    @Value("${custom.aliPay.CSB}")
    private String aliPayCSBUrl;

    @ApiModelProperty("aliPay系统的线下退款地址")
    @Value("${custom.aliPay.refundUrl}")
    private String aliPayRefundUrl;

    @ApiModelProperty("NganLuong系统的pay地址")
    @Value("${custom.NganLuong.payUrl}")
    private String nganLuongPayUrl;


    @ApiModelProperty("xendit系统的收单地址")
    @Value("${custom.xendit.cusPayurl}")
    private String xenditCusPayUrl;

    @ApiModelProperty("xendit系统的支付资金地址")
    @Value("${custom.xendit.payUrl}")
    private String xenditPayUrl;

    @ApiModelProperty("xendit查询可用银行地址")
    @Value("${custom.xendit.banksUrl}")
    private String xenditBanksUrl;

    @ApiModelProperty("WECHAT系统的线下CSB地址")
    @Value("${custom.wechat.offlineCSB}")
    private String wechatOfflineCSB;

    @ApiModelProperty("WECHAT系统的线下BSC地址")
    @Value("${custom.wechat.offlineBSC}")
    private String wechatOfflineBSC;

    @ApiModelProperty("WECHAT系统的线下查询地址")
    @Value("${custom.wechat.queryUrl}")
    private String wechatQueryUrl;

    @ApiModelProperty("WECHAT系统的撤销地址")
    @Value("${custom.wechat.cancelUrl}")
    private String wechatCancelUrl;

    @ApiModelProperty("WECHAT系统的退款地址")
    @Value("${custom.wechat.wechatRefundUrl}")
    private String wechatRefundUrl;

    @ApiModelProperty("WECHAT系统的文件目录")
    @Value("${custom.wechat.fliePath}")
    private String fliePath;
}

