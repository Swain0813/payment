package com.payment.trade.service.impl;

import com.alibaba.fastjson.JSON;
import com.payment.common.constant.AD3Constant;
import com.payment.common.constant.AD3MQConstant;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.constant.TradeConstant;
import com.payment.common.dto.FundChangeDTO;
import com.payment.common.entity.Orders;
import com.payment.common.entity.RabbitMassage;
import com.payment.common.enums.Status;
import com.payment.common.exception.BusinessException;
import com.payment.common.redis.RedisService;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.common.utils.DateToolUtils;
import com.payment.common.utils.MD5Util;
import com.payment.common.utils.ReflexClazzUtils;
import com.payment.common.utils.SignTools;
import com.payment.common.vo.FundChangeVO;
import com.payment.trade.dao.OrdersMapper;
import com.payment.trade.dto.AD3OfflineCallbackDTO;
import com.payment.trade.rabbitmq.RabbitMQSender;
import com.payment.trade.service.ClearingService;
import com.payment.trade.service.CommonService;
import com.payment.trade.service.OfflineCallbackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * @author XuWenQi
 * @Date: 2019/3/28 15:43
 * @Description: 线下回调业务接口实现类
 */
@Service
@Slf4j
public class OfflineCallbackServiceImpl implements OfflineCallbackService {

    @Autowired
    private RabbitMQSender rabbitMQSender;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private ClearingService clearingService;

    @Autowired
    private CommonService commonService;

    @Autowired
    private RedisService redisService;


    /**
     * ad3线下回调
     *
     * @param ad3OfflineCallbackDTO ad3线下回调输入实体
     * @return
     */
    @Override
    public String ad3Callback(AD3OfflineCallbackDTO ad3OfflineCallbackDTO) {
        //校验输入参数
        checkParam(ad3OfflineCallbackDTO);
        //生成签名
        String mySign = createSign(ad3OfflineCallbackDTO, redisService.get(AD3Constant.AD3_LOGIN_TOKEN));
        log.info("=================【AD3线下回调接口信息记录】=================【回调签名】 mySign: {}", mySign);
        //验签
//        if (!ad3OfflineCallbackDTO.getSignMsg().equals(mySign)) {
//            log.info("=================【AD3线下回调接口信息记录】=================【签名不匹配】 ad3Sign: {}", ad3OfflineCallbackDTO.getSignMsg());
//            return "failed";
//        }
        //查询订单信息
        Orders orders = ordersMapper.selectByPrimaryKey(ad3OfflineCallbackDTO.getMerorderNo());
        //校验业务信息
        checkBusiness(ad3OfflineCallbackDTO, orders);
        //校验订单状态
        if (!TradeConstant.ORDER_PAYING.equals(orders.getTradeStatus())) {
            log.info("=================【AD3线下回调接口信息记录】=================【订单状态不为支付中】");
            return "success";
        }
        //通道流水号
        orders.setChannelNumber(ad3OfflineCallbackDTO.getTxnId());
        //通道回调时间
        orders.setChannelCallbackTime(DateToolUtils.getDateFromString(ad3OfflineCallbackDTO.getTxnDate(), "yyyyMMddHHmmss"));
        //修改时间
        orders.setUpdateTime(new Date());
        Example example = new Example(Orders.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("tradeStatus", "2");
        criteria.andEqualTo("id", orders.getId());
        if (AD3Constant.ORDER_SUCCESS.equals(ad3OfflineCallbackDTO.getStatus())) {
            log.info("=================【AD3线下回调接口信息记录】=================【订单已支付成功】 orderId: {}", orders.getId());
            orders.setTradeStatus((TradeConstant.ORDER_PAY_SUCCESS));
            //更新订单信息
            if (ordersMapper.updateByExampleSelective(orders, example) == 1) {
                log.info("=================【AD3线下回调接口信息记录】=================【订单支付成功后更新数据库成功】 orderId: {}", orders.getId());
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
                        log.info("=================【AD3线下回调接口信息记录】=================【上报清结算前线下下单创建账户信息】");
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
                            log.info("=================【AD3线下回调接口信息记录】=================【上报清结算失败,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】");
                            RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                            rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                        }
                    } else {
                        log.info("=================【AD3线下回调接口信息记录】=================【上报清结算失败,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】");
                        RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                        rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                    }
                } catch (Exception e) {
                    log.error("=================【AD3线下回调接口信息记录】=================【上报清结算异常,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】", e);
                    RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                    rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                }
            } else {
                log.info("=================【AD3线下回调接口信息记录】=================【订单支付成功后更新数据库失败】 orderId: {}", orders.getId());
            }
        } else if (AD3Constant.ORDER_FAILED.equals(ad3OfflineCallbackDTO.getStatus())) {
            log.info("=================【AD3线下回调接口信息记录】=================【订单已支付失败】 orderId: {}", orders.getId());
            //支付失败
            orders.setTradeStatus(TradeConstant.ORDER_PAY_FAILD);
            //计算支付失败时的通道网关手续费
            commonService.calcCallBackGatewayFeeFailed(orders);
            if (ordersMapper.updateByExampleSelective(orders, example) == 1) {
                log.info("=================【AD3线下回调接口信息记录】=================【订单支付失败后更新数据库成功】 orderId: {}", orders.getId());
            } else {
                log.info("=================【AD3线下回调接口信息记录】=================【订单支付失败后更新数据库失败】 orderId: {}", orders.getId());
            }
        } else {
            log.info("=================【AD3线下回调接口信息记录】=================【订单为其他状态】 orderId: {}", orders.getId());
        }
        try {
            //商户服务器回调地址不为空,回调商户服务器
            if (!StringUtils.isEmpty(orders.getReturnUrl())) {
                commonService.replyReturnUrl(orders);
            }
        } catch (Exception e) {
            log.error("=================【AD3线下回调接口信息记录】=================【回调商户异常】", e);
        }
        return "success";
    }

    /**
     * 校验ad3输入参数
     *
     * @param ad3OfflineCallbackDTO ad3线下回调输入实体
     * @return
     */
    private void checkParam(AD3OfflineCallbackDTO ad3OfflineCallbackDTO) {
        if (ad3OfflineCallbackDTO == null) {
            log.info("=================【AD3线下回调接口信息记录】=================【回调参数为空】");
            throw new BusinessException(EResultEnum.CALLBACK_PARAMETER_IS_NULL.getCode());
        }
        if (StringUtils.isEmpty(ad3OfflineCallbackDTO.getMerorderNo())) {
            log.info("=================【AD3线下回调接口信息记录】=================【订单号为空】");
            //订单号
            throw new BusinessException(EResultEnum.CALLBACK_PARAMETER_IS_NULL.getCode());
        }
        if (StringUtils.isEmpty(ad3OfflineCallbackDTO.getStatus())) {
            log.info("=================【AD3线下回调接口信息记录】=================【订单状态为空】");
            //订单状态
            throw new BusinessException(EResultEnum.CALLBACK_PARAMETER_IS_NULL.getCode());
        }
        if (StringUtils.isEmpty(ad3OfflineCallbackDTO.getSignMsg())) {
            log.info("=================【AD3线下回调接口信息记录】=================【签名为空】");
            //签名
            throw new BusinessException(EResultEnum.CALLBACK_PARAMETER_IS_NULL.getCode());
        }
    }

    /**
     * 校验ad3业务信息
     *
     * @param ad3OfflineCallbackDTO ad3线下回调输入实体
     * @param orders                订单实体
     * @return
     */
    private void checkBusiness(AD3OfflineCallbackDTO ad3OfflineCallbackDTO, Orders orders) {
        if (orders == null) {
            //订单信息不存在
            log.info("=================【AD3线下回调接口信息记录】=================【回调订单信息不存在】");
            throw new BusinessException(EResultEnum.ORDER_NOT_EXIST.getCode());
        }
        log.info("=================【AD3线下回调接口信息记录】=================【回调订单信息记录】 orders: {}", JSON.toJSONString(orders));
        if (!AD3Constant.AD3_OFFLINE_SUCCESS.equals(ad3OfflineCallbackDTO.getRespcode())) {
            //返回码异常
            log.info("=================【AD3线下回调接口信息记录】=================【返回码异常】");
            throw new BusinessException(EResultEnum.CALLBACK_PARAMETERS_ARE_ILLEGAL.getCode());
        }
        //校验交易金额与交易币种
        BigDecimal ad3Amount = new BigDecimal(ad3OfflineCallbackDTO.getMerorderAmount());
        BigDecimal tradeAmount = orders.getTradeAmount();
        if (ad3Amount.compareTo(tradeAmount) != 0 || !ad3OfflineCallbackDTO.getMerorderCurrency().equals(orders.getTradeCurrency())) {
            log.info("=================【AD3线下回调接口信息记录】=================【订单信息不匹配】");
            throw new BusinessException(EResultEnum.ORDER_INFO_NO_MATCHING.getCode());
        }
    }

    /**
     * 生成AD3线下回调签名
     *
     * @param obj obj
     * @return AD3回调签名
     */
    private static String createSign(Object obj, String token) {
        //获得对象属性名对应的属性值Map
        Map<String, Object> objMap = ReflexClazzUtils.getFieldNames(obj);
        HashMap<String, String> paramMap = new HashMap<>();
        //转换成String
        for (String str : objMap.keySet()) {
            paramMap.put(str, String.valueOf(objMap.get(str)));
        }
        paramMap.put("signMsg", null);
        //排序,去空,将属性值按属性名首字母升序排序
        String signature = SignTools.getSignStr(paramMap);
        String clearText = signature + "&" + token;
        log.info("=================【AD3线下回调接口信息记录】=================【签名前的明文】 clearText: {}", clearText);
        return MD5Util.getMD5String(clearText);
    }
}
