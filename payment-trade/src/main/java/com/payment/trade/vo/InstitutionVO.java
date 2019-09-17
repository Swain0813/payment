package com.payment.trade.vo;

import com.payment.common.entity.Orders;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@ApiModel(value = "机构关联信息输出实体", description = "机构关联信息输出实体")
public class InstitutionVO{

    @ApiModelProperty(value = "id")
    private String id;

    @ApiModelProperty(value = "订单id")
    private String orderId;

    @ApiModelProperty(value = "创建时间")
    public Date createTime;

    @ApiModelProperty(value = "修改时间")
    private Date updateTime;

    @ApiModelProperty(value = "创建者")
    private String creator;

    @ApiModelProperty(value = "更改者")
    private String modifier;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "机构编号")
    private String institutionCode;

    @ApiModelProperty(value = "代理机构编号")
    private String agencyCode;

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

    @ApiModelProperty(value = "国家编号")
    private String countryCode;

    @ApiModelProperty(value = "浮动率")
    private BigDecimal floatRate;

    @ApiModelProperty(value = "法人护照号")
    private String legalPassportCode;

    @ApiModelProperty(value = "法人护照照片")
    private String legalPassportImg;

    @ApiModelProperty(value = "法人护照有效期")
    private String legalPassportValidity;

    @ApiModelProperty(value = "机构合同")
    private String institutionContract;

    @ApiModelProperty(value = "公司章程")
    private String companyArticles;

    @ApiModelProperty(value = "机构地址")
    private String institutionAdress;

    @ApiModelProperty(value = "机构电话")
    private String institutionPhone;

    @ApiModelProperty(value = "机构邮箱")
    private String institutionEmail;

    @ApiModelProperty(value = "联系人地址")
    private String contactAddress;

    @ApiModelProperty(value = "机构网站url")
    private String institutionWebUrl;

    @ApiModelProperty(value = "联系人电话")
    private String contactPhone;

    @ApiModelProperty(value = "联系人")
    private String contactPeople;

    @ApiModelProperty(value = "销售联系人")
    private String saleContactPeople;

    @ApiModelProperty(value = "销售邮箱")
    private String saleContactEmail;

    @ApiModelProperty(value = "审核状态")
    private Byte auditStatus = 1;//1-待审核 2-审核通过 3-审核不通过

    @ApiModelProperty(value = "禁用启用")
    private Boolean enabled;

    @ApiModelProperty(value = "直连间连限制开关")//默认是false 是否支持直连
    private Boolean connectLimit;

    @ApiModelProperty(value = "订单信息")
    private Orders orders;

    @ApiModelProperty(value = "产品")
    private List<ProductVO> productList;
}
