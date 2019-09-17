package com.payment.trade.vo;

import com.payment.common.entity.Orders;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@ApiModel(value = "机构关联信息输出实体", description = "机构关联信息输出实体")
public class OnlineRelevantVO {


    @Id
    @Column(name = "id")
    @ApiModelProperty(hidden = true)
    @GeneratedValue(generator = "UUID")
    public String id;
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "create_time")
    public Date createTime;

    @ApiModelProperty(value = "修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "update_time")
    private Date updateTime;

    @ApiModelProperty(value = "创建者")
    @Column(name = "creator")
    private String creator;

    @ApiModelProperty(value = "更改者")
    @Column(name = "modifier")
    private String modifier;

    @ApiModelProperty(value = "备注")
    @Column(name = "remark")
    private String remark;
    @ApiModelProperty(value = "id")
    private String orderId;

    @ApiModelProperty(value = "收银台url")
    private String url;

    @ApiModelProperty(value = "订单")
    private Orders orders;

    @ApiModelProperty(value = "机构编号")
    @Column(name = "institution_code")
    private String institutionCode;

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

    @ApiModelProperty(value = "法人护照号")
    @Column(name = "legal_passport_code")
    private String legalPassportCode;

    @ApiModelProperty(value = "法人护照照片")
    @Column(name = "legal_passport_img")
    private String legalPassportImg;

    @ApiModelProperty(value = "法人护照有效期")
    @Column(name = "legal_passport_validity")
    private String legalPassportValidity;

    @ApiModelProperty(value = "机构合同")
    @Column(name = "institution_contract")
    private String institutionContract;

    @ApiModelProperty(value = "公司章程")
    @Column(name = "company_articles")
    private String companyArticles;

    @ApiModelProperty(value = "机构地址")
    @Column(name = "institution_adress")
    private String institutionAdress;

    @ApiModelProperty(value = "机构电话")
    @Column(name = "institution_phone")
    private String institutionPhone;

    @ApiModelProperty(value = "机构邮箱")
    @Column(name = "institution_email")
    private String institutionEmail;

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

    @ApiModelProperty(value = "审核状态")
    @Column(name = "audit_status")
    private Byte auditStatus = 1;//1-待审核 2-审核通过 3-审核不通过

    @ApiModelProperty(value = "禁用启用")
    @Column(name = "enabled")
    private Boolean enabled;

    @ApiModelProperty(value = "产品")
    private List<OnlineProductVO> onlineProductList;

}
