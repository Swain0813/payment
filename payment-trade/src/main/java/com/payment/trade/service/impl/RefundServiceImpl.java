package com.payment.trade.service.impl;
import com.alibaba.fastjson.JSON;
import com.payment.common.base.BaseServiceImpl;
import com.payment.common.config.AuditorProvider;
import com.payment.common.constant.AD3Constant;
import com.payment.common.constant.AD3MQConstant;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.constant.TradeConstant;
import com.payment.common.dto.FundChangeDTO;
import com.payment.common.dto.RefundDTO;
import com.payment.common.dto.SearchOrderDTO;
import com.payment.common.dto.SearchOrderExportDTO;
import com.payment.common.entity.*;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.common.utils.DateToolUtils;
import com.payment.common.utils.IDS;
import com.payment.common.vo.FundChangeVO;
import com.payment.trade.channels.ad3Offline.AD3Service;
import com.payment.trade.channels.ad3Online.AD3OnlineAcquireService;
import com.payment.trade.channels.alipay.AliPayService;
import com.payment.trade.channels.megaPay.MegaPayService;
import com.payment.trade.channels.wechat.WechatService;
import com.payment.trade.dao.*;
import com.payment.trade.rabbitmq.RabbitMQSender;
import com.payment.trade.service.ClearingService;
import com.payment.trade.service.CommonService;
import com.payment.trade.service.RefundService;
import com.payment.trade.utils.SettleDateUtil;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


/**
 * @description:
 * @author: YangXu
 * @create: 2019-02-18 10:34
 **/
@Slf4j
@Service
@Transactional
public class RefundServiceImpl extends BaseServiceImpl<OrderRefund> implements RefundService {

    @Autowired
    private TcsCtFlowMapper tcsCtFlowMapper;

    @Autowired
    private TcsStFlowMapper tcsStFlowMapper;

    @Autowired
    private AD3Service ad3Service;

    @Autowired
    private AD3OnlineAcquireService ad3OnlineAcquireService;

    @Autowired
    private OrderRefundMapper refundOrderMapper;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private AuditorProvider auditorProvider;

    @Autowired
    private ClearingService clearingService;

    @Autowired
    private RabbitMQSender rabbitMQSender;

    @Autowired
    private CommonService commonService;

    @Autowired
    private InstitutionProductMapper institutionProductMapper;

    @Autowired
    private ReconciliationMapper reconciliationMapper;

    @Autowired
    private AliPayService aliPayService;

    @Autowired
    private WechatService wechatService;

    @Autowired
    private MegaPayService megaPayService;

    /**
     * @return
     * @Author YangXu
     * @Date 2019/2/28
     * @Descripate 创建退款单
     **/
    public OrderRefund creatOrderRefund(RefundDTO refundDTO, Orders oldOrder) {
        OrderRefund orderRefund = new OrderRefund();
        BeanUtils.copyProperties(oldOrder, orderRefund);
        orderRefund.setLanguage(auditorProvider.getLanguage());//语言
        //检查币种是否一致，若不一致掉换汇接口
        if (!oldOrder.getTradeCurrency().equals(refundDTO.getRefundCurrency())) {
            //调用汇率计算
            BigDecimal tradeAmount = refundDTO.getRefundAmount().multiply(oldOrder.getExchangeRate());
            orderRefund.setExchangeRate(oldOrder.getExchangeRate());//换汇汇率
            orderRefund.setExchangeTime(new Date());//换汇时间
            orderRefund.setExchangeStatus(TradeConstant.SWAP_SUCCESS);//换汇状态
            orderRefund.setTradeAmount(tradeAmount);//实际退款金额
        } else {
            orderRefund.setTradeAmount(refundDTO.getRefundAmount());
        }
        orderRefund.setTradeAmount(orderRefund.getTradeAmount().setScale(2, BigDecimal.ROUND_DOWN));
        orderRefund.setId("R" + IDS.uniqueID());//退款订单号
        orderRefund.setOrderId(oldOrder.getId());//原订单流水号
        orderRefund.setRefundType(refundDTO.getRefundType());//退款类型
        if (refundDTO.getRefundType() == 1) {
            orderRefund.setTradeAmount(oldOrder.getTradeAmount());//全额退款退原订单交易金额
        }
        orderRefund.setInstitutionOrderTime(DateToolUtils.parseDate(refundDTO.getRefundTime(), DateToolUtils.DATE_FORMAT_DATETIME));//商户请求退款时间(商户所在地)
        orderRefund.setAmount(refundDTO.getRefundAmount());//商户请求退款金额
        orderRefund.setOrderCurrency(refundDTO.getRefundCurrency());//客户请求收单币种
        orderRefund.setRefundStatus(TradeConstant.REFUND_WAIT);//待退款
        orderRefund.setDraweeName(refundDTO.getPayerName());//付款人姓名
        orderRefund.setDraweeAccount(refundDTO.getPayerAccount());//付款人账户
        orderRefund.setDraweeBank(refundDTO.getPayerBank());//付款人银行
        orderRefund.setDraweeEmail(refundDTO.getPayerEmail());//付款人邮箱
        orderRefund.setDraweePhone(refundDTO.getPayerPhone());//付款人电话
        //退款时的应结算时间就是当前退款时间
        orderRefund.setProductSettleCycle(SettleDateUtil.getSettleDate(DateToolUtils.formatTimestamp.format(new Date())));
        //备注
        orderRefund.setRemark(null);
        //备注1
        orderRefund.setRemark1(null);
        //备注2
        orderRefund.setRemark2(null);
        //备注3
        orderRefund.setRemark3(null);
        orderRefund.setChannelRate(null);//通道费率
        orderRefund.setChannelFee(null);
        orderRefund.setChannelFeeType(null);
        orderRefund.setChannelGatewayRate(null);//通道网关费率
        orderRefund.setChannelGatewayCharge(null);
        orderRefund.setChannelGatewayFee(null);
        orderRefund.setChannelGatewayFeeType(null);
        orderRefund.setChannelGatewayStatus(null);
        orderRefund.setCreateTime(new Date());
        orderRefund.setUpdateTime(null);
        orderRefund.setCreator(null);
        orderRefund.setModifier(null);
        return orderRefund;
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/2/19
     * @Descripate 校验退款单参数
     **/
    public String checkRefundDTO(Institution institution, RefundDTO refundDTO, Orders oldOrder, BigDecimal oldRefundAmount) {
        //1.商户编号是否存在
        if (institution == null) {
            //商户编号不存在
            log.info("----------------- 校验退款参数 机构信息不存在 -------------- institution : {} ,refundDTO : {},oldOrder :{}", institution, JSON.toJSON(refundDTO), JSON.toJSON(oldOrder));
            throw new BusinessException(EResultEnum.INSTITUTION_NOT_EXIST.getCode());
        }
        if (!TradeConstant.AUDIT_SUCCESS.equals(institution.getAuditStatus())) {
            //商户状态检验
            log.info("----------------- 校验退款参数 商户状态检验不通过 -------------- institution : {} ,refundDTO : {},oldOrder :{}", institution, JSON.toJSON(refundDTO), JSON.toJSON(oldOrder));
            throw new BusinessException(EResultEnum.INSTITUTION_STATUS_ABNORMAL.getCode());
        }
        if (!institution.getEnabled()) {
            //商户状态检验
            log.info("----------------- 校验退款参数 商户启用禁用 -------------- institution : {} ,refundDTO : {},oldOrder :{}", institution, JSON.toJSON(refundDTO), JSON.toJSON(oldOrder));
            throw new BusinessException(EResultEnum.INSTITUTION_STATUS_ABNORMAL.getCode());
        }

        //3.验证退款金额
        BigDecimal accountBalance = accountMapper.getBalance(refundDTO.getInstitutionId(), refundDTO.getRefundCurrency()); //结算账户余额
        accountBalance = accountBalance == null ? BigDecimal.ZERO : accountBalance;
        BigDecimal newRefundAmount = oldRefundAmount.add(refundDTO.getRefundAmount());

        //4.验证原订单交易状态状态(只有交易状态为交易成功和退款)
        Integer CTstatus = tcsCtFlowMapper.getCTstatus(oldOrder.getId());
        Integer STstatus = tcsStFlowMapper.getSTstatus(oldOrder.getId());
        if (TradeConstant.ORDER_CLEAR_SUCCESS.equals(CTstatus) && TradeConstant.ORDER_SETTLE_SUCCESS.equals(STstatus)) {
            //退款
            if (newRefundAmount.compareTo(oldOrder.getAmount()) == 1) {
                log.info("----------------- 校验退款参数 退款金额不合法 -------------- institution : {} ,refundDTO : {},oldOrder :{}", institution, JSON.toJSON(refundDTO), JSON.toJSON(oldOrder));
                throw new BusinessException(EResultEnum.REFUND_AMOUNT_NOT_LEGAL.getCode());
            }
            if (accountBalance.compareTo(refundDTO.getRefundAmount()) == -1) {
                log.info("----------------- 校验退款参数 账户余额不足 -------------- institution : {} ,refundDTO : {},oldOrder :{}", institution, JSON.toJSON(refundDTO), JSON.toJSON(oldOrder));
                throw new BusinessException(EResultEnum.INSUFFICIENT_ACCOUNT_BALANCE.getCode());
            }
            return TradeConstant.RF;
        } else {
            if (refundDTO.getRefundType() == 2) {
                throw new BusinessException(EResultEnum.ORDER_NOT_SETTLE.getCode());
            }
            //撤销
            if (newRefundAmount.compareTo(oldOrder.getAmount()) == 1) {
                log.info("----------------- 校验退款参数 退款金额不合法 -------------- institution : {} ,refundDTO : {},oldOrder :{}", institution, JSON.toJSON(refundDTO), JSON.toJSON(oldOrder));
                throw new BusinessException(EResultEnum.REFUND_AMOUNT_NOT_LEGAL.getCode());
            }

            if (refundDTO.getTradeDirection() == 1) {//线上退款，验证收款人信息
                if (StringUtils.isBlank(refundDTO.getPayerName())) {//付款人姓名
                    throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
                }
                if (StringUtils.isBlank(refundDTO.getPayerAccount())) {//付款人账户
                    throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
                }
                if (StringUtils.isBlank(refundDTO.getPayerBank())) {//付款人银行
                    throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
                }
                if (StringUtils.isBlank(refundDTO.getPayerEmail())) {//付款人邮箱
                    throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
                }
                if (StringUtils.isBlank(refundDTO.getPayerPhone())) {//付款人电话
                    throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
                }
            }
            return TradeConstant.RV;
        }
    }


    /**
     * @return
     * @Author YangXu
     * @Date 2019/2/19
     * @Descripate 退款接口
     * 这块代码业务修改不要忘记修改机构后台管理退款接口在下面(refundOrderSys)
     **/
    @Override
    public BaseResponse refundOrder(RefundDTO refundDTO, String ip) {
        BaseResponse baseResponse = new BaseResponse();
        //输入参数的check
        if (refundDTO.getSign() == null) {//签名必填
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        //验签
        if (TradeConstant.TRADE_UPLINE.equals(refundDTO.getTradeDirection())) {//线下
            if (!commonService.checkOnlineSignMsgUseMD5(refundDTO)) {
                throw new BusinessException(EResultEnum.DECRYPTION_ERROR.getCode());//验签不匹配
            }
        } else {
            if (!commonService.checkOnlineSignMsg(refundDTO)) {
                throw new BusinessException(EResultEnum.DECRYPTION_ERROR.getCode());//验签不匹配
            }
        }
        //查询原订单
        Orders oldOrder = ordersMapper.selectByInstitutionOrderIdAndStatus(refundDTO.getOrderNo(), TradeConstant.ORDER_PAY_SUCCESS);
        //2.判断原订单是否存在
        if (oldOrder == null) {
            //商户订单号不存在
            throw new BusinessException(EResultEnum.ORDER_NOT_EXIST.getCode());
        }
        //线下的单子如果上游通道不支持退款直接报不支持退款
        if (TradeConstant.TRADE_UPLINE.equals(refundDTO.getTradeDirection()) && !this.commonService.getChannelByChannelCode(oldOrder.getChannelCode()).getSupportRefundState()) {
            throw new BusinessException(EResultEnum.NOT_SUPPORT_REFUND.getCode());
        }
        //交易状态 以及撤销状态和退款状态的判断
        if (oldOrder.getCancelStatus() != null) {
            throw new BusinessException(EResultEnum.REFUND_CANCEL_ERROR.getCode());//撤销的单子不能退款--该交易已撤销
        }
        Channel channel = commonService.getChannelByChannelCode(oldOrder.getChannelCode());
        //AD3-eNets退款只能当天退款
        if (channel.getChannelCnName().toLowerCase().contains(TradeConstant.AD3_ENETS)) {
            String channelCallbackTime = DateToolUtils.getReqDate(oldOrder.getChannelCallbackTime());
            String today = DateToolUtils.getReqDate();
            if (!channelCallbackTime.equals(today)) {
                throw new BusinessException(EResultEnum.NOT_SUPPORT_REFUND.getCode());
            }
        }
        //检查是否可以退款
        Institution institution = commonService.getInstitutionInfo(refundDTO.getInstitutionId());
        BigDecimal oldRefundAmount = refundOrderMapper.getTotalAmountByOrderId(oldOrder.getId()); //已退款金额
        oldRefundAmount = oldRefundAmount == null ? BigDecimal.ZERO : oldRefundAmount;
        String type = this.checkRefundDTO(institution, refundDTO, oldOrder, oldRefundAmount);
        BigDecimal newRefundAmount = oldRefundAmount.add(refundDTO.getRefundAmount());
        //创建订单
        OrderRefund orderRefund = creatOrderRefund(refundDTO, oldOrder);
        orderRefund.setReqIp(ip);
        if (newRefundAmount.compareTo(oldOrder.getAmount()) == -1) {
            orderRefund.setRemark2("部分");
        } else if (newRefundAmount.compareTo(oldOrder.getAmount()) == 0) {
            orderRefund.setRemark2("全额");
        }
        //计算退款手续费
        InstitutionProduct institutionProduct = institutionProductMapper.selectByProductCodeAndInstitutionCode(oldOrder.getProductCode(), oldOrder.getInstitutionCode());
        //如果订单币种与产品币种不一致时,需要换汇操作
        if (!orderRefund.getOrderCurrency().equals(orderRefund.getTradeCurrency())) {
            institutionProduct = commonService.CalcFeeExchange(orderRefund.getTradeCurrency(), institutionProduct, new BigDecimal(orderRefund.getCommodityName()), orderRefund.getOrderCurrency());
        }
        BigDecimal poundage = BigDecimal.ZERO;
        if (institutionProduct.getRefundRate() == null || institutionProduct.getRefundRateType() == null) {
            log.info("----------------- 退款 计费失败信息记录 ----------------费率:{},费率类型:{}", institutionProduct.getRefundRate(), institutionProduct.getRefundRateType());
            throw new BusinessException(EResultEnum.SYS_ERROR_CREATE_ORDER_FAIL.getCode());
        }
        //单笔费率
        if (institutionProduct.getRefundRateType().equals(TradeConstant.FEE_TYPE_RATE)) {
            //手续费=订单金额*单笔费率
            poundage = refundDTO.getRefundAmount().multiply(institutionProduct.getRefundRate());
            //判断手续费是否小于最小值，大于最大值
            if (institutionProduct.getRefundMinTate() != null && poundage.compareTo(institutionProduct.getRefundMinTate()) == -1) {
                poundage = institutionProduct.getRefundMinTate();
            }
            if (institutionProduct.getRefundMaxTate() != null && poundage.compareTo(institutionProduct.getRefundMaxTate()) == 1) {
                poundage = institutionProduct.getRefundMaxTate();
            }
        }
        //单笔定额
        if (institutionProduct.getRefundRateType().equals(TradeConstant.FEE_TYPE_QUOTA)) {
            //手续费=单笔定额值
            poundage = institutionProduct.getRefundRate();
        }
        //判断结算户金额
        Account account = accountMapper.getAccount(oldOrder.getInstitutionCode(), refundDTO.getRefundCurrency());
        BigDecimal add = refundDTO.getRefundAmount().add(poundage);
        //小于
        if (account.getSettleBalance().compareTo(add) == -1) {
            log.info("-------------退款失败 结算金额小于退款金额+手续费-------------机构号:{},结算户余额:{},币种:{},手续费+退款金额:{}",
                    refundDTO.getInstitutionId(), account.getSettleBalance(), refundDTO.getRefundCurrency(), add.toString());
            throw new BusinessException(EResultEnum.INSUFFICIENT_ACCOUNT_BALANCE.getCode());
        }
        //设置退款手续费
        orderRefund.setRefundFee(poundage);
        //把原订单状态改为退款
        ordersMapper.updateOrderRefundStatus(refundDTO.getOrderNo(), TradeConstant.ORDER_REFUND_WAIT);
        //检查通道是否支持系统退款
        if (channel.getSupportRefundState()) {
            orderRefund.setRefundMode(TradeConstant.REFUND_MODE_AUTO);//退款方式 1：系统退款 2：人工退款
            refundOrderMapper.insert(orderRefund);
        } else {
            log.info("==============refundOrder该通道不支持退款=============");
            orderRefund.setRefundMode(TradeConstant.REFUND_MODE_PERSON);//退款方式 1：系统退款 2：人工退款
            //人工退款上报清结算
            FundChangeDTO fundChangeDTO = new FundChangeDTO(type, orderRefund);
            fundChangeDTO.setShouldDealtime(orderRefund.getProductSettleCycle());//应结日期
            BaseResponse cFundChange = clearingService.fundChange(fundChangeDTO, null);
            if (cFundChange.getCode().equals(String.valueOf(AsianWalletConstant.HTTP_SUCCESS_STATUS))) {//请求成功
                FundChangeVO fundChangeVO = (FundChangeVO) cFundChange.getData();
                if (!fundChangeVO.getRespCode().equals(TradeConstant.CLEARING_SUCCESS)) {
                    orderRefund.setRefundStatus(TradeConstant.REFUND_FALID);
                    orderRefund.setRemark("上报清结算失败");
                }
            } else {//请求失败
                orderRefund.setRefundStatus(TradeConstant.REFUND_FALID);
                orderRefund.setRemark("上报清结算失败");
            }
            refundOrderMapper.insert(orderRefund);
            return baseResponse;
        }
        //获取原订单的refCode字段(NextPos用)
        orderRefund.setSign(oldOrder.getSign());
        if (type.equals(TradeConstant.RF)) {
            this.doRefundOrder(baseResponse, orderRefund);
        } else {
            this.doCancelOrder(baseResponse, orderRefund);
        }
        return baseResponse;
    }


    /**
     * 后台系统和机构系统退款订单接口
     * 给机构后台系统用的 实现和refundOrder是一样的两边修改一定要考虑这个地方
     *
     * @param refundDTO
     * @param ip
     * @return
     */
    @Override
    public BaseResponse refundOrderSys(RefundDTO refundDTO, String ip) {
        BaseResponse baseResponse = new BaseResponse();
        //查询原订单
        Orders oldOrder = ordersMapper.selectByInstitutionOrderIdAndStatus(refundDTO.getOrderNo(), TradeConstant.ORDER_PAY_SUCCESS);
        //2.判断原订单是否存在
        if (oldOrder == null) {
            //商户订单号不存在
            throw new BusinessException(EResultEnum.ORDER_NOT_EXIST.getCode());
        }
        //线下的单子如果上游通道不支持退款直接报不支持退款
        if (TradeConstant.TRADE_UPLINE.equals(refundDTO.getTradeDirection()) && !this.commonService.getChannelByChannelCode(oldOrder.getChannelCode()).getSupportRefundState()) {
            throw new BusinessException(EResultEnum.NOT_SUPPORT_REFUND.getCode());
        }
        //交易状态 以及撤销状态和退款状态的判断
        if (oldOrder.getCancelStatus() != null) {
            throw new BusinessException(EResultEnum.REFUND_CANCEL_ERROR.getCode());//撤销的单子不能退款--该交易已撤销
        }
        Channel channel = commonService.getChannelByChannelCode(oldOrder.getChannelCode());
        //AD3-eNets退款只能当天退款
        if (channel.getChannelCnName().toLowerCase().contains(TradeConstant.AD3_ENETS)) {
            String channelCallbackTime = DateToolUtils.getReqDate(oldOrder.getChannelCallbackTime());
            String today = DateToolUtils.getReqDate();
            if (!channelCallbackTime.equals(today)) {
                throw new BusinessException(EResultEnum.NOT_SUPPORT_REFUND.getCode());
            }
        }
        //检查是否可以退款
        Institution institution = commonService.getInstitutionInfo(refundDTO.getInstitutionId());
        BigDecimal oldRefundAmount = refundOrderMapper.getTotalAmountByOrderId(oldOrder.getId()); //已退款金额
        oldRefundAmount = oldRefundAmount == null ? BigDecimal.ZERO : oldRefundAmount;
        String type = this.checkRefundDTO(institution, refundDTO, oldOrder, oldRefundAmount);
        BigDecimal newRefundAmount = oldRefundAmount.add(refundDTO.getRefundAmount());
        //创建订单
        OrderRefund orderRefund = creatOrderRefundSys(refundDTO, oldOrder);
        log.info("---------------后台系统和机构系统退款订单OrderRefund---------------orderRefund:{}", JSON.toJSONString(orderRefund));
        orderRefund.setReqIp(ip);
        if (newRefundAmount.compareTo(oldOrder.getAmount()) == -1) {
            orderRefund.setRemark2("部分");
        } else if (newRefundAmount.compareTo(oldOrder.getAmount()) == 0) {
            orderRefund.setRemark2("全额");
        }
        //计算手续费
        InstitutionProduct institutionProduct = institutionProductMapper.selectByProductCodeAndInstitutionCode(oldOrder.getProductCode(), oldOrder.getInstitutionCode());
        //如果订单币种与产品币种不一致时,需要换汇操作
        if (!orderRefund.getOrderCurrency().equals(orderRefund.getTradeCurrency())) {
            institutionProduct = commonService.CalcFeeExchange(orderRefund.getTradeCurrency(), institutionProduct, new BigDecimal(orderRefund.getCommodityName()), orderRefund.getOrderCurrency());
        }
        BigDecimal poundage = BigDecimal.ZERO;
        if (institutionProduct.getRefundRate() == null || institutionProduct.getRefundRateType() == null) {
            log.info("-----------------后台系统和机构系统退款订单接口 退款 计费失败信息记录 ----------------费率:{},费率类型:{}", institutionProduct.getRefundRate(), institutionProduct.getRefundRateType());
            throw new BusinessException(EResultEnum.SYS_ERROR_CREATE_ORDER_FAIL.getCode());
        }
        //单笔费率
        if (institutionProduct.getRefundRateType().equals(TradeConstant.FEE_TYPE_RATE)) {
            //手续费=订单金额*单笔费率
            poundage = refundDTO.getRefundAmount().multiply(institutionProduct.getRefundRate());
            //判断手续费是否小于最小值，大于最大值
            if (institutionProduct.getRefundMinTate() != null && poundage.compareTo(institutionProduct.getRefundMinTate()) == -1) {
                poundage = institutionProduct.getRefundMinTate();
            }
            if (institutionProduct.getRefundMaxTate() != null && poundage.compareTo(institutionProduct.getRefundMaxTate()) == 1) {
                poundage = institutionProduct.getRefundMaxTate();
            }
        }
        //单笔定额
        if (institutionProduct.getRefundRateType().equals(TradeConstant.FEE_TYPE_QUOTA)) {
            //手续费=单笔定额值
            poundage = institutionProduct.getRefundRate();
        }
        //判断结算户金额
        Account account = accountMapper.getAccount(oldOrder.getInstitutionCode(), refundDTO.getRefundCurrency());
        BigDecimal add = refundDTO.getRefundAmount().add(poundage);
        //小于
        if (account.getSettleBalance().compareTo(refundDTO.getRefundAmount().add(poundage)) == -1) {
            log.info("-------------后台系统和机构系统退款订单接口 退款失败 结算金额小于退款金额+手续费-------------机构号:{},结算户余额:{},币种:{},手续费+退款金额:{}",
                    refundDTO.getInstitutionId(), account.getSettleBalance(), refundDTO.getRefundCurrency(), add.toString());
            throw new BusinessException(EResultEnum.INSUFFICIENT_ACCOUNT_BALANCE.getCode());
        }
        //设置退款手续费
        orderRefund.setRefundFee(poundage);
        //把原订单状态改为退款
        ordersMapper.updateOrderRefundStatus(refundDTO.getOrderNo(), TradeConstant.ORDER_REFUND_WAIT);
        //检查通道是否支持系统退款
        if (channel.getSupportRefundState()) {
            orderRefund.setRefundMode(TradeConstant.REFUND_MODE_AUTO);//退款方式 1：系统退款 2：人工退款
            refundOrderMapper.insert(orderRefund);
        } else {
            orderRefund.setRefundMode(TradeConstant.REFUND_MODE_PERSON);//退款方式 1：系统退款 2：人工退款
            //人工退款上报清结算
            FundChangeDTO fundChangeDTO = new FundChangeDTO(type, orderRefund);
            fundChangeDTO.setShouldDealtime(orderRefund.getProductSettleCycle());//应结日期
            BaseResponse cFundChange = clearingService.fundChange(fundChangeDTO, null);
            if (cFundChange.getCode().equals(String.valueOf(AsianWalletConstant.HTTP_SUCCESS_STATUS))) {//请求成功
                FundChangeVO fundChangeVO = (FundChangeVO) cFundChange.getData();
                if (!fundChangeVO.getRespCode().equals(TradeConstant.CLEARING_SUCCESS)) {
                    orderRefund.setRefundStatus(TradeConstant.REFUND_FALID);
                    orderRefund.setRemark("后台系统和机构系统退款订单接口上报清结算失败");
                }
            } else {//请求失败
                orderRefund.setRefundStatus(TradeConstant.REFUND_FALID);
                orderRefund.setRemark("后台系统和机构系统退款订单接口上报清结算失败");
            }
            refundOrderMapper.insert(orderRefund);
            return baseResponse;
        }
        //获取原订单的refCode字段(NextPos用)
        orderRefund.setSign(oldOrder.getSign());
        if (type.equals(TradeConstant.RF)) {
            this.doRefundOrder(baseResponse, orderRefund);
        } else {
            this.doCancelOrder(baseResponse, orderRefund);
        }
        return baseResponse;
    }


    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/14
     * @Descripate 退款操作
     **/
    @Override
    public void doRefundOrder(BaseResponse baseResponse, OrderRefund orderRefund) {
        log.info("----------------- 退款操作 退款操作 -------------- orderRefund : {}", JSON.toJSON(orderRefund));
        FundChangeDTO fundChangeDTO = new FundChangeDTO(TradeConstant.RF, orderRefund);
        fundChangeDTO.setShouldDealtime(orderRefund.getProductSettleCycle());//应结日期
        BaseResponse cFundChange = clearingService.fundChange(fundChangeDTO, null);
        if (cFundChange.getCode().equals(String.valueOf(AsianWalletConstant.HTTP_SUCCESS_STATUS))) {//请求成功
            FundChangeVO fundChangeVO = (FundChangeVO) cFundChange.getData();
            if (!fundChangeVO.getRespCode().equals(TradeConstant.CLEARING_SUCCESS)) {
                log.info("**************** 退款操作doRefundOrder 上报队列 MQ_TK_SBQJSSB_DL *************** orderRefund : {}", JSON.toJSON(orderRefund));
                RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orderRefund));
                rabbitMQSender.sendSleep(AD3MQConstant.MQ_TK_SBQJSSB_DL, JSON.toJSONString(rabbitMassage));
                baseResponse.setMsg(EResultEnum.REFUNDING.getCode());
                return;
            }
        } else {//请求失败
            log.info("----------------- 退款操作doRefundOrder 上报队列 MQ_TK_SBQJSSB_DL -------------- orderRefund : {}", JSON.toJSON(orderRefund));
            RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orderRefund));
            rabbitMQSender.sendSleep(AD3MQConstant.MQ_TK_SBQJSSB_DL, JSON.toJSONString(rabbitMassage));
            baseResponse.setMsg(EResultEnum.REFUNDING.getCode());
            return;
        }
        //获取通道信息
        Channel channel = this.commonService.getChannelByChannelCode(orderRefund.getChannelCode());
        if (channel.getChannelEnName().equalsIgnoreCase(AD3Constant.AD3_ONLINE)) {//线上退款
            ad3OnlineAcquireService.doUsRefundInRef(baseResponse, fundChangeDTO, orderRefund);
        } else if (channel.getChannelEnName().equalsIgnoreCase(AD3Constant.AD3_OFFLINE)) {//线下退款
            ad3Service.doUsRefundInRef(baseResponse, orderRefund, fundChangeDTO);
        } else if (channel.getChannelEnName().equalsIgnoreCase(TradeConstant.ALIPAY_CSB_ONLINE) ||
                channel.getChannelEnName().equalsIgnoreCase(TradeConstant.ALIPAY_BSC_OFFLINE) ||
                channel.getChannelEnName().equalsIgnoreCase(TradeConstant.ALIPAY_CSB_OFFLINE)) {
            aliPayService.aliPayRefund(orderRefund, fundChangeDTO, baseResponse);
        } else if (channel.getChannelEnName().equalsIgnoreCase(TradeConstant.WECHAT_CSB_ONLINE) ||
                channel.getChannelEnName().equalsIgnoreCase(TradeConstant.WECHAT_CSB_OFFLINE) ||
                channel.getChannelEnName().equalsIgnoreCase(TradeConstant.WECHAT_BSC_OFFLINE)) {
            wechatService.wechatRefund(orderRefund, fundChangeDTO, baseResponse);
        } else if (channel.getChannelEnName().equalsIgnoreCase(TradeConstant.NEXTPOS_ONLINE) ||
                channel.getChannelEnName().equalsIgnoreCase(TradeConstant.NEXTPOS_CSB_OFFLINE)) {
            //nextPos通道退款
            megaPayService.megaPayNextPosRefund(orderRefund, fundChangeDTO, baseResponse);
        }
    }


    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/14
     * @Descripate 撤销操作
     **/
    @Override
    public void doCancelOrder(BaseResponse baseResponse, OrderRefund orderRefund) {
        log.info("----------------- 撤销操作 撤销操作 --------------  orderRefund : {}", JSON.toJSON(orderRefund));
        //上报清结算撤销
        FundChangeDTO fundChangeDTO = new FundChangeDTO(TradeConstant.RV, orderRefund);
        fundChangeDTO.setShouldDealtime(orderRefund.getProductSettleCycle());//应结日期
        BaseResponse cFundChange = clearingService.fundChange(fundChangeDTO, null);
        if (cFundChange.getCode().equals(String.valueOf(AsianWalletConstant.HTTP_SUCCESS_STATUS))) {//请求成功
            FundChangeVO fundChangeVO = (FundChangeVO) cFundChange.getData();
            if (!fundChangeVO.getRespCode().equals(TradeConstant.CLEARING_SUCCESS)) {//业务处理失败
                log.info("----------------- 撤销操作doCancelOrder 撤销上报清结算失败 -------------- cFundChange : {}", JSON.toJSON(cFundChange));
                RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orderRefund));
                log.info("----------------- 撤销操作doCancelOrder 上报队列 MQ_CX_SBQJSSB_DL -------------- orderRefund : {}", JSON.toJSONString(orderRefund));
                rabbitMQSender.sendSleep(AD3MQConstant.MQ_CX_SBQJSSB_DL, JSON.toJSONString(rabbitMassage));
                baseResponse.setMsg(EResultEnum.REFUNDING.getCode());
                return;
            }
        } else {//请求失败
            log.info("----------------- 撤销操作 撤销上报清结算失败 -------------- cFundChange : {}", JSON.toJSON(cFundChange));
            RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orderRefund));
            log.info("----------------- 撤销操作 上报队列 MQ_CX_SBQJSSB_DL -------------- orderRefund : {}", JSON.toJSONString(orderRefund));
            rabbitMQSender.sendSleep(AD3MQConstant.MQ_CX_SBQJSSB_DL, JSON.toJSONString(rabbitMassage));
            baseResponse.setMsg(EResultEnum.REFUNDING.getCode());
            return;
        }
        //上报上游退款通道
        Channel channel = this.commonService.getChannelByChannelCode(orderRefund.getChannelCode());
        RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orderRefund));
        if (channel.getChannelEnName().equalsIgnoreCase(AD3Constant.AD3_ONLINE)) {//线上退款
            ad3OnlineAcquireService.doUsCancelInRef(baseResponse, orderRefund, rabbitMassage);
        } else if (channel.getChannelEnName().equalsIgnoreCase(AD3Constant.AD3_OFFLINE)) {//线下退款
            ad3Service.doUsCancelInRef(baseResponse, orderRefund, rabbitMassage);
        } else if (channel.getChannelEnName().equalsIgnoreCase(TradeConstant.ALIPAY_CSB_ONLINE) ||
                channel.getChannelEnName().equalsIgnoreCase(TradeConstant.ALIPAY_BSC_OFFLINE) ||
                channel.getChannelEnName().equalsIgnoreCase(TradeConstant.ALIPAY_CSB_OFFLINE)) {
            aliPayService.aliPayCancel(orderRefund, baseResponse, rabbitMassage);
        } else if (channel.getChannelEnName().equalsIgnoreCase(TradeConstant.WECHAT_CSB_ONLINE) ||
                channel.getChannelEnName().equalsIgnoreCase(TradeConstant.WECHAT_CSB_OFFLINE) ||
                channel.getChannelEnName().equalsIgnoreCase(TradeConstant.WECHAT_BSC_OFFLINE)) {
            wechatService.wechatCancel(orderRefund, baseResponse, rabbitMassage);
        } else if (channel.getChannelEnName().equalsIgnoreCase(TradeConstant.NEXTPOS_ONLINE) ||
                channel.getChannelEnName().equalsIgnoreCase(TradeConstant.NEXTPOS_CSB_OFFLINE)) {
            //nextPos通道撤销
            megaPayService.megaPayNextPosCancel(orderRefund, baseResponse, rabbitMassage);
        }
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/4
     * @Descripate 人工退款接口
     **/
    @Override
    public void artificialRefund(String name, String refundOrderId, Boolean enabled, String remark) {
        OrderRefund orderRefund = refundOrderMapper.selectByPrimaryKey(refundOrderId);
        if (enabled) {//审核通过
            //退款成功
            refundOrderMapper.updateStatuts(orderRefund.getId(), TradeConstant.REFUND_SUCCESS, null, remark);
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
        } else {//审核不通过
            //退款失败
            refundOrderMapper.updateStatuts(orderRefund.getId(), TradeConstant.REFUND_FALID, null, remark);
            //创建调账记录
            Reconciliation reconciliation = commonService.createReconciliation(orderRefund, TradeConstant.REFUND_FAIL_RECONCILIATION);
            reconciliationMapper.insert(reconciliation);
            FundChangeDTO fundChangeDTO = new FundChangeDTO(TradeConstant.AA, orderRefund);
            fundChangeDTO.setShouldDealtime(DateToolUtils.formatTimestamp.format(new Date()));//应结日期
            fundChangeDTO.setRefcnceFlow(reconciliation.getId());
            fundChangeDTO.setSysorderid(orderRefund.getId());
            fundChangeDTO.setTradetype(TradeConstant.AA);
            fundChangeDTO.setTxnamount(String.valueOf(orderRefund.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP)));
            fundChangeDTO.setSltamount(String.valueOf(orderRefund.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP)));
            fundChangeDTO.setBalancetype(TradeConstant.NORMAL_FUND);
            fundChangeDTO.setSignMsg(null);
            BaseResponse cFundChange = clearingService.fundChange(fundChangeDTO, null);
            if (!cFundChange.getCode().equals(String.valueOf(AsianWalletConstant.HTTP_SUCCESS_STATUS))) {//请求失败
                orderRefund.setRemark3(JSON.toJSONString(fundChangeDTO));
                RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orderRefund));
                log.info("----------------- 退款操作 上报队列 MQ_QJS_TZSB_DL -------------- rabbitMassage : {} ", JSON.toJSON(rabbitMassage));
                rabbitMQSender.send(AD3MQConstant.MQ_QJS_TZSB_DL, JSON.toJSONString(rabbitMassage));
            } else {//请求成功
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
                    log.info("----------------- 退款操作 上报队列 MQ_QJS_TZSB_DL -------------- rabbitMassage : {} ", JSON.toJSON(rabbitMassage));
                    rabbitMQSender.send(AD3MQConstant.MQ_QJS_TZSB_DL, JSON.toJSONString(rabbitMassage));
                }
            }
        }
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/4
     * @Descripate 重复请求退款接口
     **/
    @Override
    public String repeatRefund(String name, String refundOrderId) {
        OrderRefund orderRefund = refundOrderMapper.selectByPrimaryKey(refundOrderId);
        //上报上游退款通道
        Channel channel = this.commonService.getChannelByChannelCode(orderRefund.getChannelCode());
        if (channel.getChannelEnName().equalsIgnoreCase(AD3Constant.AD3_ONLINE)) {//线上退款
            return ad3OnlineAcquireService.repeatRefund(name, orderRefund);
        } else if (channel.getChannelEnName().equalsIgnoreCase(AD3Constant.AD3_OFFLINE)) {//线下退款
            return ad3Service.repeatRefund(name, orderRefund);
        }
        return null;
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/4
     * @Descripate 分页查询退款接口
     **/
    @Override
    public PageInfo<OrderRefund> pageRefundOrder(SearchOrderDTO searchOrderDTO) {
        return new PageInfo<OrderRefund>(refundOrderMapper.pageRefundOrder(searchOrderDTO));
    }

    /**
     * @param searchOrderDTO
     * @return
     * @Author YangXu
     * @Date 2019/3/4
     * @Descripate 导出查询退款接口
     */
    @Override
    public List<OrderRefund> exportRefundOrder(SearchOrderExportDTO searchOrderDTO) {
        if (org.springframework.util.StringUtils.isEmpty(searchOrderDTO.getPageNum()) || org.springframework.util.StringUtils.isEmpty(searchOrderDTO.getPageSize())) {
            searchOrderDTO.setPageNum(1);
            searchOrderDTO.setPageSize(30);
        }
        return refundOrderMapper.exportRefundOrder(searchOrderDTO);
    }

    /**
     * 机构退款设置属性值
     *
     * @param refundDTO
     * @param oldOrder
     * @return
     */
    public OrderRefund creatOrderRefundSys(RefundDTO refundDTO, Orders oldOrder) {
        OrderRefund orderRefund = new OrderRefund();
        BeanUtils.copyProperties(oldOrder, orderRefund);
        orderRefund.setLanguage(auditorProvider.getLanguage());//语言
        //检查币种是否一致，若不一致掉换汇接口
        if (!oldOrder.getTradeCurrency().equals(refundDTO.getRefundCurrency())) {
            //调用汇率计算
            BigDecimal tradeAmount = refundDTO.getRefundAmount().multiply(oldOrder.getExchangeRate());
            orderRefund.setExchangeRate(oldOrder.getExchangeRate());//换汇汇率
            orderRefund.setExchangeTime(new Date());//换汇时间
            orderRefund.setExchangeStatus(TradeConstant.SWAP_SUCCESS);//换汇状态
            orderRefund.setTradeAmount(tradeAmount);//实际退款金额
        } else {
            orderRefund.setTradeAmount(refundDTO.getRefundAmount());
        }
        orderRefund.setTradeAmount(orderRefund.getTradeAmount().setScale(2, BigDecimal.ROUND_DOWN));
        orderRefund.setId("R" + IDS.uniqueID());//退款订单号
        orderRefund.setOrderId(oldOrder.getId());//原订单流水号
        orderRefund.setRefundType(refundDTO.getRefundType());//退款类型
        if (refundDTO.getRefundType() == 1) {
            orderRefund.setTradeAmount(oldOrder.getTradeAmount());//全额退款退原订单交易金额
        }
        orderRefund.setInstitutionOrderTime(DateToolUtils.parseDate(refundDTO.getRefundTime(), DateToolUtils.DATE_FORMAT_DATETIME));//商户请求退款时间(商户所在地)
        orderRefund.setAmount(refundDTO.getRefundAmount());//商户请求退款金额
        orderRefund.setOrderCurrency(refundDTO.getRefundCurrency());//客户请求收单币种
        orderRefund.setRefundStatus(TradeConstant.REFUND_WAIT);//待退款
        orderRefund.setDraweeName(refundDTO.getPayerName());//付款人姓名
        orderRefund.setDraweeAccount(refundDTO.getPayerAccount());//付款人账户
        orderRefund.setDraweeBank(refundDTO.getPayerBank());//付款人银行
        orderRefund.setDraweeEmail(refundDTO.getPayerEmail());//付款人邮箱
        orderRefund.setDraweePhone(refundDTO.getPayerPhone());//付款人电话
        orderRefund.setChannelRate(null);//通道费率
        orderRefund.setChannelFee(null);
        orderRefund.setChannelFeeType(null);
        //退款时的应结算时间就是当前退款时间
        orderRefund.setProductSettleCycle(SettleDateUtil.getSettleDate(DateToolUtils.formatTimestamp.format(new Date())));
        //备注
        orderRefund.setRemark(null);
        //备注1
        orderRefund.setRemark1(null);
        //备注2
        orderRefund.setRemark2(null);
        //备注3
        orderRefund.setRemark3(null);
        orderRefund.setChannelGatewayRate(null);//通道网关费率
        orderRefund.setChannelGatewayCharge(null);
        orderRefund.setChannelGatewayFee(null);
        orderRefund.setChannelGatewayFeeType(null);
        orderRefund.setChannelGatewayStatus(null);
        orderRefund.setCreateTime(new Date());//创建时间
        orderRefund.setUpdateTime(new Date());//修改时间
        orderRefund.setCreator(refundDTO.getModifier());//创建人
        orderRefund.setModifier(refundDTO.getModifier());//修改人
        return orderRefund;
    }

}
