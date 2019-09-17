package com.payment.trade.vo;
import com.payment.common.entity.Channel;
import com.payment.common.entity.Institution;
import com.payment.common.entity.InstitutionProduct;
import com.payment.common.entity.Product;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "基础信息输出实体", description = "基础信息输出实体")
public class BasicsInfoVO {

    @ApiModelProperty("机构")
    private Institution institution;

    @ApiModelProperty("产品")
    private Product product;

    @ApiModelProperty("机构产品")
    private InstitutionProduct institutionProduct;

    @ApiModelProperty("通道")
    private Channel channel;

    @ApiModelProperty("银行名称")
    private String bankName;
}
