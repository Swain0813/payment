package com.payment.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 机构对账表模块导出输入参数
 */
@Data
@ApiModel(value = "机构对账表模块导出输入参数", description = "机构对账表模块导出输入参数")
public class TradeCheckAccountExportDTO {

    @NotNull(message = "50002")
    @ApiModelProperty(value = "机构编号")
    private String institutionCode;

    @ApiModelProperty(value = "开始时间")
    private String startDate;

    @ApiModelProperty(value = "结束时间")
    private String endDate;

    @ApiModelProperty(value = "对账时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date checkDate;

    @ApiModelProperty(value = "币种")
    private String currency;

    @ApiModelProperty(value = "语言")
    private String language;
}
