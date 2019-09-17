package com.payment.common.dto.enets;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @description: Enets线下扫码实体
 * @author: YangXu
 * @create: 2019-06-13 13:45
 **/
@Data
@ApiModel(value = "enets线下扫码请求实体", description = "enets线下扫码请求实体")
public class EnetsOffLineRequestDTO {

    private EnetsSMRequestDTO requestJsonDate;

    private String institutionOrderId;

    private String apiSecret;

    private String apiKeyId;

    private String reqIp;

    private String orderId;



    public EnetsOffLineRequestDTO() {
    }

    public EnetsOffLineRequestDTO(EnetsSMRequestDTO enetsSMRequestDTO,String institutionOrderId,String reqIp,String orderId) {
        this.requestJsonDate = enetsSMRequestDTO;
        this.apiSecret  = "21296dd3-5bf6-4dfc-b8a2-03fbcc213b7b";
        this.apiKeyId = "b027dacd-1c13-4916-8b93-38fae6be2f80";
        this.institutionOrderId = institutionOrderId;
        this.reqIp = reqIp;
        this.orderId = orderId;
    }
}
