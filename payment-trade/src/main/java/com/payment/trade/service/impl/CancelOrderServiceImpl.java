package com.payment.trade.service.impl;
import com.alibaba.fastjson.JSON;
import com.payment.common.constant.AD3Constant;
import com.payment.common.constant.AD3MQConstant;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.constant.TradeConstant;
import com.payment.common.dto.FundChangeDTO;
import com.payment.common.entity.*;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.common.utils.DateToolUtils;
import com.payment.common.utils.IDS;
import com.payment.common.vo.FundChangeVO;
import com.payment.trade.channels.ad3Offline.AD3Service;
import com.payment.trade.channels.alipay.AliPayService;
import com.payment.trade.channels.megaPay.MegaPayService;
import com.payment.trade.channels.wechat.WechatService;
import com.payment.trade.dao.*;
import com.payment.trade.dto.UndoDTO;
import com.payment.trade.rabbitmq.RabbitMQSender;
import com.payment.trade.service.CancelOrderService;
import com.payment.trade.service.ClearingService;
import com.payment.trade.service.CommonService;
import com.payment.trade.utils.SettleDateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 撤销订单服务
 */
@Service
@Transactional
@Slf4j
public class CancelOrderServiceImpl implements CancelOrderService {

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private RabbitMQSender rabbitMQSender;

    @Autowired
    private CommonService commonService;

    @Autowired
    private OrderRefundMapper refundOrderMapper;

    @Autowired
    private TcsCtFlowMapper tcsCtFlowMapper;

    @Autowired
    private TcsStFlowMapper tcsStFlowMapper;

    @Autowired
    private ClearingService clearingService;

    @Autowired
    private AD3Service ad3Service;

    @Autowired
    private DeviceBindingMapper deviceBindingMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private AliPayService aliPayService;

    @Autowired
    private WechatService wechatService;

    @Autowired
    private InstitutionProductMapper institutionProductMapper;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private MegaPayService megaPayService;

    /**
     * 撤销当前指定的订单
     *
     * @param undoDTO
     * @return
     */
    @Override
    public BaseResponse undo(UndoDTO undoDTO) {
        BaseResponse baseResponse = new BaseResponse();
        //根据机构code获取机构信息
        Institution institution = commonService.getInstitutionInfo(undoDTO.getInstitutionId());
        //撤销订单的业务check
        this.checkOrderInfo(undoDTO, institution);
        //根据订单号获取订单信息
        Orders order = ordersMapper.selectByInstitutionOrderId(undoDTO.getOrderNo());
        //订单不存在的场合
        if (order == null) {
            log.info("**************** 校验撤销订单参数 订单信息不存在**************** order : {}", JSON.toJSON(undoDTO));
            throw new BusinessException(EResultEnum.ORDER_NOT_EXIST.getCode());
        }
        //防止线上订单调用该接口或者阻止调用退款的接口后再调用撤销接口
        if (TradeConstant.TRADE_ONLINE.equals(order.getTradeDirection()) || order.getRefundStatus() != null) {//线上订单调用该撤销接口
            throw new BusinessException(EResultEnum.ONLINE_ORDER_IS_NOT_ALLOW_UNDO.getCode());
        }
        //线下的单子如果上游通道不支持退款直接报不支持退款
        if (!this.commonService.getChannelByChannelCode(order.getChannelCode()).getSupportRefundState()) {
            throw new BusinessException(EResultEnum.ONLINE_ORDER_IS_NOT_ALLOW_UNDO.getCode());
        }
        //撤销中订单判断,如果是撤销中的订单，不能再撤销
        if (TradeConstant.ORDER_CANNELING.equals(order.getCancelStatus())) {//撤销中的订单不能再撤销
            throw new BusinessException(EResultEnum.CANCEL_ORDER_RUNNING_IS_NOT_UNDO.getCode());
        }
        //撤销状态是已经撤销成功的场合，不能再撤销
        if (TradeConstant.ORDER_CANNEL_SUCCESS.equals(order.getCancelStatus())) {//撤销成功的订单不能再撤销
            throw new BusinessException(EResultEnum.CANCEL_ORDER_SUCCESS_IS_NOT_UNDO.getCode());
        }
        //验签
        if (!commonService.checkOnlineSignMsgUseMD5(undoDTO)) {
            throw new BusinessException(EResultEnum.DECRYPTION_ERROR.getCode());
        }
        //根据订单里的通道code判断去查那个通道接口
        Channel channel = this.commonService.getChannelByChannelCode(order.getChannelCode());
        //AD3-eNets退款只能当天不能撤销
        if (channel.getChannelCnName().toLowerCase().contains(TradeConstant.AD3_ENETS)) {
            String channelCallbackTime = DateToolUtils.getReqDate(order.getChannelCallbackTime());
            String today = DateToolUtils.getReqDate();
            if (!channelCallbackTime.equals(today)) {
                throw new BusinessException(EResultEnum.ONLINE_ORDER_IS_NOT_ALLOW_UNDO.getCode());
            }
        }
        //撤销时的设备编号以及操作人为退款单创建时设备编号和设备操作员准备
        order.setDeviceCode(undoDTO.getTerminalId());//设备编号
        order.setDeviceOperator(undoDTO.getOperatorId());//设备操作员
        //撤销操作先更新状态-撤销中
        ordersMapper.updateOrderCancelStatus(undoDTO.getOrderNo(), undoDTO.getOperatorId(), TradeConstant.ORDER_CANNELING);
        //订单是交易成功的场合
        if (TradeConstant.ORDER_PAY_SUCCESS.equals(order.getTradeStatus())) {
            //获取清算状态
            Integer ctState = tcsCtFlowMapper.getCTstatus(order.getId());
            //获取结算状态
            Integer stState = tcsStFlowMapper.getSTstatus(order.getId());
            if (TradeConstant.ORDER_CLEAR_SUCCESS.equals(ctState) && TradeConstant.ORDER_SETTLE_SUCCESS.equals(stState)) {//清算状态是已清算和已结算的场合
                //调用退款操作
                this.refund(order);
            } else {
                //调用撤销操作
                this.cancelOrder(order);
            }
        } else if (TradeConstant.ORDER_PAYING.equals(order.getTradeStatus())) {//订单是付款中的场合
            RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(order));
            if (channel.getChannelEnName().equalsIgnoreCase(AD3Constant.AD3_OFFLINE)) {//AD3线下通道查询接口
                ad3Service.cancelRefunding(order, rabbitMassage);
            } else if (channel.getChannelEnName().equalsIgnoreCase(TradeConstant.ALIPAY_CSB_ONLINE) ||
                    channel.getChannelEnName().equalsIgnoreCase(TradeConstant.ALIPAY_BSC_OFFLINE) ||
                    channel.getChannelEnName().equalsIgnoreCase(TradeConstant.ALIPAY_CSB_OFFLINE)) {
                aliPayService.cancelRefunding(order, rabbitMassage);
            } else if (channel.getChannelEnName().equalsIgnoreCase(TradeConstant.WECHAT_CSB_ONLINE) ||
                    channel.getChannelEnName().equalsIgnoreCase(TradeConstant.WECHAT_CSB_OFFLINE) ||
                    channel.getChannelEnName().equalsIgnoreCase(TradeConstant.WECHAT_BSC_OFFLINE)) {
                wechatService.cancelRefunding(order, rabbitMassage);
            } else if (channel.getChannelEnName().equalsIgnoreCase(TradeConstant.NEXTPOS_ONLINE) ||
                    channel.getChannelEnName().equalsIgnoreCase(TradeConstant.NEXTPOS_CSB_OFFLINE)) {
                megaPayService.cancelRefunding(order, rabbitMassage);
            }
            baseResponse.setCode(EResultEnum.CANCEL_ORDER_RUNNING.getCode());//受理成功，撤销中
            return baseResponse;
        } else {
            baseResponse.setCode(EResultEnum.ORDER_STATUS_IS_WRONG.getCode());//订单交易状态不合法
            return baseResponse;
        }
        return baseResponse;
    }

    /**
     * 撤销订单的业务check
     *
     * @param institution
     */
    private void checkOrderInfo(UndoDTO undoDTO, Institution institution) {
        //机构编号不存在的场合
        if (institution == null) {
            //机构编号不存在
            log.info("**************** 校验撤销订单参数 机构信息不存在**************** institution : {}", JSON.toJSON(undoDTO));
            throw new BusinessException(EResultEnum.INSTITUTION_NOT_EXIST.getCode());
        }
        //机构状态check-审核状态
        if (!TradeConstant.AUDIT_SUCCESS.equals(institution.getAuditStatus())) {
            log.info("**************** 校验撤销订单参数 商户审核状态的检验 **************** institution : {}", institution, JSON.toJSON(institution.getInstitutionCode()));
            throw new BusinessException(EResultEnum.INSTITUTION_STATUS_ABNORMAL.getCode());
        }
        //机构状态check-启用禁用
        if (!institution.getEnabled()) {
            log.info("**************** 校验撤销订单参数 商户启用禁用的检验 **************** institution : {}", institution, JSON.toJSON(institution.getInstitutionCode()));
            throw new BusinessException(EResultEnum.INSTITUTION_STATUS_ABNORMAL.getCode());
        }
        //查询机构绑定设备
        DeviceBinding deviceBinding = deviceBindingMapper.selectByInstitutionCodeAndImei(institution.getInstitutionCode(), undoDTO.getTerminalId());
        if (deviceBinding == null) {
            log.info("**************校验撤销订单参数 设备编号不合法***************** institution:{}", institution.getInstitutionCode());
            //设备编号不合法
            throw new BusinessException(EResultEnum.DEVICE_CODE_INVALID.getCode());
        }
        //查询设备操作员
        SysUser sysUser = sysUserMapper.selectByInstitutionCodeAndUserName(institution.getInstitutionCode().concat(undoDTO.getOperatorId()));
        if (sysUser == null) {
            log.info("**************校验撤销订单参数 设备操作员不合法***************** institution:{}", institution.getInstitutionCode());
            //设备操作员不合法
            throw new BusinessException(EResultEnum.DEVICE_OPERATOR_INVALID.getCode());
        }
    }

    /**
     * 撤销操作
     *
     * @param order
     * @param
     */
    @Override
    public void cancelOrder(Orders order) {
        //创建退款订单
        OrderRefund orderRefund = this.creatOrderRefund(order);
        //判断通道是否支持系统退款
        if (this.commonService.getChannelByChannelCode(order.getChannelCode()).getSupportRefundState()) {
            orderRefund.setRefundMode(TradeConstant.REFUND_MODE_AUTO);//系统退款
            refundOrderMapper.insert(orderRefund);//创建退款订单表
        } else {
            orderRefund.setRefundMode(TradeConstant.REFUND_MODE_PERSON);//人工退款
            refundOrderMapper.insert(orderRefund);//创建退款订单表
            return;
        }
        //上报清结算撤销
        FundChangeDTO fundChangeDTO = new FundChangeDTO(TradeConstant.RV, orderRefund);
        fundChangeDTO.setShouldDealtime(orderRefund.getProductSettleCycle());//上报上游清算系统的应结日期
        BaseResponse cFundChange = clearingService.fundChange(fundChangeDTO, null);
        log.info("***********************线下撤销操作上报清结算的撤销接口的输入参数：{}", JSON.toJSONString(fundChangeDTO));
        //调用清结算接口-资金变动接口失败的场合
        if (cFundChange.getCode().equals(String.valueOf(AsianWalletConstant.HTTP_SUCCESS_STATUS))) {//请求成功
            FundChangeVO fundChangeVO = (FundChangeVO) cFundChange.getData();
            if (!fundChangeVO.getRespCode().equals(TradeConstant.CLEARING_SUCCESS)) {//业务失败
                log.info("****************线下撤销操作时撤销调用清结算资金变动失败-业务失败****************** cFundChange : {}", JSON.toJSON(cFundChange));
                RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orderRefund));
                rabbitMQSender.send(AD3MQConstant.MQ_CANCEL_ORDER_FUND_CHANGE_RV_FAIL, JSON.toJSONString(rabbitMassage));//撤销时调用清结算资金变动RV时发生失败时的队列
                return;
            }
        } else {//请求失败
            log.info("****************线下撤销操作时撤销调用清结算资金变动失败-请求失败****************** cFundChange : {}", JSON.toJSON(cFundChange));
            RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orderRefund));
            rabbitMQSender.send(AD3MQConstant.MQ_CANCEL_ORDER_FUND_CHANGE_RV_FAIL, JSON.toJSONString(rabbitMassage));//撤销时调用清结算资金变动RV时发生失败时的队列
            return;
        }
        //获取原订单的refCode字段(NextPos用)
        orderRefund.setSign(order.getSign());
        //根据通道的通道的服务名称调用对应的通道退款接口
        Channel channel = this.commonService.getChannelByChannelCode(order.getChannelCode());
        RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orderRefund));
        if (channel.getChannelEnName().equalsIgnoreCase(AD3Constant.AD3_OFFLINE)) {
            ad3Service.repeal(orderRefund, rabbitMassage);//调用AD3通道的退款接口
        } else if (channel.getChannelEnName().equalsIgnoreCase(TradeConstant.ALIPAY_CSB_ONLINE) ||
                channel.getChannelEnName().equalsIgnoreCase(TradeConstant.ALIPAY_BSC_OFFLINE) ||
                channel.getChannelEnName().equalsIgnoreCase(TradeConstant.ALIPAY_CSB_OFFLINE)) {
            aliPayService.aliPayCancel(orderRefund, new BaseResponse(), rabbitMassage);
        } else if (channel.getChannelEnName().equalsIgnoreCase(TradeConstant.WECHAT_CSB_ONLINE) ||
                channel.getChannelEnName().equalsIgnoreCase(TradeConstant.WECHAT_CSB_OFFLINE) ||
                channel.getChannelEnName().equalsIgnoreCase(TradeConstant.WECHAT_BSC_OFFLINE)) {
            wechatService.wechatCancel(orderRefund, new BaseResponse(), rabbitMassage);
        } else if (channel.getChannelEnName().equalsIgnoreCase(TradeConstant.NEXTPOS_ONLINE) ||
                channel.getChannelEnName().equalsIgnoreCase(TradeConstant.NEXTPOS_CSB_OFFLINE)) {
            //nextPos通道撤销
            megaPayService.megaPayNextPosCancel(orderRefund, new BaseResponse(), rabbitMassage);
        }
    }

    /**
     * 线下撤销订单退款操作
     *
     * @param order
     * @return
     */
    @Override
    public void refund(Orders order) {
        //创建退款订单
        OrderRefund orderRefund = this.creatOrderRefund(order);
        //判断通道是否支持系统退款
        if (this.commonService.getChannelByChannelCode(order.getChannelCode()).getSupportRefundState()) {
            orderRefund.setRefundMode(TradeConstant.REFUND_MODE_AUTO);//系统退款
            refundOrderMapper.insert(orderRefund);//创建退款订单表
        } else {
            //不支持退款
            throw new BusinessException(EResultEnum.NOT_SUPPORT_REFUND.getCode());
        }
        //调用清结算的资金变动接口
        FundChangeDTO fundChangeDTO = new FundChangeDTO(TradeConstant.RF, orderRefund);
        fundChangeDTO.setShouldDealtime(orderRefund.getProductSettleCycle());//应结日期
        BaseResponse cFundChange = clearingService.fundChange(fundChangeDTO, null);
        log.info("*******************线下撤销订单退款操作时调用清结算资金变动接口的输入参数：{}", JSON.toJSON(fundChangeDTO));
        if (cFundChange.getCode().equals(String.valueOf(AsianWalletConstant.HTTP_SUCCESS_STATUS))) {//请求成功
            FundChangeVO fundChangeVO = (FundChangeVO) cFundChange.getData();
            if (!fundChangeVO.getRespCode().equals(TradeConstant.CLEARING_SUCCESS)) {//业务失败
                log.info("**************** 撤销订单时退款调用清结算资金冻结解冻接口失败-业务失败*************** BaseResponse :{}", JSON.toJSON(cFundChange));
                RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orderRefund));
                rabbitMQSender.send(AD3MQConstant.MQ_CANCEL_ORDER_FUND_CHANGE_RF_FAIL, JSON.toJSONString(rabbitMassage));//撤销时退款调用清结算资金变动RF发生失败时队列
                return;
            }
        } else {//请求失败
            log.info("**************** 撤销订单时退款调用清结算资金冻结解冻接口失败-请求失败*************** BaseResponse :{}", JSON.toJSON(cFundChange));
            RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orderRefund));
            rabbitMQSender.send(AD3MQConstant.MQ_CANCEL_ORDER_FUND_CHANGE_RF_FAIL, JSON.toJSONString(rabbitMassage));//撤销时退款调用清结算资金变动RF发生失败时队列
            return;
        }
        //获取原订单的refCode字段(NextPos用)
        orderRefund.setSign(order.getSign());
        //调用上游通道的退款接口
        Channel channel = this.commonService.getChannelByChannelCode(order.getChannelCode());
        if (channel.getChannelEnName().equalsIgnoreCase(AD3Constant.AD3_OFFLINE)) {
            ad3Service.cancelRefund(orderRefund, fundChangeDTO);
        } else if (channel.getChannelEnName().equalsIgnoreCase(TradeConstant.ALIPAY_CSB_ONLINE) ||
                channel.getChannelEnName().equalsIgnoreCase(TradeConstant.ALIPAY_BSC_OFFLINE) ||
                channel.getChannelEnName().equalsIgnoreCase(TradeConstant.ALIPAY_CSB_OFFLINE)) {
            aliPayService.aliPayRefund(orderRefund, fundChangeDTO, new BaseResponse());
        } else if (channel.getChannelEnName().equalsIgnoreCase(TradeConstant.WECHAT_CSB_ONLINE) ||
                channel.getChannelEnName().equalsIgnoreCase(TradeConstant.WECHAT_CSB_OFFLINE) ||
                channel.getChannelEnName().equalsIgnoreCase(TradeConstant.WECHAT_BSC_OFFLINE)) {
            wechatService.wechatRefund(orderRefund, fundChangeDTO, new BaseResponse());
        } else if (channel.getChannelEnName().equalsIgnoreCase(TradeConstant.NEXTPOS_ONLINE) ||
                channel.getChannelEnName().equalsIgnoreCase(TradeConstant.NEXTPOS_CSB_OFFLINE)) {
            //nextPos通道退款
            megaPayService.megaPayNextPosRefund(orderRefund, fundChangeDTO, new BaseResponse());
        }
    }

    /**
     * 创建退款订单
     *
     * @param order
     * @return
     */
    private OrderRefund creatOrderRefund(Orders order) {
        //创建退款订单
        OrderRefund orderRefund = new OrderRefund();
        BeanUtils.copyProperties(order, orderRefund);
        //计算手续费
        InstitutionProduct institutionProduct = institutionProductMapper.selectByProductCodeAndInstitutionCode(order.getProductCode(), order.getInstitutionCode());
        //如果订单币种与产品币种不一致时,需要换汇操作
        if (!orderRefund.getOrderCurrency().equals(orderRefund.getTradeCurrency())) {
            institutionProduct = commonService.CalcFeeExchange(String.valueOf(orderRefund.getProductCode()), institutionProduct, orderRefund.getExchangeRate(), orderRefund.getOrderCurrency());
        }
        BigDecimal poundage = BigDecimal.ZERO;
        if (institutionProduct.getRefundRate() == null || institutionProduct.getRefundRateType() == null) {
            log.info("-----------------撤销退款 计费失败信息记录 ----------------费率:{},费率类型:{}", institutionProduct.getRefundRate(), institutionProduct.getRefundRateType());
            throw new BusinessException(EResultEnum.SYS_ERROR_CREATE_ORDER_FAIL.getCode());
        }
        //单笔费率
        if (institutionProduct.getRefundRateType().equals(TradeConstant.FEE_TYPE_RATE)) {
            //手续费=订单金额*单笔费率
            poundage = order.getAmount().multiply(institutionProduct.getRefundRate());
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
        Account account = accountMapper.getAccount(order.getInstitutionCode(), order.getOrderCurrency());
        BigDecimal add = order.getAmount().add(poundage);
        //小于
        if (account.getSettleBalance().compareTo(add) == -1) {
            log.info("-------------撤销退款失败 结算金额小于退款金额+手续费-------------机构号:{},结算户余额:{},币种:{},手续费+退款金额:{}",
                    order.getInstitutionCode(), account.getSettleBalance(), order.getOrderCurrency(), add.toString());
            throw new BusinessException(EResultEnum.INSUFFICIENT_ACCOUNT_BALANCE.getCode());
        }
        //设置退款手续费
        orderRefund.setRefundFee(poundage);
        orderRefund.setId("R" + IDS.uniqueID().toString());//退款订单号
        orderRefund.setOrderId(order.getId());//原订单流水号
        orderRefund.setRefundType(TradeConstant.REFUND_TYPE_TOTAL);//退款类型-全额退款
        orderRefund.setInstitutionOrderTime(new Date());//机构请求退款时间
        orderRefund.setRefundStatus(TradeConstant.REFUND_WAIT);//退款中
        orderRefund.setDeviceCode(order.getDeviceCode());//设备编号
        orderRefund.setDeviceOperator(order.getDeviceOperator());//设备操作员
        orderRefund.setCreateTime(new Date());//创建时间
        orderRefund.setCreator(order.getDeviceOperator());//撤销订单的操作人
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
        orderRefund.setChannelFee(null);//通道手续费
        orderRefund.setChannelFeeType(null);//通道手续费类型
        orderRefund.setChannelGatewayCharge(null);//通道网关是否收取
        orderRefund.setChannelGatewayRate(null);//通道网关费率
        orderRefund.setChannelGatewayFee(null);//通道网关手续费
        orderRefund.setChannelGatewayFeeType(null);//通道网关手续费类型
        orderRefund.setChannelGatewayStatus(null);//通道网关收取状态
        orderRefund.setUpdateTime(null);//更新时间
        orderRefund.setModifier(null);//更新人
        return orderRefund;
    }
}
