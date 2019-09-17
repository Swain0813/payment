package com.payment.trade.channels.enets;

import com.payment.common.entity.Channel;
import com.payment.common.entity.Orders;
import com.payment.common.response.BaseResponse;
import com.payment.trade.dto.EnetsCallbackDTO;
import com.payment.trade.dto.EnetsPosCallbackDTO;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletResponse;

public interface EnetsService {

    /**
     * enets网银收单方法
     *
     * @param orders  订单
     * @param channel 通道
     * @return
     */
    BaseResponse eNetsBankPay(Orders orders, Channel channel, BaseResponse baseResponse);

    /**
     * enets线上扫码收单方法
     *
     * @param orders  订单
     * @param channel 通道
     * @return
     */
    BaseResponse eNetsOnlineQRCode(Orders orders, Channel channel, BaseResponse baseResponse);

    /**
     * enets网银浏览器回调方法
     *
     * @param enetsCallbackDTO enets回调实体
     * @return
     */
    void eNetsBankBrowserCallback(EnetsCallbackDTO enetsCallbackDTO, String txnRes, HttpServletResponse response);

    /**
     * enets网银服务器回调方法
     *
     * @param enetsCallbackDTO enets回调实体
     * @return
     */
    ResponseEntity<Void> eNetsBankServerCallback(EnetsCallbackDTO enetsCallbackDTO, String txnRes, HttpServletResponse response);


    /**
     * enets线上扫码浏览器回调方法
     *
     * @param enetsCallbackDTO enets回调实体
     * @return
     */
    void eNetsQrCodeBrowserCallback(EnetsCallbackDTO enetsCallbackDTO, String txnRes, HttpServletResponse response);

    /**
     * enets线上扫码服务器回调方法
     *
     * @param enetsCallbackDTO enets回调实体
     * @return
     */
    ResponseEntity<Void> eNetsQrCodeServerCallback(EnetsCallbackDTO enetsCallbackDTO, String txnRes, HttpServletResponse response);

    /**
     * enetsPOS CSB回调方法
     *
     * @param enetsPosCallbackDTO enets回调实体
     * @return
     */
    ResponseEntity<Void> eNetsPosCSBCallback(EnetsPosCallbackDTO enetsPosCallbackDTO, HttpServletResponse response);
}
