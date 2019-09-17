package com.payment.common.vo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;


@Data
@ApiModel(value = "机构交易对账导出实体", description = "机构交易对账导出实体")
public class ExportTradeAccountEnVO {

    @ApiModelProperty("机构交易对账总表")
    private List<TradeCheckAccountEnVO> tradeCheckAccounts;

    @ApiModelProperty("机构交易对账详细表")
    private List<TradeCheckAccountDetailEnVO> tradeCheckAccountDetails;
}
