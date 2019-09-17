package com.payment.trade.rabbitmq;

import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.payment.common.constant.AD3MQConstant;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.constant.TradeConstant;
import com.payment.common.dto.FundChangeDTO;
import com.payment.common.entity.Orders;
import com.payment.common.entity.RabbitMassage;
import com.payment.common.enums.Status;
import com.payment.common.response.BaseResponse;
import com.payment.common.utils.MD5;
import com.payment.common.utils.XMLUtil;
import com.payment.common.vo.FundChangeVO;
import com.payment.trade.dao.ChannelsOrderMapper;
import com.payment.trade.dao.OrdersMapper;
import com.payment.trade.feign.MessageFeign;
import com.payment.trade.service.ClearingService;
import com.payment.trade.service.CommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @description: NGANLUONG通道队列
 * @author: YangXu
 * @create: 2019-06-18 15:23
 **/
@Component
@Slf4j
public class NganLuongMQRecive {

    @Value("${custom.nganLuong.merchant_id}")
    private String merchantId;

    @Value("${custom.nganLuong.merchant_password}")
    private String merchantPassword;

    @Value("${custom.nganLuong.check_url}")
    private String checkUrl;

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
    private MessageFeign messageFeign;

    @Value("${custom.developer.mobile}")
    private String developerMobile;

    @Value("${custom.developer.email}")
    private String developerEmail;

    @RabbitListener(queues = "MQ_NGANLUONG_CHECK_ORDER_DL")
    public void processNganLuongCheckOrder(String value) {
        RabbitMassage rabbitMassage = JSON.parseObject(value, RabbitMassage.class);
        if (rabbitMassage.getCount() > 0) {
            //请求次数减一
            rabbitMassage.setCount(rabbitMassage.getCount() - 1);
            log.info("-----------------【NL查询队列】--------------value: {}", value);
            Map<String, Object> map = new HashMap<>();
            map.put("merchant_id", merchantId);
            map.put("merchant_password", MD5.MD5Encode(merchantPassword));
            map.put("version", "3.1");
            map.put("function", "GetTransactionDetail");
            map.put("token", rabbitMassage.getValue());
            log.info("-------【NL查询队列】------- 查询参数记录 map:{}", JSON.toJSONString(map));
            cn.hutool.http.HttpResponse execute = HttpRequest.post(checkUrl)
                    .header(Header.CONTENT_TYPE, "application/x-www-form-urlencoded")
                    .form(map)
                    .timeout(30000)
                    .execute();
            int status = execute.getStatus();
            //判断HTTP状态码
            if (status != AsianWalletConstant.HTTP_SUCCESS_STATUS) {
                log.info("-------------【NL查询队列】------------ 调用查询接口状态码异常 status:{}", status);
                return;
            }
            String body = execute.body();
            log.info("----------------------【NL查询队列】----------------------body:{}", body);
            // 注解方式xml转换为map对象
            if (StringUtils.isEmpty(body)) {
                log.info("-------------【NL查询队列】------------调用查询接口返回body为空");
                return;
            }
            Map<String, String> resultMap = null;
            try {
                resultMap = XMLUtil.xmlToMap(body, "UTF-8");
                log.info("-------------【NL查询队列】------------ 解析后的XML结果 resultMap:{}", JSON.toJSONString(resultMap));
            } catch (Exception e) {
                log.error("----------------------【NL查询队列】----------------------xml转换异常", e);
            }
            String transactionStatus = resultMap.get("transaction_status");
            String token = resultMap.get("token");
            String errorCode = resultMap.get("error_code");
            String orderCode = resultMap.get("order_code");
            String totalAmount = resultMap.get("total_amount");
            String transactionId = resultMap.get("transaction_id");
            //校验参数
            if (StringUtils.isEmpty(transactionStatus) || !"00".equals(errorCode)) {
                log.info("-----------------【NL查询队列】-------------回调参数有错误 继续上报查询订单队列 E_MQ_NGANLUONG_CHECK_ORDER_DL -------------- token:{}", value);
                rabbitMQSender.send(AD3MQConstant.E_MQ_NGANLUONG_CHECK_ORDER_DL, JSON.toJSONString(rabbitMassage));
                return;
            }
            //查询原订单信息
            Orders orders = ordersMapper.selectByPrimaryKey(orderCode);
            if (orders == null) {
                log.info("-------------【NL查询队列】------------查询订单信息不存在 orderCode :{}", orderCode);
                return;
            }
            //订单已支付
            if (!orders.getTradeStatus().equals(TradeConstant.ORDER_PAYING)) {
                log.info("-------------【NL查询队列】------------- 订单状态不为支付中");
                return;
            }
            //校验订单信息
//            if (new BigDecimal(totalAmount).compareTo(orders.getTradeAmount()) != 0) {
//                log.info("------------- NGANLUONG 查询队列信息记录 ------------- 订单信息不匹配 orders ：{}", JSON.toJSONString(orders));
//                return;
//            }
            orders.setChannelNumber(transactionId);//通道流水号
            orders.setChannelCallbackTime(new Date());//通道回调时间
            orders.setUpdateTime(new Date());//修改时间
            Example example = new Example(Orders.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("tradeStatus", "2");
            criteria.andEqualTo("id", orders.getId());
            if ("00".equals(transactionStatus)) {
                log.info("---------【NL查询队列】----------【订单已支付成功】 orderID : {}", orders.getId());
                //支付成功
                orders.setTradeStatus(TradeConstant.ORDER_PAY_SUCCESS);
                //更改channelsOrders状态
                try {
                    channelsOrderMapper.updateStatusById(orderCode, transactionId, TradeConstant.TRADE_SUCCESS);
                } catch (Exception e) {
                    log.error("=================【NL查询队列】=================【更新通道订单异常】", e);
                }
                //修改订单状态
                int i = ordersMapper.updateByExampleSelective(orders, example);
                if (i > 0) {
                    log.info("=================【NL查询队列】=================【订单支付成功后更新数据库成功】 orderId: {}", orders.getId());
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
                            log.info("=================【NL查询队列】=================【上报清结算前线下下单创建账户信息】");
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
                                log.info("=================【NL查询队列】=================【上报清结算失败,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】");
                                RabbitMassage massage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                                rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(massage));
                            }
                        } else {
                            log.info("=================【NL查询队列】=================【上报清结算失败,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】");
                            RabbitMassage massage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                            rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(massage));
                        }
                    } catch (Exception e) {
                        log.error("=================【NL查询队列】=================【上报清结算异常,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】", e);
                        RabbitMassage massage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                        rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(massage));
                    }
                } else {
                    log.info("=================【NL查询队列】=================【订单支付成功后更新数据库失败】 orderId: {}", orders.getId());
                }
            } else if ("02".equals(transactionStatus)) {
                log.info("---------【NL查询队列】----------订单已支付失败 orderID : {}", orders.getId());
                //支付失败
                orders.setTradeStatus(TradeConstant.ORDER_PAY_FAILD);
                //更改channelsOrders状态
                try {
                    channelsOrderMapper.updateStatusById(orderCode, transactionId, TradeConstant.TRADE_FALID);
                } catch (Exception e) {
                    log.error("=================【NL查询队列】=================【更新通道订单异常】", e);
                }
                //计算支付失败时通道网关手续费
                commonService.calcCallBackGatewayFeeFailed(orders);
                if (ordersMapper.updateByExampleSelective(orders, example) == 1) {
                    log.info("=================【NL查询队列】=================【订单支付失败后更新数据库成功】 orderId: {}", orders.getId());
                } else {
                    log.error("=================【NL查询队列】=================【订单支付失败后更新数据库失败】 orderId: {}", orders.getId());
                }
            } else {
                log.info("---------【NL查询队列】----------【订单是交易中】 orderID : {}", orders.getId());
                log.info("-----------------【NL查询队列】-------------【继续上报查询订单队列】 【E_MQ_NGANLUONG_CHECK_ORDER_DL】  token: {}", token);
                rabbitMQSender.send(AD3MQConstant.E_MQ_NGANLUONG_CHECK_ORDER_DL, JSON.toJSONString(rabbitMassage));
                return;
            }
            try {
                //商户回调地址不为空,回调商户服务器
                if (!StringUtils.isEmpty(orders.getReturnUrl())) {
                    log.info("----------【NL查询队列】----------回调商户开始");
                    commonService.replyReturnUrl(orders);
                }
                log.info("------------------【NL查询队列】----------------NGANLUONG 回调商户结束");
            } catch (Exception e) {
                log.error("----------【NL查询队列】----------回调商户异常", e);
            }
        } else {
            log.info("---------【NL查询队列】---------- 三次查询,订单为交易中 rabbitMassage : {}", JSON.toJSONString(rabbitMassage));
            messageFeign.sendSimple(developerMobile, "NGANLUONG查询队列三次查询预警 MQ_NGANLUONG_CHECK_ORDER_DL: { " + value + " }");//短信通知
            messageFeign.sendSimpleMail(developerEmail, "NGANLUONG查询队列三次查询预警 MQ_NGANLUONG_CHECK_ORDER_DL", "MQ_NGANLUONG_CHECK_ORDER_DL预警：{ " + value + " }");//邮件通知
        }
    }
}
