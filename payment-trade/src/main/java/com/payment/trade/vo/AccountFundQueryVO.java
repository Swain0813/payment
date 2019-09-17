package com.payment.trade.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author XuWenQi
 * @Date: 2019/3/7 15:35
 * @Description: 账户资金查询接口输出实体
 */
@Data
@ApiModel(value = "账户资金查询接口输出实体", description = "账户资金查询接口输出实体")
public class AccountFundQueryVO {

    @ApiModelProperty(value = "返回结果")//T000表示成功，其余见响应码列表
    private String respCode;

    @ApiModelProperty(value = "返回结果描述")//success,成功或者错误提示
    private String respMsg;

    @ApiModelProperty(value = "版本号")//固定v1.0
    private String version;

    @ApiModelProperty(value = "字符集")//1.utf-8 2.gbk
    private String inputCharset;

    @ApiModelProperty(value = "语言")//1中文 2英文
    private String language;

    @ApiModelProperty(value = "商户号")
    private String merchantId;

    @ApiModelProperty(value = "订单币种")//3位国际 ISO编码
    private String currency;

    @ApiModelProperty(value = "虚拟账户编号")
    private String vaccountNo;

    @ApiModelProperty(value = "清算账户资金")
    private String clear_balance;

    @ApiModelProperty(value = "冻结资金")
    private String frozenBalance;

    @ApiModelProperty(value = "结算资金")
    private String settle_balance;

    @ApiModelProperty(value = "账户状态")
    private String enabled;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "数据版本号")
    private String dateVersion;

    @ApiModelProperty(value = "签名字符串")
    private String signMsg;
}
