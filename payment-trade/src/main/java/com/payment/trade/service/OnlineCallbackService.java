package com.payment.trade.service;

import com.payment.trade.dto.AD3OnlineCallbackDTO;

import javax.servlet.http.HttpServletResponse;

/**
 * 线上回调接口Service
 */
public interface OnlineCallbackService {
    /**
     * 线上AD3回调接口
     *
     *
     * @param ad3OnlineCallbackDTO @return
     */
    String callback(AD3OnlineCallbackDTO ad3OnlineCallbackDTO);

    /**
     * AD3线上浏览器地址回调处理方法
     *  @param acquireVO
     * @param response
     */
    void jump(AD3OnlineCallbackDTO acquireVO, HttpServletResponse response);
}
