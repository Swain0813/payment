package com.payment.trade.channels.alipay;
import com.payment.common.dto.FundChangeDTO;
import com.payment.common.entity.Channel;
import com.payment.common.entity.OrderRefund;
import com.payment.common.entity.Orders;
import com.payment.common.entity.RabbitMassage;
import com.payment.common.response.BaseResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
public interface AliPayService {

    /**
     * AliPayCSB收单方法
     *
     * @param orders  订单
     * @param channel 通道
     * @return
     */
    BaseResponse aliPayCSB(Orders orders, Channel channel, BaseResponse baseResponse);

    /**
     * AliPay退款方法 上报清结算
     *
     * @param orderRefund  订单
     * @param
     * @return
     */
    BaseResponse aliPayRefund(OrderRefund orderRefund, FundChangeDTO fundChangeDTO, BaseResponse baseResponse);

    /**
     * AliPay退款方法 不上报清结算
     *
     * @param orders  订单
     * @param
     * @return
     */
    void aliPayRefund2(Orders orders, RabbitMassage rabbitMassage);

    /**
     * @Author YangXu
     * @Date 2019/6/28
     * @Descripate AliPay退款方法 处于退款中时
     * @return
     **/
    void cancelRefunding(Orders order, RabbitMassage rabbitMassage);

    /**
     * AliPay撤销方法
     *
     * @param orderRefund  订单
     * @param
     * @return
     */
    BaseResponse aliPayCancel(OrderRefund orderRefund,  BaseResponse baseResponse, RabbitMassage rabbitMassage);


    /**
     * @Author YangXu
     * @Date 2019/6/24
     * @Descripate aliPay支付CSB扫码服务器回调
     * @return
     **/
    BaseResponse aliPayCB_TPMQRCReturn(HttpServletRequest request, HttpServletResponse response);
}
