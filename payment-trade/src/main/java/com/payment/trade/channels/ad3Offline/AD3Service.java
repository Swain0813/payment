package com.payment.trade.channels.ad3Offline;
import com.payment.common.dto.FundChangeDTO;
import com.payment.common.entity.OrderRefund;
import com.payment.common.entity.Orders;
import com.payment.common.entity.RabbitMassage;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.HttpResponse;
import com.payment.trade.dto.*;
import com.payment.trade.utils.AbstractHandler;
import com.payment.trade.vo.AD3LoginVO;
import com.payment.trade.vo.TerminalOrderVO;
import java.util.Map;

/**
 * AD3通道接口(AD3线下相关业务)
 */
public interface AD3Service extends AbstractHandler {

    /**
     * 终端登陆接口
     *
     * @param ad3LoginDTO 登陆输入实体
     * @param headerMap   请求头map
     * @return token
     */
    HttpResponse ad3Login(AD3LoginDTO ad3LoginDTO, Map<String, Object> headerMap);


    /**
     * 终端CSB扫码支付接口
     *
     * @param ad3CSBScanPayDTO 终端CSB扫码支付输入实体
     * @param headerMap        请求头map
     * @return CSB扫码支付返回实体
     */
    HttpResponse ad3CSBScanPay(AD3CSBScanPayDTO ad3CSBScanPayDTO, Map<String, Object> headerMap);

    /**
     * 终端BSC扫码支付接口
     *
     * @param ad3BSCScanPayDTO 终端CSB扫码支付输入实体
     * @param headerMap        请求头map
     * @return CSB扫码支付返回实体
     */
    HttpResponse ad3BSCScanPay(AD3BSCScanPayDTO ad3BSCScanPayDTO, Map<String, Object> headerMap);


    /**
     * 终端单笔订单查询接口
     *
     * @param ad3QuerySingleOrderDTO 查询单笔订单输入实体
     * @param headerMap              请求头map
     * @return ad3订单状态输出实体
     */
    HttpResponse ad3QueryOneOrder(AD3QuerySingleOrderDTO ad3QuerySingleOrderDTO, Map<String, Object> headerMap);


    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/14
     * @Descripate ad3线下退款接口
     **/
    HttpResponse RefundOrder(AD3RefundDTO ad3RefundDTO, Map<String, Object> headerMap);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/4/4
     * @Descripate 重复请求退款
     **/
    String repeatRefund(String name, OrderRefund orderRefund);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/15
     * @Descripate 退款时 ---  退款操作
     **/
    void doUsRefundInRef(BaseResponse baseResponse, OrderRefund orderRefund, FundChangeDTO fundChangeDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/15
     * @Descripate 退款时 ---  撤销操作
     **/
    void doUsCancelInRef(BaseResponse baseResponse, OrderRefund orderRefund, RabbitMassage rabbitMassage);

    /**
     * 生成AD3认证签名
     *
     * @param commonObj   AD3公共参数输入实体
     * @param businessObj AD3业务参数输入实体
     * @param token       token
     * @return ad3签名
     */
    String createAD3Signature(Object commonObj, Object businessObj, String token);


    /**
     * 撤销时-调用线下的通道退款
     *
     * @param orderRefund
     */
    void repeal(OrderRefund orderRefund,RabbitMassage rabbitMassage);

    /**
     * 撤销时退款--调用线下通道退款 上报清结算
     *
     * @param orderRefund
     * @param fundChangeDTO
     */
    void cancelRefund(OrderRefund orderRefund, FundChangeDTO fundChangeDTO);

    /**
     * 撤销时退款--调用线下通道退款 不上报清结算
     *
     * @param orders
     */
    void cancelRefund2(Orders orders, RabbitMassage rabbitMassage);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/6/28
     * @Descripate 处于退款中时
     **/
    void cancelRefunding(Orders order, RabbitMassage rabbitMassage);

    /**
     * 获取终端编号和token
     *
     * @return
     */
    AD3LoginVO getTerminalIdAndToken();


    /**
     * AD3通道终端查询单笔订单状态
     *
     * @param terminalOrderVO 终端查询订单状态实体
     * @param orders          订单实体
     * @return 订单查询输出实体
     */
    TerminalOrderVO ad3TerminalQueryOrder(TerminalOrderVO terminalOrderVO, Orders orders);
}
