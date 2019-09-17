package com.payment.trade.channels.megaPay.impl;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.payment.common.constant.AD3MQConstant;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.constant.TradeConstant;
import com.payment.common.dto.FundChangeDTO;
import com.payment.common.dto.megapay.*;
import com.payment.common.entity.*;
import com.payment.common.enums.Status;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.common.utils.BeanToMapUtil;
import com.payment.common.utils.MD5;
import com.payment.common.vo.FundChangeVO;
import com.payment.trade.channels.megaPay.MegaPayService;
import com.payment.trade.config.AD3ParamsConfig;
import com.payment.trade.dao.ChannelsOrderMapper;
import com.payment.trade.dao.OrderRefundMapper;
import com.payment.trade.dao.OrdersMapper;
import com.payment.trade.dao.ReconciliationMapper;
import com.payment.trade.dto.*;
import com.payment.trade.feign.ChannelsFeign;
import com.payment.trade.rabbitmq.RabbitMQSender;
import com.payment.trade.service.ClearingService;
import com.payment.trade.service.CommonService;
import com.payment.trade.utils.AbstractHandlerAdapter;
import com.payment.trade.utils.HandlerType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Map;

@Service
@Slf4j
@Transactional
@HandlerType(TradeConstant.NEXTPOS_CSB_OFFLINE)
public class MegaPayServiceImpl extends AbstractHandlerAdapter implements MegaPayService {

    @Autowired
    private AD3ParamsConfig ad3ParamsConfig;

    @Autowired
    private ChannelsFeign channelsFeign;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private ChannelsOrderMapper channelsOrderMapper;

    @Autowired
    private CommonService commonService;

    @Autowired
    private ClearingService clearingService;

    @Autowired
    private RabbitMQSender rabbitMQSender;

    @Autowired
    private OrderRefundMapper refundOrderMapper;

    @Autowired
    private ReconciliationMapper reconciliationMapper;

    @Autowired
    private OrderRefundMapper orderRefundMapper;


    /**
     * MegaPay网银收单方法
     *
     * @param orders  订单
     * @param channel 通道
     * @return
     */
    @Override
    public BaseResponse megaPay(Orders orders, Channel channel, BaseResponse baseResponse) {
        if (orders.getTradeCurrency().equalsIgnoreCase("THB")) {//megaPay THB通道
            MegaPayRequestDTO megaPayRequestDTO = new MegaPayRequestDTO(orders, channel, ad3ParamsConfig.getChannelCallbackUrl().concat("/onlinecallback/megaPayThbBrowserCallback"));
            log.info("----------------- MegaPay网银收单方法 ----------------- megaPayRequestDTO: {}", JSON.toJSONString(megaPayRequestDTO));
            BaseResponse response = channelsFeign.megaPayTHB(megaPayRequestDTO);
            baseResponse.setData(response.getData());
        } else if (orders.getTradeCurrency().equalsIgnoreCase("IDR")) {//megaPay IDR通道
            MegaPayIDRRequestDTO megaPayIDRRequestDTO = new MegaPayIDRRequestDTO(orders, channel, ad3ParamsConfig.getChannelCallbackUrl().concat("/onlinecallback/megaPayIdrBrowserCallback"));
            log.info("----------------- MegaPay网银收单方法 ----------------- megaPayIDRRequestDTO: {}", JSON.toJSONString(megaPayIDRRequestDTO));
            BaseResponse response = channelsFeign.megaPayIDR(megaPayIDRRequestDTO);
            baseResponse.setData(response.getData());
        }
        log.info("----------------- MegaPay网银收单方法 返回----------------- baseResponse: {}", JSON.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * MegaPay-NextPost线上扫码方法
     *
     * @param orders  订单
     * @param channel 通道
     * @return
     */
    @Override
    public BaseResponse megaPayNextPos(Orders orders, Channel channel, BaseResponse baseResponse) {
        NextPosRequestDTO nextPosRequestDTO = new NextPosRequestDTO(orders, channel, ad3ParamsConfig.getChannelCallbackUrl().concat("/onlinecallback/nextPosCallback"));
        log.info("----------------- NextPost线上扫码方法 ----------------- nextPosRequestDTO: {}", JSON.toJSONString(nextPosRequestDTO));
        BaseResponse response = channelsFeign.nextPos(nextPosRequestDTO);
        //状态码不为200的时候
        if (!response.getCode().equals(TradeConstant.HTTP_SUCCESS)) {
            //订单创建失败
            baseResponse.setCode(EResultEnum.ORDER_CREATION_FAILED.getCode());
            return baseResponse;
        }
        baseResponse.setData(response.getData());//数据
        log.info("-----------------NextPost线上扫码方法 返回----------------- baseResponse: {}", JSON.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * MegaPay nextPost退款
     *
     * @param orderRefund   退款单
     * @param fundChangeDTO 资金变动dto
     * @return
     */
    @Override
    public BaseResponse megaPayNextPosRefund(OrderRefund orderRefund, FundChangeDTO fundChangeDTO, BaseResponse baseResponse) {
        Channel channel = commonService.getChannelByChannelCode(orderRefund.getChannelCode());
        NextPosRefundDTO nextPosRefundDTO = new NextPosRefundDTO(orderRefund, channel);
        log.info("=================【NextPos退款】=================【请求Channels服务NextPos退款】请求参数 nextPosRefundDTO: {} ", JSON.toJSONString(nextPosRefundDTO));
        BaseResponse response = channelsFeign.nextPosRefund(nextPosRefundDTO);
        log.info("=================【NextPos退款】=================【Channels服务响应】 response: {} ", JSON.toJSONString(response));
        if (response.getCode().equals(TradeConstant.HTTP_SUCCESS)) {
            //请求成功
            Map<String, Object> respMap = (Map<String, Object>) response.getData();
            if (response.getMsg().equals(TradeConstant.HTTP_SUCCESS_MSG)) {
                //退款成功
                refundOrderMapper.updateStatuts(orderRefund.getId(), TradeConstant.REFUND_SUCCESS, String.valueOf(respMap.get("transactionID")), null);
                //改原订单状态
                if (TradeConstant.REFUND_TYPE_TOTAL.equals(orderRefund.getRefundType())) {
                    ordersMapper.updateOrderRefundStatus(orderRefund.getInstitutionOrderId(), TradeConstant.ORDER_REFUND_SUCCESS);
                } else {
                    if (orderRefund.getRemark2().equals("全额")) {
                        ordersMapper.updateOrderRefundStatus(orderRefund.getInstitutionOrderId(), TradeConstant.ORDER_REFUND_SUCCESS);
                    } else {
                        ordersMapper.updateOrderRefundStatus(orderRefund.getInstitutionOrderId(), TradeConstant.ORDER_REFUND_PART_SUCCESS);
                    }
                }
            } else {
                //退款失败
                baseResponse.setMsg(EResultEnum.REFUND_FAIL.getCode());
                //添加调账记录
                Reconciliation reconciliation = commonService.createReconciliation(orderRefund, TradeConstant.REFUND_FAIL_RECONCILIATION);
                reconciliationMapper.insert(reconciliation);
                fundChangeDTO.setRefcnceFlow(reconciliation.getId());
                fundChangeDTO.setSysorderid(orderRefund.getId());
                fundChangeDTO.setTradetype(TradeConstant.AA);
                fundChangeDTO.setTxnamount(String.valueOf(orderRefund.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP)));
                fundChangeDTO.setSltamount(String.valueOf(orderRefund.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP)));
                fundChangeDTO.setBalancetype(TradeConstant.NORMAL_FUND);
                fundChangeDTO.setSignMsg(null);
                BaseResponse cFundChange = clearingService.fundChange(fundChangeDTO, null);
                if (!cFundChange.getCode().equals(String.valueOf(AsianWalletConstant.HTTP_SUCCESS_STATUS))) {
                    //请求失败
                    orderRefund.setRemark3(JSON.toJSONString(fundChangeDTO));
                    RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orderRefund));
                    log.info("=================【NextPos退款】=================【退款操作 上报队列 MQ_QJS_TZSB_DL】 rabbitMassage : {}", JSON.toJSONString(rabbitMassage));
                    rabbitMQSender.send(AD3MQConstant.MQ_QJS_TZSB_DL, JSON.toJSONString(rabbitMassage));
                } else {
                    //请求成功
                    FundChangeVO fundChangeVO = (FundChangeVO) cFundChange.getData();
                    if (fundChangeVO.getRespCode().equals(TradeConstant.CLEARING_SUCCESS)) {
                        refundOrderMapper.updateStatuts(orderRefund.getId(), TradeConstant.REFUND_FALID, null, fundChangeVO.getRespMsg());
                        reconciliationMapper.updateStatusById(reconciliation.getId(), TradeConstant.RECONCILIATION_SUCCESS);
                        //改原订单状态
                        if (TradeConstant.REFUND_TYPE_TOTAL.equals(orderRefund.getRefundType())) {
                            ordersMapper.updateOrderRefundStatus(orderRefund.getInstitutionOrderId(), TradeConstant.ORDER_REFUND_FAIL);
                        } else {
                            BigDecimal oldRefundAmount = refundOrderMapper.getTotalAmountByOrderId(orderRefund.getOrderId()); //已退款金额
                            oldRefundAmount = oldRefundAmount == null ? BigDecimal.ZERO : oldRefundAmount;
                            if (oldRefundAmount.compareTo(BigDecimal.ZERO) == 0) {
                                ordersMapper.updateOrderRefundStatus(orderRefund.getInstitutionOrderId(), TradeConstant.ORDER_REFUND_FAIL);
                            }
                        }
                    } else {
                        orderRefund.setRemark3(JSON.toJSONString(fundChangeDTO));
                        RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orderRefund));
                        log.info("=================【NextPos退款】=================【退款操作 上报队列 MQ_QJS_TZSB_DL】 rabbitMassage: {} ", JSON.toJSONString(rabbitMassage));
                        rabbitMQSender.send(AD3MQConstant.MQ_QJS_TZSB_DL, JSON.toJSONString(rabbitMassage));
                    }
                }
            }
        } else {
            //请求失败
            baseResponse.setMsg(EResultEnum.REFUNDING.getCode());
            orderRefund.setRemark3(JSON.toJSONString(fundChangeDTO));
            RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orderRefund));
            log.info("===============【NextPos退款】===============【退款操作 请求失败上报队列 MQ_TK_NEXTPOS_QQSB_DL】 rabbitMassage: {} ", JSON.toJSONString(rabbitMassage));
            rabbitMQSender.send(AD3MQConstant.MQ_TK_NEXTPOS_QQSB_DL, JSON.toJSONString(rabbitMassage));
        }
        return response;
    }

    /**
     * MegaPay nextPost撤销
     *
     * @param orderRefund 退款单
     * @return
     */
    @Override
    public BaseResponse megaPayNextPosCancel(OrderRefund orderRefund, BaseResponse baseResponse, RabbitMassage rabbitMassage) {
        Channel channel = commonService.getChannelByChannelCode(orderRefund.getChannelCode());
        NextPosRefundDTO nextPosRefundDTO = new NextPosRefundDTO(orderRefund, channel);
        log.info("=================【NextPos撤销】=================【请求Channels服务NextPos退款】请求参数 nextPosRefundDTO: {} ", JSON.toJSONString(nextPosRefundDTO));
        BaseResponse response = channelsFeign.nextPosRefund(nextPosRefundDTO);
        log.info("=================【NextPos撤销】=================【Channels服务响应】 response: {} ", JSON.toJSONString(response));
        if (response.getCode().equals(TradeConstant.HTTP_SUCCESS)) {
            //请求成功
            Map<String, Object> map = (Map<String, Object>) response.getData();
            if (response.getMsg().equals(TradeConstant.HTTP_SUCCESS_MSG)) {
                //退款成功
                refundOrderMapper.updateStatuts(orderRefund.getId(), TradeConstant.REFUND_SUCCESS, String.valueOf(map.get("transactionID")), null);
                //改原订单状态
                if (TradeConstant.REFUND_TYPE_TOTAL.equals(orderRefund.getRefundType())) {
                    ordersMapper.updateOrderRefundStatus(orderRefund.getInstitutionOrderId(), TradeConstant.ORDER_REFUND_SUCCESS);
                } else {
                    if (orderRefund.getRemark2().equals("全额")) {
                        ordersMapper.updateOrderRefundStatus(orderRefund.getInstitutionOrderId(), TradeConstant.ORDER_REFUND_SUCCESS);
                    } else {
                        ordersMapper.updateOrderRefundStatus(orderRefund.getInstitutionOrderId(), TradeConstant.ORDER_REFUND_PART_SUCCESS);
                    }
                }
            } else {
                //退款失败
                log.info("=================【NextPos撤销】=================【撤销操作 上游退款失败】");
                baseResponse.setMsg(EResultEnum.REFUNDING.getCode());
                rabbitMQSender.send(AD3MQConstant.E_MQ_CX_TDTKSB_DL, JSON.toJSONString(rabbitMassage));
            }
        } else {
            //请求失败
            log.info("=================【NextPos撤销】=================【撤销操作 请求失败】");
            baseResponse.setMsg(EResultEnum.REFUNDING.getCode());
            rabbitMQSender.send(AD3MQConstant.E_MQ_CX_TDTKSB_DL, JSON.toJSONString(rabbitMassage));
        }
        return baseResponse;
    }

    /**
     * @return
     * @Descripate 处于退款中时
     **/
    @Override
    public void cancelRefunding(Orders orders, RabbitMassage rabbitMassage) {
        Channel channel = commonService.getChannelByChannelCode(orders.getChannelCode());
        NextPosQueryDTO nextPosQueryDTO = new NextPosQueryDTO(orders.getId(), channel);
        BaseResponse baseResponse = channelsFeign.nextPosQuery(nextPosQueryDTO);
        if (baseResponse.getCode().equals(TradeConstant.HTTP_SUCCESS)) {
            //请求成功
            Map<String, Object> map = (Map<String, Object>) baseResponse.getData();
            if (baseResponse.getMsg().equals(TradeConstant.HTTP_SUCCESS_MSG)) {
                //查询成功
                if (map.get(channel.getPayCode()).equals("SUCCESS")) {
                    //交易成功
                    orders.setTradeStatus(TradeConstant.ORDER_PAY_SUCCESS);//付款成功
                    //更新订单状态
                    if (ordersMapper.updateOrderByAd3Query(orders.getId(), TradeConstant.ORDER_PAY_SUCCESS,
                            orders.getChannelNumber(), orders.getChannelCallbackTime()) == 1) {//更新成功
                        RabbitMassage rabbitRefundMsg = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                        this.nextPosRefund2(orders, rabbitRefundMsg);
                    } else {//更新失败后去查询订单信息
                        RabbitMassage rabbitOrderMsg = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                        rabbitMQSender.send(AD3MQConstant.TC_MQ_CANCEL_ORDER, JSON.toJSONString(rabbitOrderMsg));
                    }
                } else if (map.get(channel.getPayCode()).equals("PAYERROR")) {
                    //交易失败
                    log.info("****************  支付失败 ***************** rabbitMassage : {} ", JSON.toJSONString(rabbitMassage));
                    orders.setTradeStatus(TradeConstant.ORDER_PAY_FAILD);//付款失败
                    ordersMapper.updateOrderByAd3Query(orders.getId(), TradeConstant.ORDER_PAY_FAILD, orders.getChannelNumber(), orders.getChannelCallbackTime());
                } else {
                    log.info("****************  其他状态 ***************** status: {}", (map.get(channel.getPayCode())));
                }
            } else {
                //请求失败
                log.info("**************** 订单处于退款中时 NextPos 查询订单失败 ***************** rabbitMassage : {} ", JSON.toJSONString(rabbitMassage));
                rabbitMQSender.sendAd3Sleep(AD3MQConstant.E_MQ_AD3_ORDER_QUERY, JSON.toJSONString(rabbitMassage));
            }
        } else {
            //请求失败
            log.info("**************** 订单处于退款中时 NextPos 查询订单失败 ***************** rabbitMassage : {} ", JSON.toJSONString(rabbitMassage));
            rabbitMQSender.sendAd3Sleep(AD3MQConstant.E_MQ_AD3_ORDER_QUERY, JSON.toJSONString(rabbitMassage));
        }
    }

    /**
     * nextPos退款接口 不上报清结算
     *
     * @param orders 订单
     * @return
     */
    @Override
    public void nextPosRefund2(Orders orders, RabbitMassage rabbitMassage) {
        Channel channel = commonService.getChannelByChannelCode(orders.getChannelCode());
        //查询退款单
        OrderRefund orderRefund = orderRefundMapper.selectByOrderId(orders.getId());
        //获取原订单的refCode字段(NextPos用)
        orderRefund.setSign(orders.getSign());
        NextPosRefundDTO nextPosRefundDTO = new NextPosRefundDTO(orderRefund, channel);
        log.info("=================【NextPos退款】=================【请求Channels服务NextPos退款】请求参数 nextPosRefundDTO: {} ", JSON.toJSONString(nextPosRefundDTO));
        BaseResponse response = channelsFeign.nextPosRefund(nextPosRefundDTO);
        log.info("=================【NextPos退款】=================【Channels服务响应】 response: {} ", JSON.toJSONString(response));
        if (response.getCode().equals(TradeConstant.HTTP_SUCCESS)) {
            //请求成功
            if (response.getMsg().equals(TradeConstant.HTTP_SUCCESS_MSG)) {
                //退款成功
                ordersMapper.updateOrderCancelStatus(orders.getInstitutionOrderId(), orders.getDeviceOperator(), TradeConstant.ORDER_CANNEL_SUCCESS);
            } else {
                //退款失败
                ordersMapper.updateOrderCancelStatus(orders.getInstitutionOrderId(), orders.getDeviceOperator(), TradeConstant.ORDER_CANNEL_FALID);
            }
        } else {
            //请求失败
            log.info("----------------- 退款操作 请求失败上报队列 MQ_TK_WECHAT_QQSB_DL -------------- rabbitMassage: {} ", JSON.toJSON(rabbitMassage));
            rabbitMQSender.sendAd3Sleep(AD3MQConstant.MQ_AD3_REFUND, JSON.toJSONString(rabbitMassage));

        }
    }

    /**
     * MegaPayTHB服务器回调方法
     *
     * @param megaPayServerCallbackDTO megaPayTHB回调参数
     * @return
     */
    @Override
    public void megaPayThbServerCallback(MegaPayServerCallbackDTO megaPayServerCallbackDTO, HttpServletRequest request, HttpServletResponse response) {
        //校验回调参数
        if (!checkTHBCallback(megaPayServerCallbackDTO)) {
            return;
        }
        //查询通道MD5Key
        ChannelsOrder channelsOrder = channelsOrderMapper.selectByPrimaryKey(megaPayServerCallbackDTO.getInv());
        //根据通道md5KeyStr获取交易状态
        String result = request.getParameter(channelsOrder.getMd5KeyStr());
        megaPayServerCallbackDTO.setResult(result);
        megaPayServerCallbackDTO.setMd5KeyStr(channelsOrder.getMd5KeyStr());
        log.info("===========【megaPayTHB服务器回调方法信息记录】==============【交易状态】: {}", result);
        if (megaPayServerCallbackDTO.getInv().startsWith("PGO")) {
            log.info("===========【megaPayTHB服务器回调方法信息记录】==============这笔回调订单信息属于AD3 inv:{}", megaPayServerCallbackDTO.getInv());
            //分发给AD3
            commonService.megaTHBCallbackAD3(megaPayServerCallbackDTO, "megaPayReturn.do");
            return;
        }
        //设置金额显示
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMinimumFractionDigits(3);//设置数值的小数部分允许的最小位数。
        numberFormat.setMaximumFractionDigits(2);//设置数值的小数部分允许的最大位数。
        numberFormat.setMaximumIntegerDigits(15);//设置数值的整数部分允许的最大位数。
        numberFormat.setMinimumIntegerDigits(1);//设置数值的整数部分允许的最小位数
        String sign = megaPayServerCallbackDTO.getRefCode() + megaPayServerCallbackDTO.getInv() + channelsOrder.getMd5KeyStr() + result + numberFormat.format(Double.valueOf(megaPayServerCallbackDTO.getAmt()));
        log.info("===========【megaPayTHB服务器回调方法信息记录】==============签名前的明文: {}", sign);
        String signMsg = MD5.MD5Encode(sign).toUpperCase();
        log.info("===========【megaPayTHB服务器回调方法信息记录】==============签名后的密文: {}", signMsg);
        //验签
        if (!org.apache.commons.lang.StringUtils.equals(sign, signMsg)) {
            log.info("===========【megaPayTHB服务器回调方法信息记录】==============签名不匹配");
            return;
        }
        //查询原订单信息
        Orders orders = ordersMapper.selectByPrimaryKey(megaPayServerCallbackDTO.getInv());
        if (orders == null) {
            log.info("===========【megaPayTHB服务器回调方法信息记录】==============【回调订单信息不存在】");
            return;
        }
        //校验订单状态
        if (!TradeConstant.ORDER_PAYING.equals(orders.getTradeStatus())) {
            log.info("=================【megaPayTHB服务器回调方法信息记录】=================【订单状态不为支付中】");
            return;
        }
        //校验订单信息
        if (new BigDecimal(megaPayServerCallbackDTO.getAmt()).compareTo(orders.getTradeAmount()) != 0) {
            log.info("=================【megaPayTHB服务器回调方法信息记录】=================【订单信息不匹配】");
            return;
        }
        orders.setUpdateTime(new Date());//修改时间
        orders.setChannelCallbackTime(new Date());//通道回调时间
        Example example = new Example(Orders.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id", orders.getId());
        criteria.andEqualTo("tradeStatus", "2");
        if ("000".equals(megaPayServerCallbackDTO.getResult())) {
            log.info("===========【megaPayTHB服务器回调方法信息记录】==============【订单已支付成功】 orderId: {}", orders.getId());
            //支付成功
            orders.setTradeStatus(TradeConstant.ORDER_PAY_SUCCESS);
            //更改channelsOrders状态
            try {
                channelsOrderMapper.updateStatusById(orders.getId(), null, TradeConstant.TRADE_SUCCESS);
            } catch (Exception e) {
                log.error("===========【megaPayTHB服务器回调方法信息记录】==============【更新通道订单异常】");
            }
            //修改原订单状态
            int i = ordersMapper.updateByExampleSelective(orders, example);
            if (i > 0) {
                log.info("=================【megaPayTHB服务器回调方法信息记录】=================【订单支付成功后更新数据库成功】 orderId: {}", orders.getId());
                //计算支付成功时的通道网关手续费
                commonService.calcCallBackGatewayFeeSuccess(orders);
                //添加日交易限额与日交易笔数
                commonService.quota(orders.getInstitutionCode(), orders.getProductCode(), orders.getTradeAmount());
                //新增交易成功的订单物流信息
                commonService.insertOrderLogistics(orders);
                //支付成功后向用户发送邮件
                commonService.sendEmail(orders.getDraweeEmail(), orders.getLanguage(), Status._1, orders);
                try {
                    //账户信息不存在的场合创建对应的账户信息
                    if (commonService.getAccount(orders.getInstitutionCode(), orders.getOrderCurrency()) == null) {
                        log.info("=================【megaPayTHB服务器回调方法信息记录】=================【上报清结算前线下下单创建账户信息】");
                        commonService.createAccount(orders.getInstitutionCode(), orders.getOrderCurrency());
                    }
                    //分润
                    if(!StringUtils.isEmpty(orders.getAgencyCode())){
                        rabbitMQSender.send(AD3MQConstant.MQ_FR_DL, orders.getId());
                    }
                    //更新成功,上报清结算
                    FundChangeDTO fundChangeDTO = new FundChangeDTO(orders, TradeConstant.NT, orders.getInstitutionCode());
                    //上报清结算资金变动接口
                    BaseResponse fundChangeResponse = clearingService.fundChange(fundChangeDTO, null);
                    if (fundChangeResponse.getCode() != null && TradeConstant.HTTP_SUCCESS.equals(fundChangeResponse.getCode())) {
                        //请求成功
                        FundChangeVO fundChangeVO = (FundChangeVO) fundChangeResponse.getData();
                        if (!fundChangeVO.getRespCode().equals(TradeConstant.CLEARING_SUCCESS)) {
                            //业务处理失败
                            log.info("=================【megaPayTHB服务器回调方法信息记录】=================【上报清结算失败,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】");
                            RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                            rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                        }
                    } else {
                        log.info("=================【megaPayTHB服务器回调方法信息记录】=================【上报清结算失败,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】");
                        RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                        rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                    }
                } catch (Exception e) {
                    log.error("=================【megaPayTHB服务器回调方法信息记录】=================【上报清结算异常,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】", e);
                    RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                    rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                }
            } else {
                log.info("=================【megaPayTHB服务器回调方法信息记录】=================【订单支付成功后更新数据库失败】 orderId: {}", orders.getId());
            }
        } else {
            log.info("===========【megaPayTHB服务器回调方法信息记录】==============【订单已支付失败】 orderId: {}", orders.getId());
            orders.setTradeStatus(TradeConstant.ORDER_PAY_FAILD);
            //上游返回的错误code
            orders.setRemark4(megaPayServerCallbackDTO.getResult());
            //更改channelsOrders状态
            try {
                channelsOrderMapper.updateStatusById(orders.getId(), null, TradeConstant.TRADE_FALID);
            } catch (Exception e) {
                log.error("===========【megaPayTHB服务器回调方法信息记录】==============【更新通道订单异常】");
            }
            //计算支付失败时通道网关手续费
            commonService.calcCallBackGatewayFeeFailed(orders);
            log.info("-------------megaPayTHB服务器回调方法信息记录------------订单已支付失败");
            if (ordersMapper.updateByExampleSelective(orders, example) == 1) {
                log.info("=================【megaPayTHB服务器回调方法信息记录】=================【订单支付失败后更新数据库成功】 orderId: {}", orders.getId());
            } else {
                log.info("=================【megaPayTHB服务器回调方法信息记录】=================【订单支付失败后更新数据库失败】 orderId: {}", orders.getId());
            }
        }
        //商户服务器回调地址不为空,回调商户服务器
        if (!StringUtils.isEmpty(orders.getReturnUrl())) {
            try {
                commonService.replyReturnUrl(orders);
            } catch (Exception e) {
                log.error("===========【megaPayTHB服务器回调方法信息记录】==============回调商户服务器异常", e);
            }
        }
    }

    /**
     * MegaPayTHB浏览器回调方法
     *
     * @param megaPayBrowserCallbackDTO megaPay回调参数
     * @return
     */
    @Override
    public void megaPayThbBrowserCallback(MegaPayBrowserCallbackDTO megaPayBrowserCallbackDTO, HttpServletResponse response) {
        if (!StringUtils.isEmpty(megaPayBrowserCallbackDTO.getOrderID())) {
            if (megaPayBrowserCallbackDTO.getOrderID().startsWith("PGO")) {
                log.info("===========【megaPayTHB浏览器回调方法信息记录】==============这笔回调订单信息属于AD3 orderId:{}", megaPayBrowserCallbackDTO.getOrderID());
                String ad3Url = ad3ParamsConfig.getAd3ItsUrl().concat("megaPayToMerchant.do");
                log.info("----------------------回调信息分发AD3方法记录----------------------分发AD3URL:{},参数:{}", ad3Url, JSON.toJSONString(megaPayBrowserCallbackDTO));
                //分发给AD3
                cn.hutool.http.HttpResponse execute = HttpRequest.get(ad3Url)
                        .header(Header.CONTENT_TYPE, "application/x-www-form-urlencoded")
                        .form(BeanToMapUtil.beanToMap(megaPayBrowserCallbackDTO))
                        .timeout(20000)
                        .execute();
                String body = execute.body();
                log.info("----------------------回调信息分发AD3方法记录----------------------回调返回 body:{}", body);
                //判断HTTP状态码
                if (execute.getStatus() == AsianWalletConstant.HTTP_SUCCESS_STATUS) {
                    log.info("----------------------回调信息分发AD3方法记录----------------------分发AD3成功 http状态码:{}", execute.getStatus());
                    return;
                }
                log.info("----------------------回调信息分发AD3方法记录----------------------分发AD3失败 http状态码:{}", execute.getStatus());
                return;
            }
        }
        //查询原订单信息
        Orders orders = ordersMapper.selectByPrimaryKey(megaPayBrowserCallbackDTO.getOrderID());
        if (orders == null) {
            log.info("-------------megaPayTHB浏览器回调接口信息记录------------回调订单信息不存在");
            return;
        }
        if (orders.getTradeStatus().equals(TradeConstant.ORDER_PAY_SUCCESS)) {
            //支付成功
            if (!StringUtils.isEmpty(orders.getJumpUrl())) {
                log.info("-------------megaPayTHB浏览器回调接口信息记录------------开始回调商户");
                commonService.replyJumpUrl(orders, response);
            } else {
                try {
                    //返回支付成功页面
                    response.sendRedirect(ad3ParamsConfig.getPaySuccessUrl() + "?page=" + TradeConstant.PAGE_SUCCESS);
                } catch (IOException e) {
                    log.info("--------------megaPayTHB浏览器回调接口信息记录--------------调用AW支付成功页面失败", e);
                }
            }
        } else {
            //其他情况
            if (!StringUtils.isEmpty(orders.getJumpUrl())) {
                log.info("-------------megaPayTHB浏览器回调接口信息记录------------开始回调商户");
                commonService.replyJumpUrl(orders, response);
            } else {
                try {
                    //返回支付中页面
                    response.sendRedirect(ad3ParamsConfig.getPaySuccessUrl() + "?page=" + TradeConstant.PAGE_PROCESSING);
                } catch (IOException e) {
                    log.info("--------------megaPayTHB浏览器回调接口信息记录--------------调用AW支付中页面失败", e);
                }
            }
        }
    }


    /**
     * MegaPayIDR服务器回调方法
     *
     * @param megaPayIDRServerCallbackDTO megaPayIDR回调参数
     * @return
     */
    @Override
    public void megaPayIdrServerCallback(MegaPayIDRServerCallbackDTO megaPayIDRServerCallbackDTO, HttpServletRequest request, HttpServletResponse response) {
        //校验订单参数
        if (!checkIDRCallback(megaPayIDRServerCallbackDTO)) {
            return;
        }
        //查询通道MD5Key
        ChannelsOrder channelsOrder = channelsOrderMapper.selectByPrimaryKey(megaPayIDRServerCallbackDTO.getNp_inv());
        if (channelsOrder == null) {
            log.info("=================【megaPayIDR服务器回调方法信息记录】=================通道订单信息不存在");
            return;
        }
        //根据通道md5KeyStr获取交易状态
        String result = request.getParameter(channelsOrder.getMd5KeyStr());
        megaPayIDRServerCallbackDTO.setResult(result);//交易结果
        megaPayIDRServerCallbackDTO.setMd5KeyStr(channelsOrder.getMd5KeyStr());//md5Key
        if (megaPayIDRServerCallbackDTO.getNp_inv().startsWith("PGO")) {
            log.info("=================【megaPayIDR服务器回调方法信息记录】=================这笔回调订单信息属于AD3 orderId:{}", megaPayIDRServerCallbackDTO.getNp_inv());
            commonService.megaIDRCallbackAD3(megaPayIDRServerCallbackDTO, "nextPayRemitReturn.do");
            return;
        }
        //设置金额显示
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMinimumFractionDigits(3);//设置数值的小数部分允许的最小位数。
        numberFormat.setMaximumFractionDigits(2);//设置数值的小数部分允许的最大位数。
        numberFormat.setMaximumIntegerDigits(15);//设置数值的整数部分允许的最大位数。
        numberFormat.setMinimumIntegerDigits(1);//设置数值的整数部分允许的最小位数
        String responsePasswordKey = "111233";
        String sign = megaPayIDRServerCallbackDTO.getNp_refCode() + megaPayIDRServerCallbackDTO.getNp_inv() + responsePasswordKey + channelsOrder.getMd5KeyStr() + result + numberFormat.format(Double.valueOf(megaPayIDRServerCallbackDTO.getNp_amt()));
        log.info("=================【megaPayIDR服务器回调方法信息记录】=================签名前的明文: {}", sign);
        String signMsg = MD5.MD5Encode(sign);
        log.info("=================【megaPayIDR服务器回调方法信息记录】=================签名后的密文: {}", signMsg);
        //验签
        if (!org.apache.commons.lang.StringUtils.equals(sign, signMsg)) {
            log.info("=================【megaPayIDR服务器回调方法信息记录】=================签名不匹配");
            return;
        }
        //查询原订单信息
        Orders orders = ordersMapper.selectByPrimaryKey(megaPayIDRServerCallbackDTO.getNp_inv());
        if (orders == null) {
            log.info("=================【megaPayIDR服务器回调方法信息记录】=================回调订单信息不存在");
            return;
        }
        //校验订单状态
        if (!TradeConstant.ORDER_PAYING.equals(orders.getTradeStatus())) {
            log.info("=================【megaPayIDR服务器回调方法信息记录】=================【订单状态不为支付中】");
            return;
        }
        //校验订单信息
        if (new BigDecimal(megaPayIDRServerCallbackDTO.getNp_amt()).compareTo(orders.getTradeAmount()) != 0) {
            log.info("=================【megaPayIDR服务器回调方法信息记录】=================【订单信息不匹配】");
            return;
        }
        orders.setUpdateTime(new Date());//修改时间
        orders.setChannelCallbackTime(new Date());//通道回调时间
        Example example = new Example(Orders.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("tradeStatus", "2");
        criteria.andEqualTo("id", orders.getId());
        if ("000".equals(megaPayIDRServerCallbackDTO.getResult())) {
            log.info("=================【megaPayIDR服务器回调方法信息记录】=================【订单已支付成功】 orderId: {}", orders.getId());
            //支付成功
            orders.setTradeStatus(TradeConstant.ORDER_PAY_SUCCESS);
            //更改channelsOrders状态
            try {
                channelsOrderMapper.updateStatusById(orders.getId(), null, TradeConstant.TRADE_SUCCESS);
            } catch (Exception e) {
                log.error("=================【megaPayIDR服务器回调方法信息记录】=================【更新通道订单异常】", e);
            }
            //修改原订单状态
            int i = ordersMapper.updateByExampleSelective(orders, example);
            if (i > 0) {
                log.info("=================【megaPayIDR服务器回调方法信息记录】=================【订单支付成功后更新数据库成功】 orderId: {}", orders.getId());
                //计算支付成功时的通道网关手续费
                commonService.calcCallBackGatewayFeeSuccess(orders);
                //添加日交易限额与日交易笔数
                commonService.quota(orders.getInstitutionCode(), orders.getProductCode(), orders.getTradeAmount());
                //新增交易成功的订单物流信息
                commonService.insertOrderLogistics(orders);
                //支付成功后向用户发送邮件
                commonService.sendEmail(orders.getDraweeEmail(), orders.getLanguage(), Status._1, orders);
                try {
                    //账户信息不存在的场合创建对应的账户信息
                    if (commonService.getAccount(orders.getInstitutionCode(), orders.getOrderCurrency()) == null) {
                        log.info("=================【megaPayIDR服务器回调方法信息记录】=================【上报清结算前线下下单创建账户信息】");
                        commonService.createAccount(orders.getInstitutionCode(), orders.getOrderCurrency());
                    }
                    //分润
                    if(!StringUtils.isEmpty(orders.getAgencyCode())){
                        rabbitMQSender.send(AD3MQConstant.MQ_FR_DL, orders.getId());
                    }
                    //更新成功,上报清结算
                    FundChangeDTO fundChangeDTO = new FundChangeDTO(orders, TradeConstant.NT, orders.getInstitutionCode());
                    //上报清结算资金变动接口
                    BaseResponse fundChangeResponse = clearingService.fundChange(fundChangeDTO, null);
                    if (fundChangeResponse.getCode() != null && TradeConstant.HTTP_SUCCESS.equals(fundChangeResponse.getCode())) {
                        //请求成功
                        FundChangeVO fundChangeVO = (FundChangeVO) fundChangeResponse.getData();
                        if (!fundChangeVO.getRespCode().equals(TradeConstant.CLEARING_SUCCESS)) {
                            //业务处理失败
                            log.info("=================【megaPayIDR服务器回调方法信息记录】=================【上报清结算失败,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】");
                            RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                            rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                        }
                    } else {
                        log.info("=================【megaPayIDR服务器回调方法信息记录】=================【上报清结算失败,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】");
                        RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                        rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                    }
                } catch (Exception e) {
                    log.error("=================【megaPayIDR服务器回调方法信息记录】=================【上报清结算异常,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】", e);
                    RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                    rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                }
            } else {
                log.info("=================【megaPayIDR服务器回调方法信息记录】=================【订单支付成功后更新数据库失败】 orderId: {}", orders.getId());
            }
        } else {
            log.info("=================【megaPayIDR服务器回调方法信息记录】=================订单已支付失败");
            //支付失败
            orders.setTradeStatus(TradeConstant.ORDER_PAY_FAILD);
            //更改channelsOrders状态
            try {
                channelsOrderMapper.updateStatusById(orders.getId(), null, TradeConstant.TRADE_FALID);
            } catch (Exception e) {
                log.error("=================【megaPayIDR服务器回调方法信息记录】=================【更新通道订单异常】", e);
            }
            //计算支付失败时通道网关手续费
            commonService.calcCallBackGatewayFeeFailed(orders);
            if (ordersMapper.updateByExampleSelective(orders, example) == 1) {
                log.info("=================【megaPayIDR服务器回调方法信息记录】=================【订单支付失败后更新数据库成功】 orderId: {}", orders.getId());
            } else {
                log.info("=================【megaPayIDR服务器回调方法信息记录】=================【订单支付失败后更新数据库失败】 orderId: {}", orders.getId());
            }
        }
        //商户服务器回调地址不为空,回调商户服务器
        if (!StringUtils.isEmpty(orders.getReturnUrl())) {
            try {
                commonService.replyReturnUrl(orders);
            } catch (Exception e) {
                log.error("=================【megaPayIDR服务器回调方法信息记录】=================回调商户服务器异常", e);
            }
        }
    }

    /**
     * MegaPayIDR浏览器回调方法
     *
     * @param megaPayIDRBrowserCallbackDTO megaPayIDR回调参数
     * @return
     */
    @Override
    public void megaPayIdrBrowserCallback(MegaPayIDRBrowserCallbackDTO megaPayIDRBrowserCallbackDTO, HttpServletResponse response) {
        if (!StringUtils.isEmpty(megaPayIDRBrowserCallbackDTO.getE_inv())) {
            if (megaPayIDRBrowserCallbackDTO.getE_inv().startsWith("PGO")) {
                log.info("-------------megaPayIDR浏览器回调接口信息记录------------这笔回调订单信息属于AD3 orderId:{}", megaPayIDRBrowserCallbackDTO.getE_inv());
                commonService.callbackAD3(megaPayIDRBrowserCallbackDTO, "nextPayRemitToMerchant.do");
                return;
            }
        }
        //查询原订单信息
        Orders orders = ordersMapper.selectByPrimaryKey(megaPayIDRBrowserCallbackDTO.getE_inv());
        if (orders == null) {
            log.info("-------------megaPayIDR浏览器回调接口信息记录------------回调订单信息不存在");
            return;
        }
        if (orders.getTradeStatus().equals(TradeConstant.ORDER_PAY_SUCCESS)) {
            //支付成功
            if (!StringUtils.isEmpty(orders.getJumpUrl())) {
                log.info("-------------megaPayIDR浏览器回调接口信息记录------------开始回调商户");
                commonService.replyJumpUrl(orders, response);
            } else {
                try {
                    //返回支付成功页面
                    response.sendRedirect(ad3ParamsConfig.getPaySuccessUrl() + "?page=" + TradeConstant.PAGE_SUCCESS);
                } catch (IOException e) {
                    log.info("--------------megaPayIDR浏览器回调接口信息记录--------------调用AW支付成功页面失败", e);
                }
            }
        } else {
            //其他情况
            if (!StringUtils.isEmpty(orders.getJumpUrl())) {
                log.info("-------------megaPayIDR浏览器回调接口信息记录------------开始回调商户");
                commonService.replyJumpUrl(orders, response);
            } else {
                try {
                    //返回支付中页面
                    response.sendRedirect(ad3ParamsConfig.getPaySuccessUrl() + "?page=" + TradeConstant.PAGE_PROCESSING);
                } catch (IOException e) {
                    log.info("--------------megaPayIDR浏览器回调接口信息记录--------------调用AW支付中页面失败", e);
                }
            }
        }
    }

    /**
     * 校验megaPayTHB服务回调参数
     * @param megaPayServerCallbackDTO
     * @return
     */
    private boolean checkTHBCallback(MegaPayServerCallbackDTO megaPayServerCallbackDTO) {
        if (StringUtils.isEmpty(megaPayServerCallbackDTO.getInv())) {
            log.info("-------------megaPay回调方法信息记录------------订单id为空");
            return false;
        }
        return true;
    }

    /**
     * 校验megaPayIDR服务回调参数
     * @param megaPayIDRServerCallbackDTO
     * @return
     */
    private boolean checkIDRCallback(MegaPayIDRServerCallbackDTO megaPayIDRServerCallbackDTO) {
        if (StringUtils.isEmpty(megaPayIDRServerCallbackDTO.getNp_inv())) {
            log.info("-------------megaPay回调方法信息记录------------订单id为空");
            return false;
        }
        return true;
    }


    /**
     * nextPos回调方法
     *
     * @param nextPosCallbackDTO nextPos回调参数
     * @return
     */
    @Override
    public void nextPosCallback(NextPosCallbackDTO nextPosCallbackDTO, Orders orders, HttpServletResponse response) {
        //校验订单参数
        if (!checkNextPosCallback(nextPosCallbackDTO)) {
            return;
        }
        //金额转换
        String amt = nextPosCallbackDTO.getAmt();
        //格式化设置"#,##0.00"
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
        Double amt_d = new Double(amt);
        String amt2 = decimalFormat.format(amt_d);
        Base64 b64 = new Base64();
        byte[] asciiByteArr = b64.decode(nextPosCallbackDTO.getRefCode().getBytes());
        String respCode = asciiToString(asciiByteArr);
        log.info("================【NextPos回调】================ respCode: {}", respCode);
        String reqSign = respCode + nextPosCallbackDTO.getEinv() + nextPosCallbackDTO.getMerRespPassword() + nextPosCallbackDTO.getMerRespID()
                + nextPosCallbackDTO.getStatus() + amt2;//"M3S721622"//"H875247"
        log.info("================【NextPos回调】================ 签名前的明文: {}", reqSign);
        String sign = MD5.MD5Encode(reqSign).toUpperCase();
        log.info("================【NextPos回调】================ 签名后的密文: {}", sign);
        if (!sign.equals(nextPosCallbackDTO.getMark())) {
            log.info("================【NextPos回调】================【签名不匹配】");
            return;
        }
        if ("000000000000".equals(respCode)) {
            log.info("================【NextPos回调】================【有问题的transaction,联系NextPos】");
            return;
        }
        //校验订单信息
        if (new BigDecimal(nextPosCallbackDTO.getAmt()).compareTo(orders.getTradeAmount()) != 0) {
            log.info("================【NextPos回调】================【订单信息不匹配】");
            return;
        }
        orders.setChannelCallbackTime(new Date());//通道回调时间
        orders.setChannelNumber(nextPosCallbackDTO.getTransactionID());//通道流水号
        orders.setSign(respCode);//respCode
        orders.setUpdateTime(new Date());//修改时间
        Example example = new Example(Orders.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("tradeStatus", "2");
        criteria.andEqualTo("id", orders.getId());
        if ("000".equals(nextPosCallbackDTO.getStatus())) {
            log.info("================【NextPos回调】================【订单已支付成功】 orderId: {}", orders.getId());
            //支付成功
            orders.setTradeStatus(TradeConstant.ORDER_PAY_SUCCESS);
            //更改channelsOrders状态
            try {
                channelsOrderMapper.updateStatusById(orders.getId(), nextPosCallbackDTO.getTransactionID(), TradeConstant.TRADE_SUCCESS);
            } catch (Exception e) {
                log.error("================【NextPos回调】================【更新通道订单异常】", e);
            }
            //修改原订单状态
            int i = ordersMapper.updateByExampleSelective(orders, example);
            if (i > 0) {
                log.info("=================【NextPos回调】=================【订单支付成功后更新数据库成功】 orderId: {}", orders.getId());
                //计算支付成功时的通道网关手续费
                commonService.calcCallBackGatewayFeeSuccess(orders);
                //添加日交易限额与日交易笔数
                commonService.quota(orders.getInstitutionCode(), orders.getProductCode(), orders.getTradeAmount());
                //新增交易成功的订单物流信息
                commonService.insertOrderLogistics(orders);
                //支付成功后向用户发送邮件
                commonService.sendEmail(orders.getDraweeEmail(), orders.getLanguage(), Status._1, orders);
                try {
                    //账户信息不存在的场合创建对应的账户信息
                    if (commonService.getAccount(orders.getInstitutionCode(), orders.getOrderCurrency()) == null) {
                        log.info("=================【NextPos回调】=================【上报清结算前线下下单创建账户信息】");
                        commonService.createAccount(orders.getInstitutionCode(), orders.getOrderCurrency());
                    }
                    //分润
                    if(!StringUtils.isEmpty(orders.getAgencyCode())){
                        rabbitMQSender.send(AD3MQConstant.MQ_FR_DL, orders.getId());
                    }
                    //更新成功,上报清结算
                    FundChangeDTO fundChangeDTO = new FundChangeDTO(orders, TradeConstant.NT, orders.getInstitutionCode());
                    //上报清结算资金变动接口
                    BaseResponse fundChangeResponse = clearingService.fundChange(fundChangeDTO, null);
                    if (fundChangeResponse.getCode() != null && TradeConstant.HTTP_SUCCESS.equals(fundChangeResponse.getCode())) {
                        //请求成功
                        FundChangeVO fundChangeVO = (FundChangeVO) fundChangeResponse.getData();
                        if (!fundChangeVO.getRespCode().equals(TradeConstant.CLEARING_SUCCESS)) {
                            //业务处理失败
                            log.info("=================【NextPos回调】=================【上报清结算失败,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】");
                            RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                            rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                        }
                    } else {
                        log.info("=================【NextPos回调】=================【上报清结算失败,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】");
                        RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                        rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                    }
                } catch (Exception e) {
                    log.error("=================【NextPos回调】=================【上报清结算异常,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】", e);
                    RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                    rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                }
            } else {
                log.info("=================【NextPos回调】=================【订单支付成功后更新数据库失败】 orderId: {}", orders.getId());
            }
        } else if ("099".equals(nextPosCallbackDTO.getStatus())) {
            log.info("================【NextPos回调】================【订单已支付失败】 orderId: {}", orders.getId());
            orders.setTradeStatus(TradeConstant.ORDER_PAY_FAILD);
            //更改channelsOrders状态
            try {
                channelsOrderMapper.updateStatusById(orders.getId(), nextPosCallbackDTO.getTransactionID(), TradeConstant.TRADE_FALID);
            } catch (Exception e) {
                log.error("================【NextPos回调】================【更新通道订单异常】", e);
            }
            //计算支付失败时通道网关手续费
            commonService.calcCallBackGatewayFeeFailed(orders);
            if (ordersMapper.updateByExampleSelective(orders, example) == 1) {
                log.info("=================【NextPos回调】=================【订单支付失败后更新数据库成功】 orderId: {}", orders.getId());
            } else {
                log.info("=================【NextPos回调】=================【订单支付失败后更新数据库失败】 orderId: {}", orders.getId());
            }
        } else {
            log.info("================【NextPos回调】================【订单是其他状态】 orderId: {}", orders.getId());
        }
        try {
            //商户服务器回调地址不为空,回调商户服务器
            if (!StringUtils.isEmpty(orders.getReturnUrl())) {
                commonService.replyReturnUrl(orders);
            }
            response.getWriter().write("00");
        } catch (Exception e) {
            log.error("=================【NextPos回调】=================【回调商户异常】", e);
        }
    }

    /**
     * 校验nextPos服务回调参数
     * @param nextPosCallbackDTO
     * @return
     */
    private boolean checkNextPosCallback(NextPosCallbackDTO nextPosCallbackDTO) {
        if (StringUtils.isEmpty(nextPosCallbackDTO.getAmt())) {
            log.info("-------------nextPost回调方法信息记录------------金额为空");
            return false;
        }
        if (StringUtils.isEmpty(nextPosCallbackDTO.getEinv())) {
            log.info("-------------nextPost回调方法信息记录------------订单id为空");
            return false;
        }
        if (StringUtils.isEmpty(nextPosCallbackDTO.getMark())) {
            log.info("-------------nextPost回调方法信息记录------------签名为空");
            return false;
        }
        if (StringUtils.isEmpty(nextPosCallbackDTO.getStatus())) {
            log.info("-------------nextPost回调方法信息记录------------交易结果为空");
            return false;
        }
        return true;
    }


    /**
     * 将byte[] 转字符串方法
     * @param value
     * @return
     */
    private static String asciiToString(byte[] value) {
        String result = "";
        for (int i = 0; i < value.length; i++) {
            result = result += (char) value[i];
        }
        return result;
    }

    /**
     * NextPos线下CSB处理方法
     *
     * @param orders       订单
     * @param channel      通道
     * @param baseResponse 通用响应实体
     * @return 通用响应实体
     */
    @Override
    public BaseResponse offlineCSB(Orders orders, Channel channel, BaseResponse baseResponse) {
        NextPosRequestDTO nextPosRequestDTO = new NextPosRequestDTO(orders, channel, ad3ParamsConfig.getChannelCallbackUrl().concat("/onlinecallback/nextPosCallback"));
        log.info("-----------------【线下CSB】下单信息记录--------------调用Channels服务【NextPos-CSB接口】-请求实体 nextPosRequestDTO: {}", JSON.toJSONString(nextPosRequestDTO));
        BaseResponse response = channelsFeign.nextPos(nextPosRequestDTO);
        log.info("-----------------【线下CSB】下单信息记录--------------调用Channels服务【NextPos-CSB接口】-响应结果 response: {}", JSON.toJSONString(response));
        //状态码不为200的时候
        if (!TradeConstant.HTTP_SUCCESS.equals(response.getCode())) {
            log.info("-----------------【线下CSB】下单信息记录--------------调用Channels服务【NextPos-CSB接口】-状态码异常 code: {}", response.getCode());
            //订单创建失败
            baseResponse.setCode(EResultEnum.ORDER_CREATION_FAILED.getCode());
            return baseResponse;
        }
        baseResponse.setData(response.getData());//数据
        return baseResponse;
    }

}
