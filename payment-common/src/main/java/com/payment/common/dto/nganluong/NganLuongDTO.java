package com.payment.common.dto.nganluong;

/**
 * @description:
 * @author: YangXu
 * @create: 2019-06-18 11:41
 **/

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @description: NGANLUONG通道请求实体
 * @author: YangXu
 * @create: 2019-06-18 10:26
 **/
@Data
@ApiModel(value = "c", description = "NGANLUONG通道请求实体")
public class NganLuongDTO {

    @ApiModelProperty(value = "NGANLUONG通道请求实体")
    private NganLuongRequestDTO nganLuongRequestDTO;

    @ApiModelProperty(value = "机构订单号")
    private String institutionOrderId;

    @ApiModelProperty(value = "reqIp")
    private String reqIp;

    public NganLuongDTO() {
    }

    public NganLuongDTO(NganLuongRequestDTO nganLuongRequestDTO, String institutionOrderId, String reqIp) {
        this.nganLuongRequestDTO = nganLuongRequestDTO;
        this.institutionOrderId = institutionOrderId;
        this.reqIp = reqIp;
    }
}
