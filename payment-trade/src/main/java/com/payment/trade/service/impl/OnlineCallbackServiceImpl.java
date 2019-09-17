package com.payment.trade.service.impl;

import cn.hutool.core.date.DateUtil;
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
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.common.vo.FundChangeVO;
import com.payment.trade.config.AD3ParamsConfig;
import com.payment.trade.dao.OrdersMapper;
import com.payment.trade.dto.AD3OnlineCallbackDTO;
import com.payment.trade.rabbitmq.RabbitMQSender;
import com.payment.trade.service.ClearingService;
import com.payment.trade.service.CommonService;
import com.payment.trade.service.OnlineCallbackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author shenxinran
 * @Date: 2019/3/15 14:27
 * @Description: 线上网关回调Service
 */
@Transactional
@Service
@Slf4j
public class OnlineCallbackServiceImpl implements OnlineCallbackService {

    @Autowired
    private RabbitMQSender rabbitMQSender;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private CommonService commonService;

    @Autowired
    private ClearingService clearingService;

    @Autowired
    private AD3ParamsConfig ad3ParamsConfig;

    /**
     * 线上AD3回调接口处理方法
     *
     * @param ad3OnlineCallbackDTO @return
     * @return
     */
    @Override
    public String callback(AD3OnlineCallbackDTO ad3OnlineCallbackDTO) {
        //校验参数
        checkCallbackDTO(ad3OnlineCallbackDTO);
        log.info("************** ad3线上回调接口信息记录 ************** 订单信息 ad3OfflineCallbackDTO:{}", JSON.toJSONString(ad3OnlineCallbackDTO));
        //检验签名
        Orders orders = ordersMapper.selectByPrimaryKey(ad3OnlineCallbackDTO.getMerOrderNo());
        //校验原有订单信息
        if (orders == null) {
            log.info("-------------回调订单不存在------------ad3OnlineCallbackDTO:{}", JSON.toJSON(ad3OnlineCallbackDTO));
            throw new BusinessException(EResultEnum.ORDER_NOT_EXIST.getCode());
        }
        //校验订单状态
        if (!TradeConstant.ORDER_PAYING.equals(orders.getTradeStatus())) {
            log.info("=================【AD3线上回调接口信息记录】=================【订单状态不为支付中】");
            return "success";
        }
        BigDecimal ad3Amount = new BigDecimal(ad3OnlineCallbackDTO.getMerorderAmount());//ad3交易金额
        BigDecimal tradeAmount = orders.getTradeAmount();//交易金额
        if (ad3Amount.compareTo(tradeAmount) != 0 || !ad3OnlineCallbackDTO.getMerorderCurrency().equals(orders.getTradeCurrency())) {
            log.info("************** ad3线上回调接口信息记录 ************** 订单信息不匹配 ad3OfflineCallbackDTO:{},orders:{}", JSON.toJSONString(ad3OnlineCallbackDTO), JSON.toJSONString(orders));
            throw new BusinessException(EResultEnum.ORDER_INFO_NO_MATCHING.getCode());
        }
        orders.setChannelNumber(ad3OnlineCallbackDTO.getTxnid());//通道流水号
        orders.setUpChannelFee(ad3OnlineCallbackDTO.getTradeFee());//上游通道手续费
        orders.setChannelCallbackTime(DateUtil.parse(ad3OnlineCallbackDTO.getTxndate(), "yyyyMMddHHmmss"));//通道回调时间
        orders.setUpdateTime(new Date());//更新时间
        String status = ad3OnlineCallbackDTO.getStatus();
        Example example = new Example(Orders.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("tradeStatus", "2");
        criteria.andEqualTo("id", orders.getId());
        if (AD3Constant.ORDER_SUCCESS.equals(status)) {
            log.info("=================【AD3线上回调接口信息记录】=================【订单已支付成功】 orderId: {}", orders.getId());
            orders.setTradeStatus(TradeConstant.ORDER_PAY_SUCCESS);
            int i = ordersMapper.updateByExampleSelective(orders, example);
            if (i > 0) {
                log.info("=================【AD3线上回调接口信息记录】=================【订单支付成功后更新数据库成功】 orderId: {}", orders.getId());
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
                        log.info("=================【AD3线上回调接口信息记录】=================【上报清结算前线下下单创建账户信息】");
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
                            log.info("=================【AD3线上回调接口信息记录】=================【上报清结算失败,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】");
                            RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                            rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                        }
                    } else {
                        log.info("=================【AD3线上回调接口信息记录】=================【上报清结算失败,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】");
                        RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                        rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                    }
                } catch (Exception e) {
                    log.error("=================【AD3线上回调接口信息记录】=================【上报清结算异常,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】", e);
                    RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                    rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                }
            } else {
                log.info("=================【AD3线上回调接口信息记录】=================【订单支付成功后更新数据库失败】 orderId: {}", orders.getId());
            }
        } else if (AD3Constant.ORDER_FAILED.equals(status)) {
            log.info("=================【AD3线上回调接口信息记录】=================【订单已支付失败】 orderId: {}", orders.getId());
            //支付失败
            orders.setTradeStatus(TradeConstant.ORDER_PAY_FAILD);
            //计算支付失败时的通道网关手续费
            commonService.calcCallBackGatewayFeeFailed(orders);
            if (ordersMapper.updateByPrimaryKeySelective(orders) == 1) {
                log.info("=================【AD3线上回调接口信息记录】=================【订单支付失败后更新数据库成功】 orderId: {}", orders.getId());
            } else {
                log.info("=================【AD3线上回调接口信息记录】=================【订单支付失败后更新数据库失败】 orderId: {}", orders.getId());
            }
        } else {
            log.info("=================【AD3线上回调接口信息记录】=================【订单为其他状态】 orderId: {}", orders.getId());
        }
        //回调商户
        if (!StringUtils.isEmpty(orders.getReturnUrl())) {
            try {
                commonService.replyReturnUrl(orders);
            } catch (Exception e) {
                log.error("=================【AD3线上回调接口信息记录】=================【回调商户异常】", e);
            }
        }
        return "success";
    }

    /**
     * AD3线上浏览器地址回调处理方法
     * @param ad3OnlineCallbackDTO
     * @param response
     */
    @Override
    public void jump(AD3OnlineCallbackDTO ad3OnlineCallbackDTO, HttpServletResponse response) {
        //校验参数
        checkCallbackDTO(ad3OnlineCallbackDTO);
        Orders orders = ordersMapper.selectByPrimaryKey(ad3OnlineCallbackDTO.getMerOrderNo());
        //校验原有订单信息
        if (orders == null) {
            log.info("-------------浏览器地址回调订单不存在------------ad3OnlineCallbackDTO:{}", JSON.toJSON(ad3OnlineCallbackDTO));
            throw new BusinessException(EResultEnum.ORDER_NOT_EXIST.getCode());
        }
        orders.setChannelCallbackTime(new Date());
        orders.setUpdateTime(new Date());
        if (!StringUtils.isEmpty(orders.getJumpUrl())) {
            log.info("----------回调商户----------");
            commonService.replyJumpUrl(orders, response);
        } else {
            //调用AW页面
            try {
                response.sendRedirect(ad3ParamsConfig.getPaySuccessUrl() + "?page=" + TradeConstant.PAGE_PROCESSING);
            } catch (IOException e) {
                log.info("--------------调用AW支付成功页面失败--------------");
            }
        }


    }

    /**
     * 校验回调参数
     *
     * @param ad3OnlineCallbackDTO
     * @return
     */
    private void checkCallbackDTO(AD3OnlineCallbackDTO ad3OnlineCallbackDTO) {
        log.info("--------------回调参数记录---------------- AD3OnlineCallbackDTO:{}", JSON.toJSON(ad3OnlineCallbackDTO));
        //商户上送的商户订单号
        if (StringUtils.isEmpty(ad3OnlineCallbackDTO.getMerOrderNo())) {
            throw new BusinessException(EResultEnum.CALLBACK_PARAMETER_IS_NULL.getCode());
        }
        if (StringUtils.isEmpty(ad3OnlineCallbackDTO.getTxnid())) {
            throw new BusinessException(EResultEnum.CALLBACK_PARAMETER_IS_NULL.getCode());
        }
        if (StringUtils.isEmpty(ad3OnlineCallbackDTO.getTxndate())) {
            throw new BusinessException(EResultEnum.CALLBACK_PARAMETER_IS_NULL.getCode());
        }
        if (StringUtils.isEmpty(ad3OnlineCallbackDTO.getStatus())) {
            throw new BusinessException(EResultEnum.CALLBACK_PARAMETER_IS_NULL.getCode());
        }
        if (StringUtils.isEmpty(ad3OnlineCallbackDTO.getTradeFee())) {
            throw new BusinessException(EResultEnum.CALLBACK_PARAMETER_IS_NULL.getCode());
        }
    }
}
