package com.payment.trade.service.impl;
import com.alibaba.fastjson.JSON;
import com.payment.common.constant.AD3MQConstant;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.constant.TradeConstant;
import com.payment.common.dto.*;
import com.payment.common.entity.*;
import com.payment.common.exception.BusinessException;
import com.payment.common.redis.RedisService;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.common.utils.IDS;
import com.payment.common.vo.FundChangeVO;
import com.payment.common.vo.OrderPaymentDetailVO;
import com.payment.trade.channels.help2pay.Help2PayService;
import com.payment.trade.dao.*;
import com.payment.trade.rabbitmq.RabbitMQSender;
import com.payment.trade.service.ClearingService;
import com.payment.trade.service.CommonService;
import com.payment.trade.service.PayOutService;
import com.payment.trade.vo.BasicsInfoVO;
import com.payment.trade.vo.CalcFeeVO;
import com.payment.trade.vo.CalcRateVO;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


/**
 * @description:
 * @author: YangXu
 * @create: 2019-07-23 10:45
 **/
@Slf4j
@Service
@Transactional
public class PayOutServiceImpl implements PayOutService {
    @Autowired
    private CommonService commonService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private OrderPaymentMapper orderPaymentMapper;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private ClearingService clearingService;

    @Autowired
    private Help2PayService help2PayService;

    @Autowired
    private ReconciliationMapper reconciliationMapper;

    @Autowired
    private RabbitMQSender rabbitMQSender;


    /**
     * @return
     * @Author YangXu
     * @Date 2019/7/23
     * @Descripate 批量付款接口
     * 这边如果有修改要考虑下面的机构后台用的付款接口institutionPayment
     **/
    @Override
    public BaseResponse payment(PayOutDTO payOutDTO) {
        BaseResponse baseResponse = new BaseResponse();
        //余额校验批次号
        String key = AsianWalletConstant.PAYOUT_BALANCE_KEY + "_" + IDS.uniqueID();
        if (payOutDTO.getPayOutRequestDTOs() == null || payOutDTO.getPayOutRequestDTOs().size() == 0) {
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        //校验签名
        if (StringUtils.isEmpty(payOutDTO.getSign()) || StringUtils.isEmpty(payOutDTO.getSignType())) {
            log.info("--------------- payment 批量付款接口 ---------------，签名为空");
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        if (!commonService.checkPayment(payOutDTO)) {
            throw new BusinessException(EResultEnum.DECRYPTION_ERROR.getCode());
        }
        //创建付款单
        List<OrderPayment> list = Lists.newArrayList();
        for (PayOutRequestDTO payOutRequestDTO : payOutDTO.getPayOutRequestDTOs()) {
            //校验参数
            BasicsInfoVO basicsInfoVO = null;
            try {
                check(payOutRequestDTO);
                //获取机构产品通道基础信息
                basicsInfoVO = commonService.getBasicsInfo(payOutRequestDTO);
            } catch (Exception e) {
                log.info("------------ 校验产数或获取获取机构产品通道基础信息异常 ----------------- payOutRequestDTO ：{}", JSON.toJSONString(payOutRequestDTO));
            }
            //创建付款单
            OrderPayment orderPayment = commonService.createOrderPayment(payOutRequestDTO, payOutDTO.getReqIp(), basicsInfoVO);
            if (basicsInfoVO == null) {
                orderPayment.setRemark("校验产数或获取获取机构产品通道基础信息异常");
                list.add(orderPayment);
                continue;
            }
            //判断是否需要换汇----订单币种和汇款币种是否一致
            if (payOutRequestDTO.getOrderCurrency().equals(payOutRequestDTO.getPaymentCurrency())) {
                //币种相同
                if (StringUtils.isEmpty(payOutRequestDTO.getOrderAmount())) {
                    //订单金额
                    payOutRequestDTO.setOrderAmount(payOutRequestDTO.getPaymentAmount());
                }
                if (StringUtils.isEmpty(payOutRequestDTO.getPaymentAmount())) {
                    //汇款金额
                    payOutRequestDTO.setPaymentAmount(payOutRequestDTO.getOrderAmount());
                }
                if(payOutRequestDTO.getOrderAmount().compareTo(BigDecimal.ZERO)== 0){
                    orderPayment.setTradeAmount(payOutRequestDTO.getPaymentAmount());
                    orderPayment.setPaymentAmount(payOutRequestDTO.getPaymentAmount());
                }else if(payOutRequestDTO.getPaymentAmount().compareTo(BigDecimal.ZERO)== 0){
                    orderPayment.setTradeAmount(payOutRequestDTO.getOrderAmount());
                    orderPayment.setPaymentAmount(payOutRequestDTO.getOrderAmount());
                }
                orderPayment.setExchangeRate(new BigDecimal(1)); //汇率
                orderPayment.setExchangeStatus(TradeConstant.SWAP_SUCCESS); //换汇状态
                orderPayment.setExchangeTime(new Date()); //换汇时间
                //原始汇率
                orderPayment.setOldExchangeRate(new BigDecimal(1));
                //算费
                orderPayment = calculateFee(orderPayment, basicsInfoVO);
                list.add(orderPayment);
            } else {
                //币种不同---进行换汇然后把换汇后的金额进行设置
                log.info("-------------------- 换汇开始 ---------------------");
                CalcRateVO calcRateVO = null;
                if (StringUtils.isEmpty(payOutRequestDTO.getOrderAmount())
                        || (!StringUtils.isEmpty(payOutRequestDTO.getOrderAmount()) && payOutRequestDTO.getOrderAmount().compareTo(BigDecimal.ZERO) == 0)) {
                    calcRateVO = commonService.calcExchangeRate(payOutRequestDTO.getPaymentCurrency(), payOutRequestDTO.getOrderCurrency(),
                            BigDecimal.ZERO, payOutRequestDTO.getPaymentAmount());
                    //订单金额
                    payOutRequestDTO.setOrderAmount(calcRateVO.getTradeAmount());
                }
                if (StringUtils.isEmpty(payOutRequestDTO.getPaymentAmount())
                        || (!StringUtils.isEmpty(payOutRequestDTO.getPaymentAmount()) && payOutRequestDTO.getPaymentAmount().compareTo(BigDecimal.ZERO) == 0)) {
                    calcRateVO = commonService.calcExchangeRate(payOutRequestDTO.getOrderCurrency(), payOutRequestDTO.getPaymentCurrency(),
                            BigDecimal.ZERO, payOutRequestDTO.getOrderAmount());
                    //汇款金额
                    payOutRequestDTO.setPaymentAmount(calcRateVO.getTradeAmount());
                }
                //换汇成功
                if (calcRateVO.getExchangeStatus() != null && TradeConstant.SWAP_SUCCESS.equals(calcRateVO.getExchangeStatus())) {
                    log.info("-------------------- 换汇成功 ---------------------");
                    //订单金额
                    orderPayment.setTradeAmount(payOutRequestDTO.getOrderAmount());
                    //汇款金额
                    orderPayment.setPaymentAmount(payOutRequestDTO.getPaymentAmount());
                    //汇率
                    orderPayment.setExchangeRate(calcRateVO.getExchangeRate());
                    //换汇时间
                    orderPayment.setExchangeTime(calcRateVO.getExchangeTime());
                    //原始汇率
                    orderPayment.setOldExchangeRate(calcRateVO.getOriginalRate());
                    //算费 手续费是以汇款金额计算还要换回来
                    orderPayment = calculateFee(orderPayment, basicsInfoVO);
                    //手续费换汇
                    log.info("-------------------- 手续费换汇开始 ---------------------");
                    CalcRateVO calcRateVO1 = commonService.calcExchangeRate(payOutRequestDTO.getPaymentCurrency(), payOutRequestDTO.getOrderCurrency(),
                            BigDecimal.ZERO, orderPayment.getFee());
                    if (calcRateVO1.getExchangeStatus() != null && TradeConstant.SWAP_SUCCESS.equals(calcRateVO1.getExchangeStatus())) {
                        log.info("-------------------- 手续费换汇成功 ---------------------");
                        orderPayment.setFee(calcRateVO1.getTradeAmount());
                        //换汇状态
                        orderPayment.setExchangeStatus(TradeConstant.SWAP_SUCCESS);
                    } else {
                        log.info("-------------------- 手续费换汇失败 ---------------------");
                        //换汇状态
                        orderPayment.setExchangeStatus(TradeConstant.SWAP_FALID);
                        orderPayment.setPayoutStatus(TradeConstant.PAYMENT_FAIL);
                        orderPayment.setRemark("手续费换汇失败");
                    }
                    list.add(orderPayment);
                } else {
                    log.info("-------------------- 换汇失败 ---------------------");
                    //付款状态
                    orderPayment.setPayoutStatus(TradeConstant.PAYMENT_FAIL);
                    //订单金额
                    orderPayment.setTradeAmount(payOutRequestDTO.getOrderAmount());
                    //汇款金额
                    orderPayment.setPaymentAmount(payOutRequestDTO.getPaymentAmount());
                    //汇率
                    orderPayment.setExchangeRate(calcRateVO.getExchangeRate());
                    //换汇状态
                    orderPayment.setExchangeStatus(TradeConstant.SWAP_FALID);
                    //换汇时间
                    orderPayment.setExchangeTime(calcRateVO.getExchangeTime());
                    list.add(orderPayment);
                }
            }
        }
        //校检余额
        for (OrderPayment orderPayment : list) {
            String banlance = redisService.get(key.concat("_").concat(orderPayment.getTradeCurrency()));
            Account account = null;
            if (StringUtils.isEmpty(banlance)) {
                //查询订单买入汇率
                account = accountMapper.getAccount(orderPayment.getInstitutionCode(), orderPayment.getTradeCurrency());
                if (account == null) {
                    log.info("-----------------批量付款接口 校检余额 ----------------未查询到账户信息 InstitutionCode :{}, TradeCurrency: {}",
                            orderPayment.getInstitutionCode(), orderPayment.getTradeCurrency());
                    throw new BusinessException(EResultEnum.ACCOUNT_IS_NOT_EXIST.getCode());
                }
                BigDecimal newBalance1 = account.getSettleBalance().subtract(account.getFreezeBalance()).subtract(orderPayment.getTradeAmount());
                if (newBalance1.compareTo(BigDecimal.ZERO) == -1) {
                    throw new BusinessException(EResultEnum.INSUFFICIENT_ACCOUNT_BALANCE.getCode());
                }
                banlance = String.valueOf(newBalance1);
            } else {
                BigDecimal newBalance2 = new BigDecimal(banlance).subtract(orderPayment.getTradeAmount());
                if (newBalance2.compareTo(BigDecimal.ZERO) == -1) {
                    throw new BusinessException(EResultEnum.INSUFFICIENT_ACCOUNT_BALANCE.getCode());
                }
                banlance = String.valueOf(newBalance2);
            }
            //redis里保存一分钟
            redisService.set(key.concat("_").concat(orderPayment.getTradeCurrency()), banlance, 1 * 60 * 60);
        }
        //将汇率记录插入到付款订单表
        orderPaymentMapper.insertList(list);
        //冻结金额，上报清结算
        for (OrderPayment op : list) {
            //付款状态为待付款时上报清结算
            if (TradeConstant.PAYMENT_WAIT.equals(op.getPayoutStatus())) {
                FundChangeDTO fundChangeDTO = new FundChangeDTO(op);
                BaseResponse fundChangeResponse = clearingService.fundChange(fundChangeDTO, null);
                if (fundChangeResponse.getCode() != null && fundChangeResponse.getCode().equals(String.valueOf(AsianWalletConstant.HTTP_SUCCESS_STATUS))) {//请求成功
                    FundChangeVO fundChangeVO = (FundChangeVO) fundChangeResponse.getData();
                    if (!fundChangeVO.getRespCode().equals(TradeConstant.CLEARING_SUCCESS)) {//业务处理失败
                        op.setPayoutStatus(TradeConstant.PAYMENT_FAIL);
                        op.setRemark("批量付款接口上报清结算失败");
                        op.setUpdateTime(new Date());
                        orderPaymentMapper.updateByPrimaryKeySelective(op);
                    }
                } else {//请求失败
                    op.setPayoutStatus(TradeConstant.PAYMENT_FAIL);
                    op.setRemark("批量付款接口上报清结算失败");
                    op.setUpdateTime(new Date());
                    orderPaymentMapper.updateByPrimaryKeySelective(op);
                }
            }
        }
        baseResponse.setCode(EResultEnum.SUCCESS.getCode());
        baseResponse.setMsg("SUCCESS");
        return baseResponse;
    }


    /**
     * @return
     * @Author YangXu
     * @Date 2019/7/23
     * @Descripate 校验参数
     **/
    public void check(PayOutRequestDTO payOutRequestDTO) {
        //机构编号编号
        if (StringUtils.isEmpty(payOutRequestDTO.getInstitutionId())) {
            log.info("--------------- 校验参数 --------------- PayOutRequestDTO : InstitutionId is NULL");
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        //汇款币种
        if (StringUtils.isEmpty(payOutRequestDTO.getPaymentCurrency())) {
            log.info("--------------- 校验参数 --------------- PayOutRequestDTO : PaymentCurrency is NULL");
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        //订单币种
        if (StringUtils.isEmpty(payOutRequestDTO.getOrderCurrency())) {
            log.info("--------------- 校验参数 --------------- PayOutRequestDTO : OrderCurrency is NULL");
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        //机构上报付款流水号
        if (StringUtils.isEmpty(payOutRequestDTO.getOrderNo())) {
            log.info("--------------- 校验参数 --------------- PayOutRequestDTO : OrderNo is NULL");
            payOutRequestDTO.setOrderNo("CPO" + IDS.uniqueID());
        }
        //汇款银行卡号
        if (StringUtils.isEmpty(payOutRequestDTO.getBankAccountNumber())) {
            log.info("--------------- 校验产数 --------------- PayOutRequestDTO : BankAccountNumber is NULL");
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        //银行机构代码
        if (StringUtils.isEmpty(payOutRequestDTO.getIssuerId())) {
            log.info("--------------- 校验产数 --------------- PayOutRequestDTO : IssuerId is NULL");
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        //订单金额  汇款金额
        if (StringUtils.isEmpty(payOutRequestDTO.getOrderAmount()) && StringUtils.isEmpty(payOutRequestDTO.getPaymentAmount())) {
            log.info("--------------- 校验产数 --------------- PayOutRequestDTO : OrderAmount or PaymentAmount is NULL");
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        //机构订单号
        if (orderPaymentMapper.selectByInstitutionOrderId(payOutRequestDTO.getOrderNo()) > 0) {
            log.info("--------------- 校验产数 --------------- PayOutRequestDTO : institution order id exist");
            throw new BusinessException(EResultEnum.INSTITUTION_ORDER_ID_EXIST.getCode());
        }
    }


    /**
     * 计算手续费
     *
     * @param orderPayment
     * @param basicsInfoVO
     * @return
     */
    public OrderPayment calculateFee(OrderPayment orderPayment, BasicsInfoVO basicsInfoVO) {
        CalcFeeVO calcFeeVO = commonService.calcPoundageOrderPayment(orderPayment, basicsInfoVO);
        if (TradeConstant.CHARGE_STATUS_FALID.equals(calcFeeVO.getChargeStatus())) {
            //计算手续费失败的场合
            orderPayment.setPayoutStatus(TradeConstant.PAYMENT_FAIL);//付款状态--付款失败
            orderPayment.setChargeStatus(TradeConstant.CHARGE_STATUS_FALID);//计费状态-计费失败
            orderPayment.setChargeTime(calcFeeVO.getChargeTime());//计费时间
            //费率
            orderPayment.setRate(basicsInfoVO.getInstitutionProduct().getRate());
            //费率类型
            orderPayment.setRateType(basicsInfoVO.getInstitutionProduct().getRateType());
        } else {
            //计算手续费成功的场合
            //付款状态---付款中
            orderPayment.setPayoutStatus(TradeConstant.PAYMENT_WAIT);
            //计费状态--计费成功
            orderPayment.setChargeStatus(TradeConstant.CHARGE_STATUS_SUCCESS);
            //手续费
            orderPayment.setFee(calcFeeVO.getFee());
            orderPayment.setChargeTime(calcFeeVO.getChargeTime());//计费时间
            //费率
            orderPayment.setRate(basicsInfoVO.getInstitutionProduct().getRate());
            //费率类型
            orderPayment.setRateType(basicsInfoVO.getInstitutionProduct().getRateType());
        }
        return orderPayment;
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/8/6
     * @Descripate 请求上游
     **/
    public BaseResponse httpPostUpStream(OrderPayment orderPayment) {
        BaseResponse bs = new BaseResponse();
        bs.setCode(EResultEnum.SUCCESS.getCode());
        Channel channel = commonService.getChannelByChannelCode(orderPayment.getChannelCode());
        log.info("------------------ 运维后台审核请求上游 httpPostUpStream ----------------orderPayment : {}", JSON.toJSONString(orderPayment));
        if (orderPayment.getExtend3().equals(TradeConstant.HELP2PAY_ONLINE)) {
            log.info("------------------ 汇款通道 ：help2Pay ----------------orderPayment : {}", JSON.toJSONString(orderPayment));
            BaseResponse baseResponse = help2PayService.help2PayPayOut(orderPayment, channel);
            if (baseResponse != null) {
                if (baseResponse.getCode().equals("200")) {
                    orderPayment.setPayoutStatus(TradeConstant.PAYMENT_SUCCESS);
                    orderPayment.setReportChannelTime(new Date());
                    orderPayment.setUpdateTime(new Date());
                    orderPaymentMapper.updateByPrimaryKeySelective(orderPayment);
                } else {
                    bs.setMsg("汇款失败调账");
                    this.faliReconciliation(orderPayment, baseResponse.getMsg());
                }
            }
        } else if (orderPayment.getExtend3().equals("人工汇款")) {//人工汇款
            log.info("------------------ 汇款通道 ：人工汇款 ----------------orderPayment : {}", JSON.toJSONString(orderPayment));
            bs.setCode(EResultEnum.SUCCESS.getCode());
            bs.setMsg("人工汇款");
            orderPayment.setExtend4(true);
            orderPayment.setUpdateTime(new Date());
            orderPayment.setRemark("人工汇款");
            orderPaymentMapper.updateByPrimaryKeySelective(orderPayment);
        } else {
            log.info("------------------ 汇款通道不存在 ----------------orderPayment : {}", JSON.toJSONString(orderPayment));
            throw new BusinessException(EResultEnum.CHANNEL_IS_NOT_EXISTS.getCode());
        }
        return bs;
    }


    /**
     * @return
     * @Author YangXu
     * @Date 2019/8/7
     * @Descripate 运维后台审核汇款单
     **/
    @Override
    public BaseResponse operationsAudit(String name, String orderPaymentId, boolean enabled, String remark) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setCode(EResultEnum.SUCCESS.getCode());
        baseResponse.setMsg("SUCCESS");
        OrderPayment orderPayment = orderPaymentMapper.selectByPrimaryKey(orderPaymentId);
        if (orderPayment == null || orderPayment.getPayoutStatus() != TradeConstant.PAYMENT_WAIT) {
            log.info("------------------ 运维后台审核汇款单 operationsAudit ----------------orderPaymentId : {}订单不存在", orderPaymentId);
            throw new BusinessException(EResultEnum.QUERY_ORDER_ERROR.getCode());
        }
        orderPayment.setRemark(remark);
        orderPayment.setModifier(name);
        if (enabled) {
            baseResponse = this.httpPostUpStream(orderPayment);
        } else {
            this.faliReconciliation(orderPayment, "汇款审核不通过");
            orderPayment.setPayoutStatus(TradeConstant.PAYMENT_FAIL);
            log.info("------------------ 运维后台审核汇款单  ----------------orderPaymentId : {} 汇款审核不通过", orderPaymentId);
            if (!StringUtils.isEmpty(orderPayment.getServerUrl())) {
                try {
                    log.info("-------------人工汇款服务器回调方法信息记录------------回调商户服务器开始----------");
                    commonService.payOutCallBack(orderPayment);
                } catch (Exception e) {
                    log.info("-------------人工汇款服务器回调方法信息记录---------回调商户服务器异常----------", e);
                }
            }
        }
        return baseResponse;
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/8/7
     * @Descripate 分页查询汇款单
     **/
    @Override
    public PageInfo<OrderPayment> pageFindOrderPayment(OrderPaymentDTO orderPaymentDTO) {
        return new PageInfo<OrderPayment>(orderPaymentMapper.pageFindOrderPayment(orderPaymentDTO));
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/8/7
     * @Descripate 查询汇款单详细信息
     **/
    @Override
    public OrderPaymentDetailVO getOrderPaymentDetail(String orderPaymentId, String language) {
        return orderPaymentMapper.getOrderPaymentDetail(orderPaymentId, language);
    }


    /**
     * @return
     * @Author YangXu
     * @Date 2019/8/7
     * @Descripate 付款失败调账操作
     **/
    @Override
    public void faliReconciliation(OrderPayment orderPayment, String message) {
        log.info("------------------ 付款失败调账操作 faliReconciliation ----------------orderPayment : {},message ：{}", JSON.toJSONString(orderPayment), message);
        orderPayment.setPayoutStatus(TradeConstant.PAYMENT_FAIL);
        orderPayment.setReportChannelTime(new Date());
        orderPayment.setUpdateTime(new Date());
        orderPayment.setRemark(message);
        orderPaymentMapper.updateByPrimaryKeySelective(orderPayment);
        //添加调账记录
        Reconciliation reconciliation = new Reconciliation(orderPayment);
        String reconciliationId = "T" + IDS.uniqueID();
        reconciliation.setId(reconciliationId);
        reconciliation.setRemark(message);
        reconciliationMapper.insert(reconciliation);
        FundChangeDTO fundChangeDTO = new FundChangeDTO(orderPayment);
        fundChangeDTO.setRefcnceFlow(reconciliation.getId());
        fundChangeDTO.setSysorderid(orderPayment.getId());
        fundChangeDTO.setTradetype(TradeConstant.AA);
        fundChangeDTO.setTxnamount(String.valueOf(orderPayment.getTradeAmount().setScale(2, BigDecimal.ROUND_HALF_UP)));
        fundChangeDTO.setSltamount(String.valueOf(orderPayment.getTradeAmount().setScale(2, BigDecimal.ROUND_HALF_UP)));
        fundChangeDTO.setBalancetype(TradeConstant.NORMAL_FUND);
        fundChangeDTO.setSignMsg(null);
        BaseResponse cFundChange = clearingService.fundChange(fundChangeDTO, null);
        if (!cFundChange.getCode().equals(String.valueOf(AsianWalletConstant.HTTP_SUCCESS_STATUS))) {//请求失败
            log.info("----------- 付款调账失败，上报对列 ------------ fundChangeDTO :{}", JSON.toJSONString(fundChangeDTO));
            RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(fundChangeDTO));
            rabbitMQSender.send(AD3MQConstant.MQ_QJS_TZSB_DL, JSON.toJSONString(rabbitMassage));
        } else {//请求成功
            FundChangeVO fundChangeVO = (FundChangeVO) cFundChange.getData();
            if (!fundChangeVO.getRespCode().equals(TradeConstant.CLEARING_SUCCESS)) {
                //业务处理失败
                log.info("----------- 付款调账失败，上报对列 ------------ fundChangeDTO :{}", JSON.toJSONString(fundChangeDTO));
                RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(fundChangeDTO));
                rabbitMQSender.send(AD3MQConstant.MQ_QJS_TZSB_DL, JSON.toJSONString(rabbitMassage));
            }
        }
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/8/7
     * @Descripate 商户后台汇款接口
     * 这个接口的修改要考虑上面的批量汇款接口api---payment
     **/
    @Override
    public BaseResponse institutionPayment(PayOutDTO payOutDTO) {
        //返回结果
        BaseResponse baseResponse = new BaseResponse();
        String key = AsianWalletConstant.PAYOUT_BALANCE_KEY + "_" + IDS.uniqueID();//余额校验批次号
        if (payOutDTO.getPayOutRequestDTOs() == null || payOutDTO.getPayOutRequestDTOs().size() == 0) {
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        List<OrderPayment> list = Lists.newArrayList();
        for (PayOutRequestDTO payOutRequestDTO : payOutDTO.getPayOutRequestDTOs()) {
            //校验参数
            BasicsInfoVO basicsInfoVO = null;
            try {
                check(payOutRequestDTO);
                //获取机构产品通道基础信息
                basicsInfoVO = commonService.getBasicsInfo(payOutRequestDTO);
            } catch (Exception e) {
                log.info("------------ 校验产数或获取获取机构产品通道基础信息异常 ----------------- payOutRequestDTO ：{}", JSON.toJSONString(payOutRequestDTO));
            }
            //创建付款单
            OrderPayment orderPayment = commonService.createOrderPayment(payOutRequestDTO, payOutDTO.getReqIp(), basicsInfoVO);
            if (basicsInfoVO == null) {
                orderPayment.setRemark("校验产数或获取获取机构产品通道基础信息异常");
                list.add(orderPayment);
                continue;
            }
            //判断是否需要换汇
            if (payOutRequestDTO.getOrderCurrency().equals(payOutRequestDTO.getPaymentCurrency())) {
                //币种相同
                if (StringUtils.isEmpty(payOutRequestDTO.getOrderAmount())) {
                    payOutRequestDTO.setOrderAmount(payOutRequestDTO.getPaymentAmount());
                }
                if (StringUtils.isEmpty(payOutRequestDTO.getPaymentAmount())) {
                    payOutRequestDTO.setPaymentAmount(payOutRequestDTO.getOrderAmount());
                }
                if(payOutRequestDTO.getOrderAmount().compareTo(BigDecimal.ZERO)== 0){
                    orderPayment.setTradeAmount(payOutRequestDTO.getPaymentAmount());
                    orderPayment.setPaymentAmount(payOutRequestDTO.getPaymentAmount());
                }else if(payOutRequestDTO.getPaymentAmount().compareTo(BigDecimal.ZERO)== 0){
                    orderPayment.setTradeAmount(payOutRequestDTO.getOrderAmount());
                    orderPayment.setPaymentAmount(payOutRequestDTO.getOrderAmount());
                }
                orderPayment.setExchangeRate(new BigDecimal(1)); //汇率
                //原始汇率
                orderPayment.setOldExchangeRate(new BigDecimal(1));
                orderPayment.setExchangeStatus(TradeConstant.SWAP_SUCCESS); //换汇状态
                orderPayment.setExchangeTime(new Date()); //换汇时间
                //算费
                orderPayment = calculateFee(orderPayment, basicsInfoVO);
                list.add(orderPayment);
            } else {
                //必币种不同
                CalcRateVO calcRateVO = null;
                if (StringUtils.isEmpty(payOutRequestDTO.getOrderAmount())
                        || (!StringUtils.isEmpty(payOutRequestDTO.getOrderAmount()) && payOutRequestDTO.getOrderAmount().compareTo(BigDecimal.ZERO) == 0)) {
                    calcRateVO = commonService.calcExchangeRate(payOutRequestDTO.getPaymentCurrency(), payOutRequestDTO.getOrderCurrency(),
                            BigDecimal.ZERO, payOutRequestDTO.getPaymentAmount());
                    payOutRequestDTO.setOrderAmount(calcRateVO.getTradeAmount());
                }
                if (StringUtils.isEmpty(payOutRequestDTO.getPaymentAmount())
                        || (!StringUtils.isEmpty(payOutRequestDTO.getPaymentAmount()) && payOutRequestDTO.getPaymentAmount().compareTo(BigDecimal.ZERO) == 0)) {
                    calcRateVO = commonService.calcExchangeRate(payOutRequestDTO.getOrderCurrency(), payOutRequestDTO.getPaymentCurrency(),
                            BigDecimal.ZERO, payOutRequestDTO.getOrderAmount());
                    payOutRequestDTO.setPaymentAmount(calcRateVO.getTradeAmount());
                }
                if (calcRateVO.getExchangeStatus() != null && TradeConstant.SWAP_SUCCESS.equals(calcRateVO.getExchangeStatus())) {
                    //换汇成功
                    orderPayment.setTradeAmount(payOutRequestDTO.getOrderAmount());
                    orderPayment.setPaymentAmount(payOutRequestDTO.getPaymentAmount());
                    orderPayment.setExchangeRate(calcRateVO.getExchangeRate()); //汇率
                    //orderPayment.setExchangeStatus(TradeConstant.SWAP_SUCCESS); //换汇状态
                    //原始汇率
                    orderPayment.setOldExchangeRate(calcRateVO.getOriginalRate());
                    orderPayment.setExchangeTime(calcRateVO.getExchangeTime()); //换汇时间
                    //算费
                    orderPayment = calculateFee(orderPayment, basicsInfoVO);
                    //手续费换汇
                    log.info("-------------------- 手续费换汇开始 ---------------------");
                    CalcRateVO calcRateVO1 = commonService.calcExchangeRate(payOutRequestDTO.getPaymentCurrency(), payOutRequestDTO.getOrderCurrency(),
                            BigDecimal.ZERO, orderPayment.getFee());
                    if (calcRateVO1.getExchangeStatus() != null && TradeConstant.SWAP_SUCCESS.equals(calcRateVO1.getExchangeStatus())) {
                        log.info("-------------------- 手续费换汇成功 ---------------------");
                        orderPayment.setFee(calcRateVO1.getTradeAmount());
                        //换汇状态
                        orderPayment.setExchangeStatus(TradeConstant.SWAP_SUCCESS);
                    } else {
                        log.info("-------------------- 手续费换汇失败 ---------------------");
                        //换汇状态
                        orderPayment.setExchangeStatus(TradeConstant.SWAP_FALID);
                        orderPayment.setPayoutStatus(TradeConstant.PAYMENT_FAIL);
                        orderPayment.setRemark("手续费换汇失败");
                    }
                    list.add(orderPayment);
                } else {
                    //换汇失败
                    orderPayment.setPayoutStatus(TradeConstant.PAYMENT_FAIL);//付款状态
                    orderPayment.setTradeAmount(payOutRequestDTO.getOrderAmount());
                    orderPayment.setPaymentAmount(payOutRequestDTO.getPaymentAmount());
                    orderPayment.setExchangeRate(calcRateVO.getExchangeRate()); //汇率
                    orderPayment.setExchangeStatus(TradeConstant.SWAP_FALID);   //换汇状态
                    orderPayment.setExchangeTime(calcRateVO.getExchangeTime()); //换汇时间
                    orderPayment.setRemark("换汇失败");
                    list.add(orderPayment);
                }
            }
        }
        //校检余额
        for (OrderPayment orderPayment : list) {
            String banlance = redisService.get(key.concat("_").concat(orderPayment.getTradeCurrency()));
            Account account = null;
            if (StringUtils.isEmpty(banlance)) {
                //查询订单买入汇率
                account = accountMapper.getAccount(orderPayment.getInstitutionCode(), orderPayment.getTradeCurrency());
                if (account == null) {
                    log.info("-----------------商户后台汇款接口  校检余额 ----------------未查询到账户信息 InstitutionCode :{}, TradeCurrency: {}",
                            orderPayment.getInstitutionCode(), orderPayment.getTradeCurrency());
                    throw new BusinessException(EResultEnum.ACCOUNT_IS_NOT_EXIST.getCode());
                }
                BigDecimal newBalance1 = account.getSettleBalance().subtract(account.getFreezeBalance()).subtract(orderPayment.getTradeAmount());
                if (newBalance1.compareTo(BigDecimal.ZERO) == -1) {
                    throw new BusinessException(EResultEnum.INSUFFICIENT_ACCOUNT_BALANCE.getCode());
                }
                banlance = String.valueOf(newBalance1);
            } else {
                BigDecimal newBalance2 = new BigDecimal(banlance).subtract(orderPayment.getTradeAmount());
                if (newBalance2.compareTo(BigDecimal.ZERO) == -1) {
                    throw new BusinessException(EResultEnum.INSUFFICIENT_ACCOUNT_BALANCE.getCode());
                }
                banlance = String.valueOf(newBalance2);
            }
            //redis里保存一分钟
            redisService.set(key.concat("_").concat(orderPayment.getTradeCurrency()), banlance, 1 * 60 * 60);
            //商户后台落地时所有订单付款中的订单变为待支付
            if (TradeConstant.PAYMENT_WAIT.equals(orderPayment.getPayoutStatus())) {
                orderPayment.setPayoutStatus(TradeConstant.PAYMENT_START);
            }
        }
        baseResponse.setCode(EResultEnum.SUCCESS.getCode());
        baseResponse.setData(orderPaymentMapper.insertList(list));
        return baseResponse;
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/8/7
     * @Descripate 商户后台审核汇款单接口
     **/
    @Override
    public BaseResponse institutionAudit(String name, String orderPaymentId, boolean enabled, String remark) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setCode(EResultEnum.SUCCESS.getCode());
        baseResponse.setMsg("SUCCESS");
        OrderPayment orderPayment = orderPaymentMapper.selectByPrimaryKey(orderPaymentId);
        if (orderPayment == null || orderPayment.getPayoutStatus() != TradeConstant.PAYMENT_START) {
            throw new BusinessException(EResultEnum.QUERY_ORDER_ERROR.getCode());
        }
        orderPayment.setRemark(remark);
        orderPayment.setModifier(name);
        if (enabled) {
            orderPayment.setPayoutStatus(TradeConstant.PAYMENT_WAIT);
            if(orderPaymentMapper.updateByPrimaryKeySelective(orderPayment)==1){
                FundChangeDTO fundChangeDTO = new FundChangeDTO(orderPayment);
                BaseResponse fundChangeResponse = clearingService.fundChange(fundChangeDTO, null);
                if (fundChangeResponse.getCode() != null && fundChangeResponse.getCode().equals(String.valueOf(AsianWalletConstant.HTTP_SUCCESS_STATUS))) {//请求成功
                    FundChangeVO fundChangeVO = (FundChangeVO) fundChangeResponse.getData();
                    if (!fundChangeVO.getRespCode().equals(TradeConstant.CLEARING_SUCCESS)) {//业务处理失败
                        orderPayment.setPayoutStatus(TradeConstant.PAYMENT_FAIL);
                        orderPayment.setRemark("商户付款接口上报清结算失败");
                        orderPayment.setUpdateTime(new Date());
                        orderPaymentMapper.updateByPrimaryKeySelective(orderPayment);
                    }
                } else {//请求失败
                    orderPayment.setPayoutStatus(TradeConstant.PAYMENT_FAIL);
                    orderPayment.setUpdateTime(new Date());
                    orderPayment.setRemark("商户付款接口上报清结算失败");
                    orderPaymentMapper.updateByPrimaryKeySelective(orderPayment);
                }
            }
        } else {
            orderPayment.setPayoutStatus(TradeConstant.PAYMENT_FAIL);
            orderPaymentMapper.updateByPrimaryKeySelective(orderPayment);
        }
        return baseResponse;
    }


    /**
     * 导出汇款单查询
     *
     * @param orderPaymentExportDTO
     * @return
     */
    @Override
    public List<OrderPayment> exportOrderPayment(OrderPaymentExportDTO orderPaymentExportDTO) {
        return orderPaymentMapper.exportOrderPayment(orderPaymentExportDTO);
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/8/12
     * @Descripate 人工汇款审核汇款单接口
     **/
    @Override
    public BaseResponse artificialPayOutAudit(String name, String orderPaymentId, boolean enabled, String remark) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setCode(EResultEnum.SUCCESS.getCode());
        baseResponse.setMsg("SUCCESS");
        OrderPayment orderPayment = orderPaymentMapper.selectByPrimaryKey(orderPaymentId);
        if (orderPayment == null || orderPayment.getPayoutStatus() != TradeConstant.PAYMENT_WAIT ) {
            throw new BusinessException(EResultEnum.QUERY_ORDER_ERROR.getCode());
        }
        orderPayment.setRemark(remark);
        orderPayment.setModifier(name);
        if (enabled) {
            //分润
            if(!StringUtils.isEmpty(orderPayment.getExtend5())){
                rabbitMQSender.send(AD3MQConstant.MQ_FR_DL, orderPayment.getId());
            }
            orderPayment.setPayoutStatus(TradeConstant.PAYMENT_SUCCESS);
            orderPayment.setUpdateTime(new Date());
            orderPaymentMapper.updateByPrimaryKeySelective(orderPayment);
        } else {
            orderPayment.setPayoutStatus(TradeConstant.PAYMENT_FAIL);
            orderPayment.setUpdateTime(new Date());
            this.faliReconciliation(orderPayment, "人工汇款审核不通过");
        }
        if (!StringUtils.isEmpty(orderPayment.getServerUrl())) {
            try {
                log.info("-------------人工汇款服务器回调方法信息记录------------回调商户服务器开始----------");
                commonService.payOutCallBack(orderPayment);
            } catch (Exception e) {
                log.info("-------------人工汇款服务器回调方法信息记录---------回调商户服务器异常---------- Exception ：{}", e);
            }
        }
        return baseResponse;
    }

}
