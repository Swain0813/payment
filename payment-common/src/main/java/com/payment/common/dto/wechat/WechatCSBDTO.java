package com.payment.common.dto.wechat;

import com.payment.common.entity.Channel;
import com.payment.common.entity.Orders;
import com.payment.common.utils.DateToolUtils;
import com.payment.common.utils.UUIDHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @description: 微信CSB请求实体
 * @author: XuWenQi
 * @create: 2019-06-25 10:11
 **/
@Data
@ApiModel(value = "微信CSB请求实体", description = "微信CSB请求实体")
public class WechatCSBDTO {

    @ApiModelProperty(value = "公众号id")
    private String appid;

    @ApiModelProperty(value = "随机字符串")
    private String nonce_str;

    @ApiModelProperty(value = "商户号")
    private String mch_id;

    @ApiModelProperty(value = "子商户号")
    private String sub_mch_id;

    @ApiModelProperty(value = "签名类型")
    private String sign_type;

    @ApiModelProperty(value = "商品描述")
    private String body;

    @ApiModelProperty(value = "商户订单号")
    private String out_trade_no;

    @ApiModelProperty(value = "标价币种")
    private String fee_type;

    @ApiModelProperty(value = "标价金额")
    private String total_fee;

    @ApiModelProperty(value = "终端IP")
    private String spbill_create_ip;

    @ApiModelProperty(value = "获取交易过期时间")
    private String time_expire;

    @ApiModelProperty(value = "通知地址")
    private String notify_url;

    @ApiModelProperty(value = "交易类型")
    private String trade_type;

    @ApiModelProperty(value = "版本号")
    private String version;

    @ApiModelProperty(value = "新增mcc")
    private String detail;

    //以下不是上报通道的参数
    @ApiModelProperty(value = "机构订单号")
    private String institutionOrderId;

    @ApiModelProperty(value = "md5Key")
    private String md5KeyStr;

    @ApiModelProperty(value = "ip")
    private String reqIp;


    public WechatCSBDTO() {
    }

    public WechatCSBDTO(Orders orders, Channel channel, String notify_url) {
        this.appid = "wx14e049b9320bccca";
        this.nonce_str = UUIDHelper.getRandomString(32);
        this.mch_id = channel.getChannelMerchantId();
        this.sub_mch_id = "66104046";
        this.sign_type = "MD5";
        this.body = StringUtils.isEmpty(orders.getCommodityName()) ? "商品" : orders.getCommodityName();//产品名称
        this.out_trade_no = orders.getId();
        this.fee_type = orders.getTradeCurrency();
        String amt = String.valueOf(orders.getTradeAmount());
        int total_fee = 0;
        if (!StringUtils.isEmpty(amt)) {
            Double amt_d = new Double(amt);
            total_fee = BigDecimal.valueOf(amt_d).multiply(new BigDecimal(100)).intValue();
        }
        this.total_fee = Integer.toString(total_fee);
        //获取5分钟后的日期
        Date expireTime = DateToolUtils.getBeforeOrAfterDateByMinNumber(new Date(), 5);
        this.time_expire = DateToolUtils.formatDate(expireTime, "yyyyMMddHHmmss");
        this.notify_url = notify_url;
        this.trade_type = "NATIVE";
        this.spbill_create_ip = "8.8.8.8";
        this.version = "1.0";
        String mccCode = "";
        this.detail = "{\"goods_detail\":[{\"wxpay_goods_id\":\"" + mccCode + "\"}]}";
        this.institutionOrderId = orders.getInstitutionOrderId();
        this.md5KeyStr = channel.getMd5KeyStr();
        this.reqIp = orders.getReqIp();
    }
}
