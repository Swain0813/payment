package com.payment.common.dto.alipay;

import com.payment.common.entity.Channel;
import com.payment.common.entity.Orders;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;

/**
 * @description: 支付宝线下BSC请求实体
 * @author: XuWenQi
 * @create: 2019-06-14 14:51
 **/
@Data
@ApiModel(value = "支付宝线下BSC请求实体", description = "支付宝线下BSC请求实体")
public class AliPayOfflineBSCDTO {

    @ApiModelProperty(value = "支付接口名称")
    private String service;

    @ApiModelProperty(value = "渠道商户号")
    private String partner;

    @ApiModelProperty(value = "编码格式")
    private String _input_charset;

    @ApiModelProperty(value = "渠道商户号 Same value with partner ID")
    private String alipay_seller_id;

    @ApiModelProperty(value = "产品名称")
    private String trans_name;

    @ApiModelProperty(value = "商户订单号")
    private String partner_trans_id;

    @ApiModelProperty(value = "交易币种")
    private String currency;

    @ApiModelProperty(value = "交易金额")
    private String trans_amount;

    @ApiModelProperty(value = "支付宝条码")
    private String buyer_identity_code;

    @ApiModelProperty(value = "barcode表示条码 qrcode表示二维码")
    private String identity_code_type;

    @ApiModelProperty(value = "固定值OVERSEAS_MBARCODE_PAY")
    private String biz_product;

    @ApiModelProperty(value = "扩展参数")
    private String extend_info;

    //以下不是上报通道的参数
    @ApiModelProperty(value = "机构订单号")
    private String institutionOrderId;

    @ApiModelProperty(value = "md5Key")
    private String md5KeyStr;

    @ApiModelProperty(value = "请求IP")
    private String reqIp;


    public AliPayOfflineBSCDTO() {
    }

    public AliPayOfflineBSCDTO(Orders orders, Channel channel, String buyer_identity_code) {
        JSONObject extendJson = new JSONObject();
        extendJson.put("secondary_merchant_name", "alldebit01");
        extendJson.put("secondary_merchant_id", "001");
        extendJson.put("secondary_merchant_industry", "0742");
        extendJson.put("store_id", "zh001");
        extendJson.put("store_name", "zhstore");
        this.extend_info = extendJson.toString();
        this.service = "alipay.acquire.overseas.spot.pay";
        this.partner = "2088421920790891";
        this._input_charset = "UTF-8";
        this.alipay_seller_id = "2088421920790891";
        this.trans_name = StringUtils.isEmpty(orders.getCommodityName()) ? "SALE" : orders.getCommodityName();//产品名称
        this.partner_trans_id = orders.getId();//订单号
        this.currency = orders.getTradeCurrency();//币种
        this.trans_amount = String.valueOf(orders.getTradeAmount());
        this.buyer_identity_code = buyer_identity_code;//支付宝条码
        this.identity_code_type = "barcode";
        this.biz_product = "OVERSEAS_MBARCODE_PAY";
        this.institutionOrderId = orders.getInstitutionOrderId();
        this.md5KeyStr = channel.getMd5KeyStr();
        this.reqIp = orders.getReqIp();
    }
}
