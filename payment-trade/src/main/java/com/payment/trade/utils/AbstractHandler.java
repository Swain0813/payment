package com.payment.trade.utils;
import com.payment.common.entity.Channel;
import com.payment.common.entity.Orders;
import com.payment.common.response.BaseResponse;

/**
 * 业务抽象处理器
 */
public interface AbstractHandler {

    /**
     * 线下CSB处理方法
     *
     * @param orders       订单
     * @param channel      通道
     * @param baseResponse 通用响应实体
     * @return 通用响应实体
     */
    BaseResponse offlineCSB(Orders orders, Channel channel, BaseResponse baseResponse);

    /**
     * 线下BSC处理方法
     *
     * @param orders       订单
     * @param channel      通道
     * @param baseResponse 通用响应实体
     * @param authCode     支付条码
     * @return 通用响应实体
     */
    BaseResponse offlineBSC(Orders orders, Channel channel, BaseResponse baseResponse, String authCode);

}
