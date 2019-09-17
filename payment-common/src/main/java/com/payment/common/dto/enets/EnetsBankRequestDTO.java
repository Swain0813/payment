package com.payment.common.dto.enets;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author: XuWenQi
 * @create: 2019-06-03 10:30
 **/

@Data
@ApiModel(value = "enets网银通道请求实体", description = "enets网银通道请求实体")
public class EnetsBankRequestDTO {

    @ApiModelProperty(value = "keyId")
    private String keyId;

    @ApiModelProperty(value = "签名")
    private String hmac;

    @ApiModelProperty(value = "请求实体")
    private String txnReq;

    //以下不是上报通道参数
    @ApiModelProperty(value = "MD5KeyStr")
    private String md5KeyStr;

    @ApiModelProperty(value = "机构订单号")
    private String institutionOrderId;

    @ApiModelProperty(value = "支付url")
    private String payUrl;

    @ApiModelProperty(value = "跳转url")
    private String jumpUrl;

    public EnetsBankRequestDTO() {
    }

    public EnetsBankRequestDTO(String txnReq, String md5KeyStr, String institutionOrderId, String sign) {
        this.keyId = "524b1a17-3dfd-431b-b4cf-c20d86e07366";
        this.hmac = sign;
        this.txnReq = txnReq;
        this.md5KeyStr = md5KeyStr;
        this.institutionOrderId = institutionOrderId;
    }

}
