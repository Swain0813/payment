package com.payment.trade.config;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * ad3配置参数读取类
 */
@Data
@Configuration("ad3ParamsConfig")
public class AD3ParamsConfig {

    @ApiModelProperty("AD3系统的url地址")
    @Value("${custom.ad3Url}")
    private String ad3Url;

    @ApiModelProperty("AD3系统商户号")
    @Value("${custom.merchantCode}")
    private String merchantCode;

    @ApiModelProperty("AD3系统操作员id")
    @Value("${custom.operatorId}")
    private String operatorId;

    @ApiModelProperty("AD3系统imei编号")
    @Value("${custom.imei}")
    private String imei;

    @ApiModelProperty("AD3系统登录密码")
    @Value("${custom.password}")
    private String password;

    @ApiModelProperty("AD3系统交易密码")
    @Value("${custom.tradePwd}")
    private String tradePassword;

    @ApiModelProperty("AD3系统私钥")
    @Value("${custom.platformProvidesPrivateKey}")
    private String platformProvidesPrivateKey;//私钥

    @ApiModelProperty("AD3签名方式")
    @Value("${custom.merchantSignType}")
    private String merchantSignType;//签名方式

    @ApiModelProperty("AD3回调地址")
    @Value("${custom.channelCallbackUrl}")
    private String channelCallbackUrl;

    @ApiModelProperty("亚洲钱包的支付成功页面的url")
    @Value("${custom.paySuccessUrl}")
    private String paySuccessUrl;

    @ApiModelProperty("付款人名称")
    @Value("${custom.draweeName}")
    private String draweeName;

    @ApiModelProperty("付款人邮箱")
    @Value("${custom.draweeEmail}")
    private String draweeEmail;

    @ApiModelProperty("ad3ItsUrl")
    @Value("${custom.ad3ItsUrl}")
    private String ad3ItsUrl;

    @ApiModelProperty("nextPosUrl")
    @Value("${custom.nextPosUrl}")
    private String nextPosUrl;
}

