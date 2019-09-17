package com.payment.trade.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * pos机机构线下机构产品相关信息
 */
@Data
@ApiModel(value = "机构线下查询机构产品相关信息实体", description = "机构线下查询机构产品相关信息实体")
public class OfflineRelevantInfoVO {

    @ApiModelProperty(value = "AW支持的所有币种")
    private List<CurrencyVO> currencys;

    @ApiModelProperty(value = "线下机构产品相关信息")
    private List<OfflineProductVO> offlineProductVOS;

}
