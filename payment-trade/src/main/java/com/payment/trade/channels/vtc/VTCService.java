package com.payment.trade.channels.vtc;

import com.payment.common.entity.Channel;
import com.payment.common.entity.Orders;
import com.payment.common.response.BaseResponse;
import com.payment.trade.dto.VtcCallbackDTO;

import javax.servlet.http.HttpServletResponse;

public interface VTCService {

    /**
     * vtc收单方法
     *
     * @param orders  订单
     * @param channel 通道
     * @return
     */
    BaseResponse vtcPay(Orders orders, Channel channel, BaseResponse baseResponse);

    /**
     * vtc服务器回调方法
     *
     * @param vtcCallbackDTO vtc回调输入参数
     * @return
     */
    void vtcPayServerCallback(VtcCallbackDTO vtcCallbackDTO, String data, HttpServletResponse response);

    /**
     * vtc浏览器回调方法
     *
     * @param vtcCallbackDTO vtc回调输入参数
     * @return
     */
    void vtcPayBrowserCallback(VtcCallbackDTO vtcCallbackDTO, HttpServletResponse response);
}
