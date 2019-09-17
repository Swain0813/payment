package com.payment.trade.channels.xendit;

import com.payment.common.entity.Channel;
import com.payment.common.entity.Orders;
import com.payment.common.response.BaseResponse;
import com.payment.trade.dto.XenditServerCallbackDTO;

public interface XenditService {

    /**
     * xendit网银收单方法
     *
     * @param orders  订单
     * @param channel 通道
     * @return
     */
    BaseResponse xenditPay(Orders orders, Channel channel, BaseResponse baseResponse);


//    /**
//     * xendit网银收单方法 付款
//     *
//     * @param orders  订单
//     * @param channel 通道
//     * @return
//     */
//    BaseResponse xenditPay2(Orders orders, Channel channel, BaseResponse baseResponse);


    /**
     * xendit服务器回调
     *
     * @param xenditServerCallbackDTO xendit服务器回调实体
     * @return
     */
    void xenditServerCallback(XenditServerCallbackDTO xenditServerCallbackDTO);
}
