package com.payment.trade.channels.ad3Online;
import com.payment.common.dto.FundChangeDTO;
import com.payment.common.entity.Channel;
import com.payment.common.entity.OrderRefund;
import com.payment.common.entity.Orders;
import com.payment.common.entity.RabbitMassage;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.HttpResponse;
import com.payment.trade.dto.AD3OnlineAcquireDTO;
import com.payment.trade.dto.AD3OnlineOrderQueryDTO;
import com.payment.trade.dto.SendAdRefundDTO;
import com.payment.trade.vo.RefundAdResponseVO;
import java.util.Map;

/**
 * AD3线上网关收单接口
 */
public interface AD3OnlineAcquireService {

    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/14
     * @Descripate ad3线上网银退款接口
     **/
    HttpResponse RefundOrder(SendAdRefundDTO sendAdRefundDTO, Map<String, Object> headerMap);


    /**
     * @Author YangXu
     * @Date 2019/4/4
     * @Descripate 重复请求退款
     * @return
     **/
    String repeatRefund(String name,OrderRefund orderRefund);


    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/14
     * @Descripate 判断退款是否成功
     **/
    boolean judgeRefundAdResponseVO(RefundAdResponseVO refundAdResponseVO);


    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/15
     * @Descripate 退款时 ---  通道退款操作
     **/
    void doUsRefundInRef(BaseResponse baseResponse,FundChangeDTO fundChangeDTO, OrderRefund orderRefund);


    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/15
     * @Descripate 退款时 --- 通道撤销操作
     **/
    void doUsCancelInRef(BaseResponse baseResponse, OrderRefund orderRefund, RabbitMassage rabbitMassage);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/14
     * @Descripate 对请求进行签名
     **/
    String signMsg(Object o);

    /**
     * ad3 线上网关收单接口
     *
     * @param  channel ad3线上网关收单接口参数实体
     * @param baseResponse
     * @return 线上网关收单接口响应实体
     */
    BaseResponse onlineOrder(Orders orders, Channel channel, BaseResponse baseResponse);

    /**
     * ad3 线上订单查询接口
     *
     * @param ad3OnlineOrderQueryDTO
     * @return
     */
    HttpResponse ad3OnlineOrderQuery(AD3OnlineOrderQueryDTO ad3OnlineOrderQueryDTO, Map<String, Object> headerMap);

    /**
     * 封装AD3收单参数
     * @param orders
     * @param channel
     * @return
     */
    AD3OnlineAcquireDTO getAd3OnlineAcquireDTOAttr(Orders orders, Channel channel);
}
