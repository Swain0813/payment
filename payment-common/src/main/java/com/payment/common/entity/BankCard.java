package com.payment.common.entity;

import com.payment.common.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "bank_card")
@ApiModel(value = "银行卡", description = "银行卡")
public class BankCard extends BaseEntity{

    @ApiModelProperty(value = "机构code")
    @Column(name = "institution_id")
    private String institutionId;

    @ApiModelProperty(value = "账户编号")
    @Column(name = "account_code")
    private String accountCode;

    @ApiModelProperty(value = "开户账号")
    @Column(name = "bank_account_code")
    private String bankAccountCode;

    @ApiModelProperty(value = "开户名称")
    @Column(name = "account_name")
    private String accountName;

    @ApiModelProperty(value = "开户行名称")
    @Column(name = "bank_name")
    private String bankName;

    @ApiModelProperty(value = "swiftCode")
    @Column(name = "swift_code")
    private String swiftCode;

    @ApiModelProperty(value = "bankCode")
    @Column(name = "bank_code")
    private String bankCode;

    @ApiModelProperty(value = "开户行地址")
    @Column(name = "bank_address")
    private String bankAddress;

    @ApiModelProperty(value = "性质 1-对公 2对私")
    @Column(name = "nature")
    private Byte nature;

    @ApiModelProperty(value = "收款人地区国家")
    @Column(name = "receiver_country")
    private String receiverCountry;

    @ApiModelProperty(value = "收款人地址")
    @Column(name = "receiver_address")
    private String receiverAddress;

    @ApiModelProperty(value = "收款人")
    @Column(name = "receiver")
    private String receiver;

    @ApiModelProperty(value = "iban")
    @Column(name = "iban")
    private String iban;

    @ApiModelProperty(value = "结算币种")
    @Column(name = "bank_currency")
    private String bankCurrency;

    @ApiModelProperty(value = "银行卡币种")
    @Column(name = "bankcode_currency")
    private String bankCodeCurrency;

    @ApiModelProperty(value = "禁用启用")
    @Column(name = "enabled")
    private Boolean enabled;

    @ApiModelProperty(value = "中间行银行编码")
    @Column(name = "intermediary_bank_code")
    private String intermediaryBankCode;

    @ApiModelProperty(value = "中间行银行名称")
    @Column(name = "intermediary_bank_name")
    private String intermediaryBankName;

    @ApiModelProperty(value = "中间行银行地址")
    @Column(name = "intermediary_bank_address")
    private String intermediaryBankAddress;

    @ApiModelProperty(value = "中间行银行账户")
    @Column(name = "intermediary_bank_account_no")
    private String intermediaryBankAccountNo;

    @ApiModelProperty(value = "中间行银行城市")
    @Column(name = "intermediary_bank_country")
    private String intermediaryBankCountry;

    @ApiModelProperty(value = "中间行银行其他code")
    @Column(name = "intermediary_other_code")
    private String intermediaryOtherCode;

    @ApiModelProperty(value = "是否设为默认银行卡")
    @Column(name = "default_flag")
    private Boolean defaultFlag;


}
