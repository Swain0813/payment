package com.payment.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;


/**
 * @description: 插入机构实体
 * @author: YangXu
 * @create: 2019-01-25 11:25
 **/
@Data
@ApiModel(value = "插入机构实体", description = "插入机构实体")
public class InstitutionExportDTO {

    @ApiModelProperty(value = "机构id")
    private String institutionId;

    @ApiModelProperty(value = "机构Code")
    private String institutionCode;

    @ApiModelProperty(value = "中文名称")
    private String cnName;

    @ApiModelProperty(value = "英文名称")
    private String enName;

    @ApiModelProperty(value = "机构类型")
    private Integer institutionType;

    @ApiModelProperty(value = "结算币种")
    private String settleCurrency;

    @ApiModelProperty(value = "国家")//dic_6 国家从字典表中获取
    private String country;

    @ApiModelProperty(value = "语言")
    private String language;

    @ApiModelProperty(value = "国家类别")
    private Integer countryType;

    @ApiModelProperty(value = "国家编号")
    private String countryCode;

    @ApiModelProperty(value = "浮动率")
    private BigDecimal floatRate;

    @ApiModelProperty(value = "法人姓名")
    private String legalName;

    @ApiModelProperty(value = "证件编号")
    private String legalPassportCode;

    @ApiModelProperty(value = "证件照片")
    private String legalPassportImg;

    @ApiModelProperty(value = "证件有效期")
    private String legalPassportValidity;

    @ApiModelProperty(value = "机构合同")
    private String institutionContract;

    @ApiModelProperty(value = "机构地址")
    private String institutionAdress;

    @ApiModelProperty(value = "机构电话")
    private String institutionPhone;

    @ApiModelProperty(value = "机构邮箱")
    private String institutionEmail;

    @ApiModelProperty(value = "机构网站url")
    private String institutionWebUrl;

    @ApiModelProperty(value = "销售邮箱")
    private String saleContactEmail;

    @ApiModelProperty(value = "联系人地址")
    private String contactAddress;

    @ApiModelProperty(value = "联系人电话")
    private String contactPhone;

    @ApiModelProperty(value = "联系人")
    private String contactPeople;

    @ApiModelProperty(value = "销售联系人")
    private String saleContactPeople;

    @ApiModelProperty(value = "公司章程")
    private String companyArticles;

    @ApiModelProperty(value = "密码")
    private String password;

    @ApiModelProperty(value = "起始时间")
    private String startTime;

    @ApiModelProperty(value = "结束时间")
    private String endTime;

    @ApiModelProperty(value = "状态")
    private Boolean enabled;

    @ApiModelProperty(value = "审核状态")
    private Integer auditStatus;

    @ApiModelProperty(value = "生效时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date institutionEffectTime;

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
    private Boolean connectLimit = false;

}
