package com.payment.common.vo;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.persistence.Column;
import java.util.Date;


/**
 * @description: 银行卡
 * @author: YangXu
 * @create: 2019-02-27 10:14
 **/
@Data
@ApiModel(value = "银行卡实体", description = "银行卡实体")
public class BankCardVO {

    @ApiModelProperty(value = "id")
    private String bankCardId;

    @ApiModelProperty(value = "机构id")
    private String institutionId;

    @ApiModelProperty(value = "机构名称")
    private String institutionName;

    @ApiModelProperty(value = "机构Code")
    private String institutionCode;

    @ApiModelProperty(value = "账户编号")
    private String accountCode;

    @ApiModelProperty(value = "开户账号")
    private String bankAccountCode;

    @ApiModelProperty(value = "开户名称")
    private String accountName;

    @ApiModelProperty(value = "开户行名称")
    private String bankName;

    @ApiModelProperty(value = "swiftCode")
    private String swiftCode;

    @ApiModelProperty(value = "结算币种")
    private String bankCurrency;

    @ApiModelProperty(value = "银行卡币种")
    private String bankCodeCurrency;

    @ApiModelProperty(value = "bankCode")
    private String bankCode;

    @ApiModelProperty(value = "开户行地址")
    private String bankAddress;

    @ApiModelProperty(value = "性质 1-对公 2对私")
    private Byte nature;

    @ApiModelProperty(value = "收款人地区国家")
    private Byte receiverCountry;

    @ApiModelProperty(value = "收款人地址")
    private String receiverAddress;

    @ApiModelProperty(value = "收款人")
    private String receiver;

    @ApiModelProperty(value = "iban")
    private String iban;

    @ApiModelProperty(value = "中间行银行编码")
    private String intermediaryBankCode;

    @ApiModelProperty(value = "中间行银行名称")
    private String intermediaryBankName;

    @ApiModelProperty(value = "中间行银行地址")
    private String intermediaryBankAddress;

    @ApiModelProperty(value = "中间行银行账户")
    private String intermediaryBankAccountNo;

    @ApiModelProperty(value = "中间行银行城市")
    private String intermediaryBankCountry;

    @ApiModelProperty(value = "中间行银行其他code")
    private String intermediaryOtherCode;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "修改时间")
    private Date updateTime;

    @ApiModelProperty(value = "创建者")
    private String creator;

    @ApiModelProperty(value = "修改者")
    private String modifier;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "启用禁用")
    private boolean enabled;

    @ApiModelProperty(value = "是否设为默认银行卡")
    private Boolean defaultFlag;



}
