package com.payment.common.vo;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author: XuWenQi
 * @create: 2019-01-25 15:53
 **/

@Data
@ApiModel(value = "汇率查询输出实体", description = "汇率查询输出实体")
public class ExchangeRateVO {

    @ApiModelProperty(value = "id")
    private String id;

    @ApiModelProperty(value = "买入汇率")
    private BigDecimal buyRate;

    @ApiModelProperty(value = "卖出汇率")
    private BigDecimal saleRate;

    @ApiModelProperty(value = "启用禁用")
    private Boolean enabled;

    @ApiModelProperty(value = "本位币种")
    private String localCurrency;

    @ApiModelProperty(value = "目标币种")
    private String foreignCurrency;

    @ApiModelProperty(value = "创建人")
    private String creator;

    @ApiModelProperty(value = "更新者")
    private String modifier;

    @ApiModelProperty(value = "发布时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date usingTime;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @ApiModelProperty(value = "失效时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date overdueTime;
}
