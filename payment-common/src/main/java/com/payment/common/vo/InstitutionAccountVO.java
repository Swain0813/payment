package com.payment.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @description: 机构详细信息详情
 * @author: YangXu
 * @create: 2019-03-01 15:38
 **/
@Data
public class InstitutionAccountVO {

    @ApiModelProperty(value = "id")
    public String id;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createTime;

    @ApiModelProperty(value = "修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @ApiModelProperty(value = "创建者")
    private String creator;

    @ApiModelProperty(value = "更改者")
    private String modifier;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "禁用启用")
    private Boolean enabled;

    @ApiModelProperty(value = "机构编号")
    private String institutionCode;

    @ApiModelProperty(value = "中文名称")
    private String cnName;

    @ApiModelProperty(value = "英文名称")
    private String enName;

    @ApiModelProperty(value = "机构类型")
    private Integer institutionType;

    @ApiModelProperty(value = "结算币种")
    private String settleCurrency;

    @ApiModelProperty(value = "国家")
    private String country;

    @ApiModelProperty(value = "国别类型")
    private Integer countryType; // 0 境外 1 中国境内

    @ApiModelProperty(value = "国家编号")//国家从数据字典里获取
    private String countryCode;

    @ApiModelProperty(value = "浮动率")
    private BigDecimal floatRate;

    @ApiModelProperty(value = "法人姓名")
    private String legalName;

    @ApiModelProperty(value = "法人护照号")
    private String legalPassportCode;

    @ApiModelProperty(value = "法人护照照片")
    private String legalPassportImg;

    @ApiModelProperty(value = "法人护照有效期")
    private String legalPassportValidity;

    @ApiModelProperty(value = "机构合同")
    private String institutionContract;

    @ApiModelProperty(value = "公司章程")
    @Column(name = "company_articles")
    private String companyArticles;

    @ApiModelProperty(value = "机构地址")
    private String institutionAdress;

    @ApiModelProperty(value = "机构电话")
    private String institutionPhone;

    @ApiModelProperty(value = "机构邮箱")
    private String institutionEmail;

    @ApiModelProperty(value = "机构网站url")
    private String institutionWebUrl;

    @ApiModelProperty(value = "联系人地址")
    private String contactAddress;

    @ApiModelProperty(value = "联系人电话")
    private String contactPhone;

    @ApiModelProperty(value = "联系人")
    private String contactPeople;

    @ApiModelProperty(value = "销售联系人")
    private String saleContactPeople;

    @ApiModelProperty(value = "销售邮箱")
    private String saleContactEmail;

    @ApiModelProperty(value = "审核状态")
    private Byte auditStatus;//1-待审核 2-审核通过 3-审核不通过

    @ApiModelProperty(value = "公司注册号")
    private String companyRegistNumber;

    @ApiModelProperty(value = "公司证件有效期")//营业期限
    private String companyValidity;

    @ApiModelProperty(value = "行业类别")
    private String industryCategory;

    @ApiModelProperty(value = "经营类目")
    private String businessCategory;

    @ApiModelProperty(value = "企业证件")
    private String businessCertificate;

    @ApiModelProperty(value = "行业许可")
    private String businessLicense;

    @ApiModelProperty(value = "直连间连限制开关")//默认是false 是否支持直连
    private Boolean connectLimit;

    @ApiModelProperty(value = "机构邮政")
    private String institutionPostalCode;

    @ApiModelProperty(value = "代理机构编号")
    private String agencyCode;

    @ApiModelProperty(value = "账户币种集合")
    private List<AccountCurrencyVO> accounts;


}
