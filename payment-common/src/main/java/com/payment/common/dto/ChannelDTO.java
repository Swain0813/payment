package com.payment.common.dto;
import com.payment.common.base.BasePageHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @description:
 * @author: YangXu
 * @create: 2019-01-30 14:27
 **/
@Data
@ApiModel(value = "插入通道实体", description = "插入通道实体")
public class ChannelDTO extends BasePageHelper {


    @ApiModelProperty(value = "通道ID")
    private String channelId;

    @ApiModelProperty(value = "产品ID")
    private List<String> productId;

    @ApiModelProperty(value = "ad3的支付code")
    private String payCode;

    @ApiModelProperty(value = "通道编号")
    private String channelCode;

    @ApiModelProperty(value = "通道英文名称")//通道的服务名称比如AD3_ONLINE,AD3_OFFLINE
    private String channelEnName;

    @ApiModelProperty(value = "通道中文名称")
    private String channelCnName;

    @ApiModelProperty(value = "通道图片")
    private String channelImg;

    @ApiModelProperty(value = "国家")
    private String country;

    @ApiModelProperty(value = "国家类别")
    private Byte countryType;

    @ApiModelProperty(value = "交易类型")
    private Byte transType;

    @ApiModelProperty(value = "支付方式")
    private String payType;

    @ApiModelProperty(value = "币种")
    private String currency;

    @ApiModelProperty(value = "银行机构号")
    private String issuerId;

    @ApiModelProperty(value = "结算周期")
    private String settleCycle;

    @ApiModelProperty(value = "通道url")
    private String channelUrl;

    @ApiModelProperty(value = "通道最小限额")
    private String limitMinAmount;

    @ApiModelProperty(value = "通道最大限额")
    private String limitMaxAmount;

    @ApiModelProperty(value = "启用禁用")
    private Boolean enabled;

    @ApiModelProperty(value = "是否支持退款")
    private Boolean supportRefundState;

    @ApiModelProperty(value = "是否直连")
    private Byte channelConnectMethod;

    @ApiModelProperty(value = "Swift Code")//Swift Code
    private String refundUrl;

    @ApiModelProperty(value = "通道单个查询url")
    private String channelSingleSelectUrl;

    @ApiModelProperty(value = "通道批量查询url")
    private String channelBatchSelectUrl;

    @ApiModelProperty(value = "语言")
    private String language;

    @ApiModelProperty(value = "通道费率")
    private BigDecimal channelRate;

    @ApiModelProperty(value = "通道费率最小值")
    private BigDecimal channelMinRate;

    @ApiModelProperty(value = "通道费率最大值")
    private BigDecimal channelMaxRate;

    @ApiModelProperty(value = "通道手续费类型")
    private String channelFeeType;

    @ApiModelProperty(value = "通道网关费率")
    private BigDecimal channelGatewayRate;

    @ApiModelProperty(value = "通道网关费率最小值")
    private BigDecimal channelGatewayMinRate;

    @ApiModelProperty(value = "通道网关费率最大值")
    private BigDecimal channelGatewayMaxRate;

    @ApiModelProperty(value = "通道网关手续费类型")
    private String channelGatewayFeeType;//1-单笔费率,2-单笔定额

    @ApiModelProperty(value = "通道网关是否收取")
    private Byte channelGatewayCharge;//1-收 2-不收

    @ApiModelProperty(value = "通道网关收取状态")
    private Byte channelGatewayStatus;// 1-成功时收取 2-失败时收取 3-全收

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "通道商户号")
    private String channelMerchantId ;

    @ApiModelProperty(value = "通道加密MD5key")
    private String md5KeyStr ;

    @ApiModelProperty(value = "银行id")
    private  List<String> bankID ;

    @ApiModelProperty(value = "优先级")
    private String sort;
}
