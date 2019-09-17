package com.payment.common.dto;
import com.payment.common.base.BasePageHelper;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @description: 查询对账实体
 * @author: YangXu
 * @create: 2019-04-02 10:31
 **/
@Data
public class SearchAccountCheckDTO extends BasePageHelper{

    @ApiModelProperty(value = "开始时间")
    private String startTime;

    @ApiModelProperty(value = "结束时间")
    private String endTime;

    @ApiModelProperty(value = "渠道编号")
    private String channelCode;

    @ApiModelProperty(value = "产品编号")
    private Integer productCode;

    @ApiModelProperty(value = "系统订单号")
    private String orderId;

    @ApiModelProperty(value = "通道订单号")
    private String channelNumber;

    @ApiModelProperty(value = "机构编号")
    private String institutionCode;

    @ApiModelProperty(value = "对账状态")
    private int errorType;
}
