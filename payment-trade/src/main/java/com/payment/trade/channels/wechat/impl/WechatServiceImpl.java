package com.payment.trade.channels.wechat.impl;
import com.alibaba.fastjson.JSON;
import com.payment.common.constant.AD3MQConstant;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.constant.TradeConstant;
import com.payment.common.dto.FundChangeDTO;
import com.payment.common.dto.wechat.WechaRefundDTO;
import com.payment.common.dto.wechat.WechatBSCDTO;
import com.payment.common.dto.wechat.WechatCSBDTO;
import com.payment.common.dto.wechat.WechatQueryDTO;
import com.payment.common.entity.*;
import com.payment.common.enums.Status;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.common.utils.ArrayUtil;
import com.payment.common.utils.SignTools;
import com.payment.common.utils.XMLUtil;
import com.payment.common.vo.FundChangeVO;
import com.payment.trade.channels.wechat.WechatService;
import com.payment.trade.config.AD3ParamsConfig;
import com.payment.trade.dao.*;
import com.payment.trade.feign.ChannelsFeign;
import com.payment.trade.rabbitmq.RabbitMQSender;
import com.payment.trade.service.ClearingService;
import com.payment.trade.service.CommonService;
import com.payment.trade.utils.AbstractHandlerAdapter;
import com.payment.trade.utils.HandlerType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

@Service
@Slf4j
@HandlerType({TradeConstant.WECHAT_CSB_OFFLINE, TradeConstant.WECHAT_BSC_OFFLINE})
public class WechatServiceImpl extends AbstractHandlerAdapter implements WechatService {

    @Autowired
    private ChannelsFeign channelsFeign;

    @Autowired
    private AD3ParamsConfig ad3ParamsConfig;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private ChannelsOrderMapper channelsOrderMapper;

    @Autowired
    private ChannelMapper channelMapper;

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


    /**
     * 微信退款接口 上报清结算
     *
     * @param orderRefund   订单
     * @param fundChangeDTO
     * @param baseResponse
     * @return
     */
    @Override
    public BaseResponse wechatRefund(OrderRefund orderRefund, FundChangeDTO fundChangeDTO, BaseResponse baseResponse) {
        Channel channel = channelMapper.selectByChannelCode(orderRefund.getChannelCode());
        WechaRefundDTO wechaRefundDTO = new WechaRefundDTO(orderRefund, channel);
        BaseResponse response = channelsFeign.wechatRefund(wechaRefundDTO);
        if (response.getCode().equals("200")) {//请求成功
            Map<String, String> map = (Map<String, String>) response.getData();
            if (map != null && map.get("return_code") != null && map.get("return_code").equals("SUCCESS")
                    && map.get("result_code") != null && !map.get("result_code").equals("") && map.get("result_code").equals("SUCCESS")) {
                //退款成功
                log.info("-----------------  wechat退款方法 -------------- refundAdResponseVO : {} ", JSON.toJSON(response));
                //退款成功
                refundOrderMapper.updateStatuts(orderRefund.getId(), TradeConstant.REFUND_SUCCESS, map.get("refund_id"), null);
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
                log.info("----------------- 退款操作 退款失败 -------------- refundAdResponseVO : {} ", JSON.toJSON(response));
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

                if (!cFundChange.getCode().equals(String.valueOf(AsianWalletConstant.HTTP_SUCCESS_STATUS))) {//请求失败
                    orderRefund.setChannelNumber(map.get("refund_id"));
                    orderRefund.setRemark3(JSON.toJSONString(fundChangeDTO));
                    RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orderRefund));
                    log.info("----------------- 退款操作 上报队列 MQ_QJS_TZSB_DL -------------- rabbitMassage : {} ", JSON.toJSON(rabbitMassage));
                    rabbitMQSender.send(AD3MQConstant.MQ_QJS_TZSB_DL, JSON.toJSONString(rabbitMassage));
                } else {//请求成功
                    FundChangeVO fundChangeVO = (FundChangeVO) cFundChange.getData();
                    if (fundChangeVO.getRespCode().equals(TradeConstant.CLEARING_SUCCESS)) {
                        refundOrderMapper.updateStatuts(orderRefund.getId(), TradeConstant.REFUND_FALID, map.get("refund_id"), fundChangeVO.getRespMsg());
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
                        orderRefund.setChannelNumber(map.get("refund_id"));
                        orderRefund.setRemark3(JSON.toJSONString(fundChangeDTO));
                        RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orderRefund));
                        log.info("----------------- 退款操作 上报队列 MQ_QJS_TZSB_DL -------------- rabbitMassage : {} ", JSON.toJSON(rabbitMassage));
                        rabbitMQSender.send(AD3MQConstant.MQ_QJS_TZSB_DL, JSON.toJSONString(rabbitMassage));
                    }
                }
            }
        } else {
            //请求失败
            baseResponse.setMsg(EResultEnum.REFUNDING.getCode());
            orderRefund.setRemark3(JSON.toJSONString(fundChangeDTO));
            RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orderRefund));
            log.info("----------------- 退款操作 请求失败上报队列 MQ_TK_WECHAT_QQSB_DL -------------- orderRefund : {} ", JSON.toJSON(rabbitMassage));
            rabbitMQSender.send(AD3MQConstant.MQ_TK_WECHAT_QQSB_DL, JSON.toJSONString(rabbitMassage));

        }
        return response;
    }

    /**
     * 微信退款接口 不上报清结算
     *
     * @param orders 订单
     * @return
     */
    @Override
    public void wechatRefund2(Orders orders, RabbitMassage rabbitMassage) {
        Channel channel = channelMapper.selectByChannelCode(orders.getChannelCode());
        WechaRefundDTO wechaRefundDTO = new WechaRefundDTO(orders, channel);
        BaseResponse response = channelsFeign.wechatRefund(wechaRefundDTO);
        if (response.getCode().equals("200")) {//请求成功
            Map<String, String> map = (Map<String, String>) response.getData();
            if (map != null && map.get("return_code") != null && map.get("return_code").equals("SUCCESS")
                    && map.get("result_code") != null && !map.get("result_code").equals("") && map.get("result_code").equals("SUCCESS")) {
                //退款成功
                log.info("-----------------  wechat退款方法 -------------- refundAdResponseVO : {} ", JSON.toJSON(response));
                ordersMapper.updateOrderCancelStatus(orders.getInstitutionOrderId(), orders.getDeviceOperator(), TradeConstant.ORDER_CANNEL_SUCCESS);
            } else {
                //退款失败
                ordersMapper.updateOrderCancelStatus(orders.getInstitutionOrderId(), orders.getDeviceOperator(), TradeConstant.ORDER_CANNEL_FALID);
            }
        } else {
            //请求失败
            log.info("----------------- 退款操作 请求失败上报队列 MQ_TK_WECHAT_QQSB_DL -------------- orderRefund : {} ", JSON.toJSON(rabbitMassage));
            rabbitMQSender.sendAd3Sleep(AD3MQConstant.MQ_AD3_REFUND, JSON.toJSONString(rabbitMassage));

        }
    }

    /**
     * 微信撤销接口
     *
     * @param orderRefund  订单
     * @param baseResponse
     * @return
     */
    @Override
    public BaseResponse wechatCancel(OrderRefund orderRefund, BaseResponse baseResponse, RabbitMassage rabbitMassage) {
        Channel channel = channelMapper.selectByChannelCode(orderRefund.getChannelCode());
        WechaRefundDTO wechaRefundDTO = new WechaRefundDTO(orderRefund, channel);
        BaseResponse response = channelsFeign.wechatRefund(wechaRefundDTO);
        if (response.getCode().equals("200")) {//请求成功
            Map<String, String> map = (Map<String, String>) response.getData();
            if (map != null && map.get("return_code") != null && map.get("return_code").equals("SUCCESS")
                    && map.get("result_code") != null && !map.get("result_code").equals("") && map.get("result_code").equals("SUCCESS")) {
                //退款成功
                log.info("-----------------  Wechat撤销方法 -------------- response : {} ", JSON.toJSON(response));
                refundOrderMapper.updateStatuts(orderRefund.getId(), TradeConstant.REFUND_SUCCESS, map.get("refund_id"), null);
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
            } else { //退款失败
                log.info("----------------- 撤销操作 上游退款失败 -------------- response : {}", JSON.toJSON(response));
                //退款失败
                baseResponse.setMsg(EResultEnum.REFUNDING.getCode());
                rabbitMQSender.send(AD3MQConstant.E_MQ_CX_TDTKSB_DL, JSON.toJSONString(rabbitMassage));
            }
        } else {
            log.info("----------------- 撤销操作 请求失败 -------------- response : {}", JSON.toJSON(response));
            baseResponse.setMsg(EResultEnum.REFUNDING.getCode());
            rabbitMQSender.send(AD3MQConstant.E_MQ_CX_TDTKSB_DL, JSON.toJSONString(rabbitMassage));
        }
        return baseResponse;
    }

    /**
     * wechatCSB收单方法
     *
     * @param orders  订单
     * @param channel 通道
     * @return
     */
    @Override
    public BaseResponse wechatCSB(Orders orders, Channel channel, BaseResponse baseResponse) {
        WechatCSBDTO wechatCSBDTO = new WechatCSBDTO(orders, channel, ad3ParamsConfig.getChannelCallbackUrl().concat("/offlineCallback").concat("/wechatCSBCallback"));
        log.info("==================wechatCSB收单方法==================wechatCSBDTO:{}", JSON.toJSONString(wechatCSBDTO));
        BaseResponse response = channelsFeign.wechatOfflineCSB(wechatCSBDTO);
        if (response.getData() == null) {
            baseResponse.setCode(EResultEnum.ORDER_CREATION_FAILED.getCode());
            return baseResponse;
        }
        baseResponse.setData(response.getData());
        log.info("==================wechatCSB收单方法 返回==================baseResponse:{}", JSON.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * wechatCSB回调
     *
     * @return
     */
    @Override
    public void wechatCSBCallback(HttpServletRequest request, HttpServletResponse response) {
        try {
            request.setCharacterEncoding("UTF-8");
            StringBuffer xmlStr = new StringBuffer();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                xmlStr.append(line);
            }
            String xmlCallback = String.valueOf(xmlStr);
            log.info("==============【微信CSB服务器回调】==============【xml格式回调参数】 xmlCallback:{}", xmlCallback);
            if (StringUtils.isEmpty(xmlCallback)) {
                log.info("==============【微信CSB服务器回调】==============回调参数为空");
                return;
            }
            //解析xml
            Map<String, String> map = XMLUtil.xml2MapForWeChat(xmlCallback);
            log.info("==============【微信CSB服务器回调】==============【解析后的xml格式回调参数】 map:{}", map);
            if (ArrayUtil.isEmpty(map)) {
                log.info("==============【微信CSB服务器回调】==============【解析后的map参数为空】");
                return;
            }
            //订单id
            String orderId = map.get("out_trade_no");
            //查询md5Key
            ChannelsOrder channelsOrder = channelsOrderMapper.selectByPrimaryKey(orderId);
            if (channelsOrder == null) {
                log.info("==============【微信CSB服务器回调】==============【通道订单信息为空】");
                return;
            }
            //查询原订单信息
            Orders orders = ordersMapper.selectByPrimaryKey(orderId);
            if (orders == null) {
                log.info("==============【微信CSB服务器回调】==============【原订单信息为空】");
                return;
            }
            if (StringUtils.isNotBlank(map.get("return_code")) && "SUCCESS".equals(map.get("return_code"))) {
                //校验签名
                String apikey = channelsOrder.getMd5KeyStr();
                String wxSign = map.get("sign");
                map.remove("sign");
                String signTemp = SignTools.getWXSignStr(map);
                String mySign = SignTools.getWXSign_MD5(signTemp, apikey);
                if (wxSign.equals(mySign)) {
                    String transactionId = map.get("transaction_id");//通道流水号
                    orders.setChannelNumber(transactionId);//通道流水号
                    orders.setChannelCallbackTime(new Date());//通道回调时间
                    orders.setUpdateTime(new Date());//修改时间
                    Example example = new Example(Orders.class);
                    Example.Criteria criteria = example.createCriteria();
                    criteria.andEqualTo("id", orders.getId());
                    criteria.andEqualTo("tradeStatus", "2");
                    if (StringUtils.isNotBlank(map.get("return_code")) && map.get("result_code").equals("SUCCESS")) {
                        log.info("==============【微信CSB服务器回调】==============订单已支付成功 orderId:{}", orderId);
                        orders.setTradeStatus(TradeConstant.ORDER_PAY_SUCCESS);
                        //更改channelsOrders状态
                        try {
                            channelsOrderMapper.updateStatusById(orderId, transactionId, TradeConstant.TRADE_SUCCESS);
                        } catch (Exception e) {
                            log.error("==============【微信CSB服务器回调】==============【更新通道订单异常】", e);
                        }
                        //修改订单状态
                        int i = ordersMapper.updateByExampleSelective(orders, example);
                        if (i > 0) {
                            log.info("=================【微信CSB服务器回调】=================【订单支付成功后更新数据库成功】 orderId: {}", orders.getId());
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
                                    log.info("=================【微信CSB服务器回调】=================【上报清结算前线下下单创建账户信息】");
                                    commonService.createAccount(orders.getInstitutionCode(), orders.getOrderCurrency());
                                }
                                //分润
                                if(!org.springframework.util.StringUtils.isEmpty(orders.getAgencyCode())){
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
                                        log.info("=================【微信CSB服务器回调】=================【上报清结算失败,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】");
                                        RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                                        rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                                    }
                                } else {
                                    log.info("=================【微信CSB服务器回调】=================【上报清结算失败,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】");
                                    RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                                    rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                                }
                            } catch (Exception e) {
                                log.error("=================【微信CSB服务器回调】=================【上报清结算异常,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】", e);
                                RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                                rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                            }
                        } else {
                            log.info("=================【微信CSB服务器回调】=================【订单支付成功后更新数据库失败】 orderId: {}", orders.getId());
                        }
                        //系统业务逻辑处理完成后需要给微信返回消息表示接受成功
                        String resultMsg = "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml> ";
                        PrintWriter out = response.getWriter();
                        out.write(resultMsg);
                        out.flush();
                        out.close();
                    } else {
                        log.info("==============【微信CSB服务器回调】==============【订单已支付失败】 orderId:{}", orderId);
                        orders.setTradeStatus(TradeConstant.ORDER_PAY_FAILD);
                        //上游返回的错误code
                        orders.setRemark4(map.get("result_code"));
                        //更改channelsOrders状态
                        try {
                            channelsOrderMapper.updateStatusById(orderId, transactionId, TradeConstant.TRADE_FALID);
                        } catch (Exception e) {
                            log.error("==============【微信CSB服务器回调】==============【更新通道订单异常】", e);
                        }
                        if (ordersMapper.updateByExampleSelective(orders, example) == 1) {
                            log.info("=================【微信CSB服务器回调】=================【订单支付失败后更新数据库成功】 orderId: {}", orders.getId());
                        } else {
                            log.info("=================【微信CSB服务器回调】=================【订单支付失败后更新数据库失败】 orderId: {}", orders.getId());
                        }
                    }
                } else {
                    log.info("==============【微信CSB服务器回调】==============【签名不匹配】");
                }
            } else {
                log.info("==============【微信CSB服务器回调】==============【返回return_code参数为失败】");
            }
        } catch (Exception e) {
            log.error("==============【微信CSB服务器回调】==============【接口异常】", e);
        }
    }


    /**
     * @return
     * @Author YangXu
     * @Date 2019/6/28
     * @Descripate 撤销 处于退款中时
     **/
    @Override
    public void cancelRefunding(Orders orders, RabbitMassage rabbitMassage) {

        Channel channel = channelMapper.selectByChannelCode(orders.getChannelCode());
        WechatQueryDTO wechatQueryDTO = new WechatQueryDTO(orders.getId(), channel);
        BaseResponse baseResponse = channelsFeign.wechatQuery(wechatQueryDTO);
        if (baseResponse.getCode().equals(TradeConstant.HTTP_SUCCESS)) {//请求成功
            Map<String, String> map = (Map<String, String>) baseResponse.getData();
            if (map.get("return_code").equals("SUCCESS")) {
                //请求成功
                if (map.get("result_code").equals("SUCCESS") && map.get("trade_state").equals("SUCCESS")) {
                    //交易成功
                    orders.setTradeStatus(TradeConstant.ORDER_PAY_SUCCESS);//付款成功
                    //更新订单状态
                    if (ordersMapper.updateOrderByAd3Query(orders.getId(), TradeConstant.ORDER_PAY_SUCCESS,
                            orders.getChannelNumber(), orders.getChannelCallbackTime()) == 1) {//更新成功
                        RabbitMassage rabbitRefundMsg = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                        this.wechatRefund2(orders, rabbitRefundMsg);
                    } else {//更新失败后去查询订单信息
                        RabbitMassage rabbitOrderMsg = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                        rabbitMQSender.send(AD3MQConstant.TC_MQ_CANCEL_ORDER, JSON.toJSONString(rabbitOrderMsg));
                    }

                } else {
                    //交易失败
                    log.info("****************  支付失败 ***************** rabbitMassage : {} ", JSON.toJSONString(rabbitMassage));
                    orders.setTradeStatus(TradeConstant.ORDER_PAY_FAILD);//付款失败
                    ordersMapper.updateOrderByAd3Query(orders.getId(), TradeConstant.ORDER_PAY_FAILD, orders.getChannelNumber(), orders.getChannelCallbackTime());
                }
            } else {
                //请求失败
                log.info("**************** 订单处于退款中时 Wechat 查询订单失败 ***************** rabbitMassage : {} ", JSON.toJSONString(rabbitMassage));
                rabbitMQSender.sendAd3Sleep(AD3MQConstant.E_MQ_AD3_ORDER_QUERY, JSON.toJSONString(rabbitMassage));
            }
        } else {
            //请求失败
            log.info("**************** 订单处于退款中时 Wechat 查询订单失败 ***************** rabbitMassage : {} ", JSON.toJSONString(rabbitMassage));
            rabbitMQSender.sendAd3Sleep(AD3MQConstant.E_MQ_AD3_ORDER_QUERY, JSON.toJSONString(rabbitMassage));
        }


    }


    /**
     * wechatCSB收单方法
     *
     * @param orders  订单
     * @param channel 通道
     * @return
     */
    @Override
    public BaseResponse offlineCSB(Orders orders, Channel channel, BaseResponse baseResponse) {
        WechatCSBDTO wechatCSBDTO = new WechatCSBDTO(orders, channel, ad3ParamsConfig.getChannelCallbackUrl().concat("/offlineCallback").concat("/wechatCSBCallback"));
        log.info("==================【线下CSB】下单信息记录==================调用Channels服务【Wechat-CSB接口】-请求实体  aliPayCSBDTO: {}", JSON.toJSONString(wechatCSBDTO));
        BaseResponse response = channelsFeign.wechatOfflineCSB(wechatCSBDTO);
        log.info("==================【线下CSB】下单信息记录==================调用Channels服务【Wechat-CSB接口】-响应结果  response:{}", response);
        if (TradeConstant.HTTP_SUCCESS.equals(response.getCode())) {
            log.info("==================【线下CSB】下单信息记录==================调用Channels服务【Wechat-CSB接口】-状态码异常 code: {}", response.getCode());
            baseResponse.setCode(EResultEnum.ORDER_CREATION_FAILED.getCode());
            return baseResponse;
        }
        baseResponse.setData(response.getData());
        return baseResponse;
    }

    /**
     * wechatBSC收单方法
     *
     * @param orders  订单
     * @param channel 通道
     * @return
     */
    @Override
    public BaseResponse offlineBSC(Orders orders, Channel channel, BaseResponse baseResponse, String authCode) {
        WechatBSCDTO wechatBSCDTO = new WechatBSCDTO(orders, channel, authCode);
        log.info("==================【线下BSC】下单信息记录==================调用Channels服务【Wechat-BSC接口】-请求实体  aliPayCSBDTO: {}", JSON.toJSONString(wechatBSCDTO));
        BaseResponse response = channelsFeign.wechatOfflineBSC(wechatBSCDTO);
        log.info("==================【线下BSC】下单信息记录==================调用Channels服务【Wechat-BSC接口】-响应结果  response:{}", JSON.toJSONString(response));
        //支付失败时
        if (!TradeConstant.HTTP_SUCCESS.equals(response.getCode())) {
            log.info("==================【线下BSC】下单信息记录==================调用Channels服务【Wechat-BSC接口】-状态码异常 code: {}", response.getCode());
            //创建订单失败
            baseResponse.setCode(EResultEnum.ORDER_CREATION_FAILED.getCode());
            return baseResponse;
        }
        orders.setChannelCallbackTime(new Date());//通道回调时间
        orders.setChannelNumber(String.valueOf(response.getData()));//通道流水号
        orders.setUpdateTime(new Date());//修改时间
        Example example = new Example(Orders.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("tradeStatus", "2");
        criteria.andEqualTo("id", orders.getId());
        if (response.getMsg().equals(TradeConstant.HTTP_SUCCESS_MSG)) {
            log.info("==================【线下BSC】下单信息记录==================订单已支付成功 orderId:{}", orders.getId());
            orders.setTradeStatus(TradeConstant.ORDER_PAY_SUCCESS);
            //更改channelsOrders状态
            try {
                channelsOrderMapper.updateStatusById(orders.getId(), String.valueOf(response.getData()), TradeConstant.TRADE_SUCCESS);
            } catch (Exception e) {
                log.error("==================【线下BSC】下单信息记录==================【通道订单更新异常】", e);
            }
            //修改订单状态
            int i = ordersMapper.updateByExampleSelective(orders, example);
            if (i > 0) {
                log.info("=================【线下BSC】下单信息记录=================【订单支付成功后更新数据库成功】 orderId: {}", orders.getId());
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
                        log.info("=================【线下BSC】下单信息记录=================【上报清结算前线下下单创建账户信息】");
                        commonService.createAccount(orders.getInstitutionCode(), orders.getOrderCurrency());
                    }
                    //分润
                    if(!org.springframework.util.StringUtils.isEmpty(orders.getAgencyCode())){
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
                            log.info("=================【线下BSC】下单信息记录=================【上报清结算失败,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】");
                            RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                            rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                        }
                    } else {
                        log.info("=================【线下BSC】下单信息记录=================【上报清结算失败,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】");
                        RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                        rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                    }
                } catch (Exception e) {
                    log.error("=================【线下BSC】下单信息记录=================【上报清结算异常,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】", e);
                    RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                    rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                }
            } else {
                log.info("=================【线下BSC】下单信息记录=================【订单支付成功后更新数据库失败】 orderId: {}", orders.getId());
            }
        } else if (TradeConstant.HTTP_FAIL_MSG.equals(response.getMsg())) {
            log.info("==================【线下BSC】下单信息记录==================订单已支付失败 orderId:{}", orders.getId());
            orders.setTradeStatus(TradeConstant.ORDER_PAY_FAILD);
            //上游返回的错误code
            orders.setRemark4(response.getMsg());
            try {
                //更改channelsOrders状态
                channelsOrderMapper.updateStatusById(orders.getId(), String.valueOf(response.getData()), TradeConstant.TRADE_FALID);
            } catch (Exception e) {
                log.error("==================【线下BSC】下单信息记录==================【通道订单更新异常】", e);
            }
            if (ordersMapper.updateByExampleSelective(orders, example) == 1) {
                log.info("=================【线下BSC】下单信息记录=================【订单支付失败后更新数据库成功】 orderId: {}", orders.getId());
            } else {
                log.info("=================【线下BSC】下单信息记录=================【订单支付失败后更新数据库失败】 orderId: {}", orders.getId());
            }
            //返回msg信息支付异常
            baseResponse.setCode(EResultEnum.PAYMENT_ABNORMAL.getCode());
        } else {
            log.info("==================【线下BSC】下单信息记录==================其他情况 正常不会发生");
            baseResponse.setCode(EResultEnum.PAYMENT_ABNORMAL.getCode());
        }
        return baseResponse;
    }
}
