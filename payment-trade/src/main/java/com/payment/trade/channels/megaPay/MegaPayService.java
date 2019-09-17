package com.payment.trade.channels.megaPay;

import com.payment.common.dto.FundChangeDTO;
import com.payment.common.entity.Channel;
import com.payment.common.entity.OrderRefund;
import com.payment.common.entity.Orders;
import com.payment.common.entity.RabbitMassage;
import com.payment.common.response.BaseResponse;
import com.payment.trade.dto.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface MegaPayService {

    /**
     * MegaPay网银收单方法
     *
     * @param orders  订单
     * @param channel 通道
     * @return
     */
    BaseResponse megaPay(Orders orders, Channel channel, BaseResponse baseResponse);

    /**
     * MegaPay nextPost线上扫码方法
     *
     * @param orders  订单
     * @param channel 通道
     * @return
     */
    BaseResponse megaPayNextPos(Orders orders, Channel channel, BaseResponse baseResponse);

    /**
     * MegaPay nextPost退款
     *
     * @param orderRefund   退款单
     * @param fundChangeDTO 资金变动dto
     * @return
     */
    BaseResponse megaPayNextPosRefund(OrderRefund orderRefund, FundChangeDTO fundChangeDTO, BaseResponse baseResponse);

    /**
     * MegaPay nextPost撤销
     *
     * @param orderRefund 退款单
     * @return
     */
    BaseResponse megaPayNextPosCancel(OrderRefund orderRefund, BaseResponse baseResponse, RabbitMassage rabbitMassage);

    /**
     * @return
     * @Descripate 处于退款中时
     **/
    void cancelRefunding(Orders order, RabbitMassage rabbitMassage);

    /**
     * nextPos退款接口 不上报清结算
     *
     * @param orders 订单
     * @return
     */
    void nextPosRefund2(Orders orders, RabbitMassage rabbitMassage);

    /**
     * MegaPayTHB服务器回调方法
     *
     * @param megaPayServerCallbackDTO megaPayTHB回调参数
     * @return
     */
    void megaPayThbServerCallback(MegaPayServerCallbackDTO megaPayServerCallbackDTO, HttpServletRequest request, HttpServletResponse response);

    /**
     * MegaPayTHB浏览器回调方法
     *
     * @param megaPayCallbackDTO megaPayTHB回调参数
     * @return
     */
    void megaPayThbBrowserCallback(MegaPayBrowserCallbackDTO megaPayCallbackDTO, HttpServletResponse response);

    /**
     * MegaPayIDR服务器回调方法
     *
     * @param megaPayIDRServerCallbackDTO megaPayIDR回调参数
     * @return
     */
    void megaPayIdrServerCallback(MegaPayIDRServerCallbackDTO megaPayIDRServerCallbackDTO, HttpServletRequest request, HttpServletResponse response);

    /**
     * MegaPayIDR浏览器回调方法
     *
     * @param megaPayIDRBrowserCallbackDTO megaPayIDR回调参数
     * @return
     */
    void megaPayIdrBrowserCallback(MegaPayIDRBrowserCallbackDTO megaPayIDRBrowserCallbackDTO, HttpServletResponse response);

    /**
     * nextPos回调方法
     *
     * @param nextPosCallbackDTO nextPos回调参数
     * @return
     */
    void nextPosCallback(NextPosCallbackDTO nextPosCallbackDTO, Orders orders, HttpServletResponse response);
}
