package com.payment.trade.channels.nganluong;

import com.payment.common.entity.Channel;
import com.payment.common.entity.Orders;
import com.payment.common.response.BaseResponse;

public interface NganLuongService {

    /**
     * nganLuong网银收单方法
     *
     * @param orders  订单
     * @param channel 通道
     * @return
     */
    BaseResponse nganLuongPay(Orders orders, Channel channel, BaseResponse baseResponse);
}
