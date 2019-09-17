package com.payment.trade.utils;

import com.payment.common.entity.Channel;
import com.payment.common.entity.Orders;
import com.payment.common.response.BaseResponse;

/**
 * 处理器适配器类,空实现,具体实现继承该类
 */
public abstract class AbstractHandlerAdapter implements AbstractHandler {


    /**
     * 线下BSC处理方法
     *
     * @param orders       订单
     * @param channel      通道
     * @param baseResponse 通用响应实体
     * @param authCode     支付条码
     * @return 通用响应实体
     */
    @Override
    public BaseResponse offlineBSC(Orders orders, Channel channel, BaseResponse baseResponse, String authCode) {
        return null;
    }


    /**
     * 线下CSB处理方法
     *
     * @param orders       订单
     * @param channel      通道
     * @param baseResponse 通用响应实体
     * @return 通用响应实体
     */
    @Override
    public BaseResponse offlineCSB(Orders orders, Channel channel, BaseResponse baseResponse) {
        return null;
    }
}
