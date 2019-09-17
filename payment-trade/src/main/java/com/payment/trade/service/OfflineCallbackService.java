package com.payment.trade.service;

import com.payment.trade.dto.AD3OfflineCallbackDTO;


/**
 * @author XuWenQi
 * @Date: 2019/3/28 15:43
 * @Description: 线下回调业务接口
 */
public interface OfflineCallbackService {

    /**
     * ad3线下回调
     *
     * @param ad3OfflineCallbackDTO ad3线下回调输入实体
     * @return
     */
    String ad3Callback(AD3OfflineCallbackDTO ad3OfflineCallbackDTO);

}
