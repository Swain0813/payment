package com.payment.trade.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author XuWenQi
 * @Date: 2019 6/19 16:08
 * @Description: xendit服务器回调实体
 */
@Data
@ApiModel(value = "xendit服务器回调实体", description = "xendit服务器回调实体")
public class XenditServerCallbackDTO {

    @ApiModelProperty(value = "Xendit生成的发票ID")
    private String id;

    @ApiModelProperty(value = "您的Xendit商业ID")
    private String user_id;

    @ApiModelProperty(value = "服务器中的发票ID，可用于在您和Xendit之间进行协调")
    private String external_id;

    @ApiModelProperty(value = "唯一数字应高于还是低于金额")
    private String is_high;

    @ApiModelProperty(value = "您公司或网站的名称")
    private String merchant_name;

    @ApiModelProperty(value = "发票的名义金额（不含税，费用）")
    private String amount;

    @ApiModelProperty(value = "PAID: 发票已成功支付")//EXPIRED，发票已过期。默认情况下不启用它。如果您想为您的业务启用它，请与我们联系
    private String status;

    @ApiModelProperty(value = "付款人的电子邮件，我们从您的API中获取此信息")
    private String payer_email;

    @ApiModelProperty(value = "发票说明，我们从您的API调用中获取此信息")
    private String description;

    @ApiModelProperty(value = "从此发票直接支付的Xendit费用")
    private String fees_paid_amount;

    @ApiModelProperty(value = "归属于您的金额扣除我们的费用")
    private String adjusted_received_amount;

    @ApiModelProperty(value = "POOL类型是非固定虚拟账户  CREDIT_CARD类型是信用卡  RETAIL_OUTLET类型是零售店  EWALLET类型是电子钱包")
    private String payment_method;

    @ApiModelProperty(value = "用获得的资金，银行的代码只有在付款方式是POOL")
    private String bank_code;

    @ApiModelProperty(value = "仅当付款方式为RETAIL_OUTLET时，用于收取货币的零售店代码")
    private String retail_outlet_name;

    @ApiModelProperty(value = "用于收到钱的电子钱包的代码只有付款方式为电子钱包")
    private String ewallet_type;

    @ApiModelProperty(value = "创建此发票的按需的链接")
    private String on_demand_link;

    @ApiModelProperty(value = "创建此发票的周期性的ID")
    private String recurring_payment_id;

    @ApiModelProperty(value = "发票支付的总金额")
    private String paid_amount;

    @ApiModelProperty(value = "跟踪发票更新时间的ISO时间戳")
    private String updated;

    @ApiModelProperty(value = "用于跟踪创建发票的ISO时间戳")
    private String created;

}
