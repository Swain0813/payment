package com.payment.trade.channels.wechat;
import com.payment.common.dto.FundChangeDTO;
import com.payment.common.entity.Channel;
import com.payment.common.entity.OrderRefund;
import com.payment.common.entity.Orders;
import com.payment.common.entity.RabbitMassage;
import com.payment.common.response.BaseResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface WechatService {

    /**
     * wechatCSB收单方法
     *
     * @param orders  订单
     * @param channel 通道
     * @return
     */
    BaseResponse wechatCSB(Orders orders, Channel channel, BaseResponse baseResponse);
    /**
     * 微信退款接口 上报清结算
     *
     * @param orderRefund  订单
     * @return
     */
    BaseResponse wechatRefund(OrderRefund orderRefund, FundChangeDTO fundChangeDTO, BaseResponse baseResponse);


    /**
     * 微信退款接口 不上报清结算
     *
     * @param orders  订单
     * @return
     */
    void wechatRefund2(Orders  orders, RabbitMassage rabbitMassage);

    /**
     * @Author YangXu
     * @Date 2019/6/28
     * @Descripate 处于退款中时
     * @return
     **/
    void cancelRefunding(Orders order, RabbitMassage rabbitMassage);

    /**
     * 微信退款接口
     *
     * @param orderRefund  订单
     * @return
     */
    BaseResponse wechatCancel(OrderRefund orderRefund, BaseResponse baseResponse, RabbitMassage rabbitMassage);

    /**
     * wechat线下CSB回调
     *
     * @return
     */
    void wechatCSBCallback(HttpServletRequest request, HttpServletResponse response);
}
