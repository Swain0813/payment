package com.payment.trade.channels.cfp;

import com.payment.common.response.BaseResponse;
import com.payment.trade.dto.CloudFlashCallbackDTO;

/**
 * 云闪付业务类
 */
public interface CloudFlashPayService {

    /**
     * 云闪付前端回调
     *
     * @param cloudFlashCallbackDTO 云闪付回调实体
     */
    BaseResponse cloudFlashPayCallback(CloudFlashCallbackDTO cloudFlashCallbackDTO);

}
