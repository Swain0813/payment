package com.payment.trade.vo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@ApiModel(value = "产品输出实体", description = "产品输出实体")
public class ProductVO {

    @ApiModelProperty(value = "产品id")
    private String productId;

    @ApiModelProperty(value = "产品编号")
    private Integer productCode;

    @ApiModelProperty(value = "产品币种")
    private String productCurrency;

    @ApiModelProperty(value = "产品币种图标")
    private String productCurrencyIcon;

    @ApiModelProperty(value = "支付方式")
    private String payType;

    @ApiModelProperty(value = "产品logo")
    private String productImg;

    @ApiModelProperty(value = "费率")
    private BigDecimal rate;

    @ApiModelProperty(value = "费率类型")
    private String rateType;

    @ApiModelProperty(value = "单笔限额")
    private BigDecimal limitAmount;

    @ApiModelProperty(value = "日交易总笔数限额")
    private Integer dailyTradingCount;

    @ApiModelProperty(value = "日交易总金额限额")
    private BigDecimal dailyTotalAmount;

    @ApiModelProperty(value = "浮动率")
    private BigDecimal ipFloatRate;

    @ApiModelProperty(value = "附加值")
    private BigDecimal ipAddValue;

    @ApiModelProperty(value = "审核限额状态")
    private Byte auditLimitStatus;

    @ApiModelProperty(value = "结算周期")
    private String settleCycle;

    @ApiModelProperty(value = "手续费付款方")
    private Byte feePayer;

    @ApiModelProperty(value = "通道")
    private List<ChannelVO> channelList;
}
