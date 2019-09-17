package com.payment.channels.service;

import com.payment.common.dto.eghl.EGHLRequestDTO;
import com.payment.common.response.BaseResponse;

public interface EghlService {

    /**
     * @Author YangXu
     * @Date 2019/5/28
     * @Descripate eghl收单接口
     * @return
     **/
    BaseResponse eGHLPay(EGHLRequestDTO eghlRequestDTO);
}
