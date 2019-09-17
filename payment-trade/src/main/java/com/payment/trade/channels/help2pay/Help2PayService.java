package com.payment.trade.channels.help2pay;

import com.payment.common.entity.Channel;
import com.payment.common.entity.OrderPayment;
import com.payment.common.entity.Orders;
import com.payment.common.response.BaseResponse;
import com.payment.trade.dto.Help2PayCallbackDTO;

import javax.servlet.http.HttpServletResponse;

public interface Help2PayService {

    /**
     * help2Pay网银收单方法
     *
     * @param orders  订单
     * @param channel 通道
     * @return
     */
    BaseResponse help2Pay(Orders orders, Channel channel, BaseResponse baseResponse);

    /**
     * help2Pay汇款方法
     * @return
     */
    BaseResponse help2PayPayOut(OrderPayment orderPayment,Channel channel);

    /**
     * help2Pay浏览器回调方法
     *
     * @param help2PayCallbackDTO help2Pay回调实体
     * @return
     */
    void help2PayBrowserCallback(Help2PayCallbackDTO help2PayCallbackDTO, HttpServletResponse response);

    /**
     * help2Pay服务器回调方法
     *
     * @param help2PayCallbackDTO help2Pay回调实体
     * @return
     */
    void help2PayServerCallback(Help2PayCallbackDTO help2PayCallbackDTO, HttpServletResponse response);

    /**
     * @Author YangXu
     * @Date 2019/7/22
     * @Descripate  help2Pay payOut验证接口
     * @return
     **/
    String verification(String transId,String key);
}
