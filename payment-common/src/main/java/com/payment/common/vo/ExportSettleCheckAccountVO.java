package com.payment.common.vo;

import com.payment.common.entity.SettleCheckAccountDetail;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: YangXu
 * @create: 2019-04-16 16:57
 **/
@Data
@ApiModel(value = "机构结算对账导出实体", description = "机构结算对账导出实体")
public class ExportSettleCheckAccountVO {

    @ApiModelProperty("币种")
    private String currency;

    @ApiModelProperty("机构结算对账导出实体")
    private List<SettleCheckAccountDetailVO> list;
}
