package com.payment.institution.entity;

import com.payment.common.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;
@Data
@Entity
@Table(name = "institution_audit")
@ApiModel(value = "机构审核表", description = "机构审核表")
public class InstitutionAudit extends BaseEntity {

    @ApiModelProperty(value = "禁用启用")
    @Column(name = "enabled")
    private Boolean enabled;

    @ApiModelProperty(value = "机构编号")
    @Column(name = "institution_code")
    private String institutionCode;

    @ApiModelProperty(value = "代理机构编号")
    @Column(name = "agency_code")
    private String agencyCode;

    @ApiModelProperty(value = "代理机构名称")
    @Column(name = "agency_name")
    private String agencyName;

    @ApiModelProperty(value = "中文名称")
    @Column(name = "cn_name")
    private String cnName;

    @ApiModelProperty(value = "英文名称")
    @Column(name = "en_name")
    private String enName;

    @ApiModelProperty(value = "机构类型")
    @Column(name = "institution_type")
    private Integer institutionType;

    @ApiModelProperty(value = "结算币种")
    @Column(name = "settle_currency")
    private String settleCurrency;

    @ApiModelProperty(value = "国家")
    @Column(name = "country")
    private String country;

    @ApiModelProperty(value = "国别类型")
    @Column(name = "country_type")
    private Integer countryType; // 0 境外 1 中国境内

    @ApiModelProperty(value = "国家编号")
    @Column(name = "country_code")
    private String countryCode;

    @ApiModelProperty(value = "浮动率")
    @Column(name = "float_rate")
    private BigDecimal floatRate;


    @ApiModelProperty(value = "法人姓名")
    @Column(name = "legal_name")
    private String legalName;

    @ApiModelProperty(value = "证件编号")
    @Column(name = "legal_passport_code")
    private String legalPassportCode;

    @ApiModelProperty(value = "证件照片")
    @Column(name = "legal_passport_img")
    private String legalPassportImg;

    @ApiModelProperty(value = "证件有效期")
    @Column(name = "legal_passport_validity")
    private String legalPassportValidity;

    @ApiModelProperty(value = "机构合同")
    @Column(name = "institution_contract")
    private String institutionContract;

    @ApiModelProperty(value = "机构地址")
    @Column(name = "institution_adress")
    private String institutionAdress;

    @ApiModelProperty(value = "机构电话")
    @Column(name = "institution_phone")
    private String institutionPhone;

    @ApiModelProperty(value = "机构邮箱")
    @Column(name = "institution_email")
    private String institutionEmail;

    @ApiModelProperty(value = "机构网站url")
    @Column(name = "institution_web_url")
    private String institutionWebUrl;

    @ApiModelProperty(value = "联系人地址")
    @Column(name = "contact_address")
    private String contactAddress;

    @ApiModelProperty(value = "联系人电话")
    @Column(name = "contact_phone")
    private String contactPhone;

    @ApiModelProperty(value = "联系人")
    @Column(name = "contact_people")
    private String contactPeople;

    @ApiModelProperty(value = "销售联系人")
    @Column(name = "sale_contact_people")
    private String saleContactPeople;

    @ApiModelProperty(value = "销售邮箱")
    @Column(name = "sale_contact_email")
    private String saleContactEmail;

    @ApiModelProperty(value = "公司章程")
    @Column(name = "company_articles")
    private String companyArticles;

    @ApiModelProperty(value = "审核状态")
    @Column(name = "audit_status")
    private Byte auditStatus = 1;//1-待审核 2-审核通过 3-审核不通过

    @ApiModelProperty(value = "生效时间")
    @Column(name = "institution_effect_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date institutionEffectTime;

    @ApiModelProperty(value = "公司注册号")
    @Column(name = "company_regist_number")
    private String companyRegistNumber;

    @ApiModelProperty(value = "公司证件有效期")//营业期限
    @Column(name = "company_validity")
    private String companyValidity;

    @ApiModelProperty(value = "行业类别")
    @Column(name = "industry_category")
    private String industryCategory;

    @ApiModelProperty(value = "经营类目")
    @Column(name = "business_category")
    private String businessCategory;

    @ApiModelProperty(value = "企业证件")
    @Column(name = "business_certificate")
    private String businessCertificate;

    @ApiModelProperty(value = "行业许可")
    @Column(name = "business_license")
    private String businessLicense;

    @ApiModelProperty(value = "直连间连限制开关")//默认是false 是否支持直连
    @Column(name = "connect_limit")
    private Boolean connectLimit;

    @ApiModelProperty(value = "机构邮政")
    @Column(name = "institution_postal_code")
    private String institutionPostalCode;

}
