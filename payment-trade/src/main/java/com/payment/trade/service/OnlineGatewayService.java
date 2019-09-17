package com.payment.trade.service;

import com.payment.common.response.BaseResponse;
import com.payment.trade.dto.*;
import com.payment.trade.vo.OnlineOrdersInfoVO;

import java.util.List;

/**
 * 亚洲钱包业务
 */
public interface OnlineGatewayService {

    /**
     * 对商户的线上网关收单
     *
     *
     * @param placeOrdersDTO
     * @return
     */
    BaseResponse gateway(PlaceOrdersDTO placeOrdersDTO);

    /**
     * 收银台所需的信息
     *
     * @param orderId
     * @param language
     * @return
     */
    BaseResponse cashier(String orderId, String language);

    /**
     * 线上通道订单状态查询
     *
     * @param onlineOrderQueryDTO
     * @return
     */
    BaseResponse onlineOrderQuery(OnlineOrderQueryDTO onlineOrderQueryDTO);

    /**
     * 收银台收单
     *
     * @param cashierDTO
     * @return
     */
    BaseResponse cashierGateway(CashierDTO cashierDTO);

    /**
     * 查询线上订单信息
     *
     * @param onlineqOrderInfoDTO
     * @return
     */
    List<OnlineOrdersInfoVO> pageOnlineqOrderInfo(OnlineqOrderInfoDTO onlineqOrderInfoDTO);

    /**
     * 线上通道订单状态查询 RSA
     *
     * @param onlineOrderQueryRSADTO
     * @return
     */
    BaseResponse onlineqOrderQueryingUseRSA(OnlineOrderQueryRSADTO onlineOrderQueryRSADTO);

    /**
     * 模拟界面用
     *
     * @param placeOrdersDTO
     * @return
     */
    BaseResponse imitateGateway(PlaceOrdersDTO placeOrdersDTO);
}
