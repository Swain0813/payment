package com.payment.channels.service;

import com.payment.common.dto.vtc.VTCRequestDTO;
import com.payment.common.response.BaseResponse;

public interface VTCService {

    /**
     * vtcPay收单接口
     * @param vtcRequestDTO
     * @return
     */
    BaseResponse vtcPay(VTCRequestDTO vtcRequestDTO);
}
