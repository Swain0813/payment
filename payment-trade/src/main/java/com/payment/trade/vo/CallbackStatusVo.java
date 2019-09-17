package com.payment.trade.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author shenxinran
 * @Date: 2019/4/18 17:51
 * @Description: 商户回调时的参数
 */
@Data
@ApiModel(value = "商户回调时的参数", description = "商户回调时的参数")
public class CallbackStatusVo {

    @ApiModelProperty(value = "响应状态")
    private String status;//success为成功

    public CallbackStatusVo(String status) {
        this.status = status;
    }
}
