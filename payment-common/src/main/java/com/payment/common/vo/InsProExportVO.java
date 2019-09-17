package com.payment.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @description: 机构产品导出实体
 * @author: YangXu
 * @create: 2019-03-06 16:58
 **/
@Data
@ApiModel(value = "机构产品导出实体", description = "机构产品导出实体")
public class InsProExportVO {
    @ApiModelProperty(value = "机构号")
    private String institutionCode;

    @ApiModelProperty(value = "机构名称")
    private String institutionName;

    @ApiModelProperty(value = "交易类型")
    private Byte transType;

    @ApiModelProperty(value = "产品")
    private String payType;

    @ApiModelProperty(value = "结算周期")
    private String settleCycle;

    @ApiModelProperty(value = "费率类型")
    private String rateType; // 单笔费率 单笔定额

    @ApiModelProperty(value = "费率")
    private BigDecimal rate;

    @ApiModelProperty(value = "分润比例")
    private BigDecimal dividedRatio;

    //8月27日注释
  /*  @ApiModelProperty(value = "分润模式")
    private Byte dividedMode;*/

    @ApiModelProperty(value = "费率最小值")
    private BigDecimal minRate;

    @ApiModelProperty(value = "费率最大值")
    private BigDecimal maxRate;

    @ApiModelProperty(value = "附加值")
    private BigDecimal addValue;

    @ApiModelProperty(value = "启用禁用")
    private Boolean enabled;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createTime;

    @ApiModelProperty(value = "修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;


}
