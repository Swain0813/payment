package com.payment.common.dto;

import com.payment.common.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @author shenxinran
 * @Date: 2019/6/13 15:07
 * @Description: 资金冻结输入实体
 */

@Data
@ApiModel(value = "资金冻结输入实体", description = "资金冻结输入实体")
public class FreezingFundsDTO extends BaseEntity {

    @NotNull(message = "50002")
    @ApiModelProperty(value = "机构code")
    private String institutionId;

    @NotNull(message = "50002")
    @ApiModelProperty(value = "账户币种")
    private String currency;

    @NotNull(message = "50002")
    @ApiModelProperty(value = "冻结类型 1-资金冻结 2-预约冻结")
    private Byte frozenType;

    @NotNull(message = "50002")
    @ApiModelProperty(value = "冻结金额")
    private BigDecimal amount;

    @NotNull(message = "50002")
    @ApiModelProperty(value = "入账账户类型 1-冻结户 2-保证金账户 ")
    private Byte accountType;

    @NotNull(message = "50002")
    @ApiModelProperty(value = "原因")
    private String remark;

}
