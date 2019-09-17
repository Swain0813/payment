package com.payment.finance.service.impl;

import com.alibaba.fastjson.JSON;
import com.payment.common.constant.AD3MQConstant;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.constant.TradeConstant;
import com.payment.common.dto.*;
import com.payment.common.entity.Account;
import com.payment.common.entity.Institution;
import com.payment.common.entity.RabbitMassage;
import com.payment.common.entity.Reconciliation;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.common.utils.DateToolUtils;
import com.payment.common.utils.IDS;
import com.payment.common.vo.FinancialFreezeVO;
import com.payment.common.vo.FundChangeVO;
import com.payment.finance.dao.AccountMapper;
import com.payment.finance.dao.ReconciliationMapper;
import com.payment.finance.rabbitmq.RabbitMQSender;
import com.payment.finance.service.ClearingService;
import com.payment.finance.service.FinanceCommonService;
import com.payment.finance.service.ReconciliationService;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


/**
 * @description: 调账服务
 * @author: YangXu
 * @create: 2019-03-25 15:53
 **/
@Transactional
@Service
@Slf4j
public class ReconciliationServiceImpl implements ReconciliationService {

    @Autowired
    ReconciliationMapper reconciliationMapper;

    @Autowired
    private ClearingService clearingService;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private RabbitMQSender rabbitMQSender;

    @Autowired
    private FinanceCommonService financeCommonService;

    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/25
     * @Descripate 分页查询调账单
     **/
    @Override
    public PageInfo<Reconciliation> pageReconciliation(ReconciliationDTO reconciliationDTO) {
        List<Reconciliation> reconciliations = reconciliationMapper.pageReconciliation(reconciliationDTO);
        reconciliations.forEach(r -> {
            if (r.getStatus() == TradeConstant.UNFREEZE_SUCCESS || r.getStatus() == TradeConstant.UNFREEZE_WAIT || r.getStatus() == TradeConstant.UNFREEZE_FALID) {
                r.setTradeAmount(r.getTradeAmount().negate());
                r.setAmount(r.getAmount().negate());
            }
        });
        return new PageInfo<Reconciliation>(reconciliations);
    }

    /**
     * 查看审核
     *
     * @param reconciliationDTO
     * @return
     */
    @Override
    public PageInfo<Reconciliation> pageReviewReconciliation(ReconciliationDTO reconciliationDTO) {
        List<Reconciliation> reconciliations = reconciliationMapper.pageReviewReconciliation(reconciliationDTO);
        reconciliations.forEach(r -> {
            if (r.getStatus() == TradeConstant.UNFREEZE_SUCCESS || r.getStatus() == TradeConstant.UNFREEZE_WAIT || r.getStatus() == TradeConstant.UNFREEZE_FALID) {
                r.setTradeAmount(r.getTradeAmount().negate());
                r.setAmount(r.getAmount().negate());
            }
        });
        return new PageInfo<Reconciliation>(reconciliations);
    }

    /**
     * 导出机构资金变动一览数据
     *
     * @param reconciliationExportDTO
     * @return
     */
    @Override
    public List<Reconciliation> exportReconciliation(ReconciliationExportDTO reconciliationExportDTO) {
        List<Reconciliation> reconciliations = reconciliationMapper.pageExportReconciliation(reconciliationExportDTO);
        reconciliations.forEach(r -> {
            if (r.getStatus() == TradeConstant.UNFREEZE_SUCCESS || r.getStatus() == TradeConstant.UNFREEZE_WAIT || r.getStatus() == TradeConstant.UNFREEZE_FALID) {
                r.setTradeAmount(r.getTradeAmount().negate());
                r.setAmount(r.getAmount().negate());
            }
        });
        return reconciliations;
    }

    /**
     * 查询可用余额
     * 新增用
     *
     * @param searchAvaBalDTO
     * @return
     */
    @Override
    public String getAvailableBalance(SearchAvaBalDTO searchAvaBalDTO) {
        BigDecimal avaBal = null;
        if (searchAvaBalDTO.getType().equals(TradeConstant.FROZEN_FUND)) { //冻结金查询
            if (!StringUtils.isEmpty(searchAvaBalDTO.getAccountType())) {
                avaBal = reconciliationMapper.selectFreezeBalance(searchAvaBalDTO).abs();
            }
        } else {
            avaBal = reconciliationMapper.selectAvailableBalance(searchAvaBalDTO);//可用余额查询
        }
        if (StringUtils.isEmpty(avaBal)) {
            throw new BusinessException(EResultEnum.ACCOUNT_IS_NOT_EXIST.getCode());//账户异常
        }
        return avaBal.toString();
    }


    /**
     * 机构资金变动审核
     *
     * @param name
     * @param reconciliationId
     * @param enabled
     * @param remark
     * @return
     */
    @Override
    public String auditReconciliation(String name, String reconciliationId, boolean enabled, String remark) {
        Reconciliation reconciliation = reconciliationMapper.selectByPrimaryKey(reconciliationId);
        if (reconciliation == null) {//调账记录不存在
            throw new BusinessException(EResultEnum.TIAOZHANG_RECORD_IS_NOT_EXIST.getCode());
        }

        if (TradeConstant.RECONCILIATION_WAIT != reconciliation.getStatus() && TradeConstant.UNFREEZE_WAIT != reconciliation.getStatus() && TradeConstant.FREEZE_WAIT != reconciliation.getStatus()) {//调账状态不合法
            throw new BusinessException(EResultEnum.FUNDS_STATUS_IS_ILLEGAL.getCode());//资金状态不合法
        }
        Institution institution = financeCommonService.getInstitutionInfo(reconciliation.getInstitutionCode());
        if (institution == null) {//机构信息不存在
            throw new BusinessException(EResultEnum.INSTITUTION_NOT_EXIST.getCode());//机构信息不存在
        }
        //机构已禁用
        if (!institution.getEnabled()) {
            throw new BusinessException(EResultEnum.INSTITUTION_IS_DISABLE.getCode());//机构已禁用
        }
        //判断币种
        Account account = accountMapper.getAccount(institution.getInstitutionCode(), reconciliation.getOrderCurrency());
        if (account == null) {//机构账户对应的币种不存在
            throw new BusinessException(EResultEnum.INSTITUTION_ACCOUNT_CURRENCY_IS_NOT_EXIST.getCode());
        }
        //机构账户对应的币种已禁用
        if (!account.getEnabled()) {
            throw new BusinessException(EResultEnum.INSTITUTION_ACCOUNT_CURRENCY_IS_DISABLE.getCode());//当前机构该币种的账户已禁用
        }
        //判断审核状态  资金变动类型 1-调账 2-资金冻结 3-资金解冻
        if (!enabled && reconciliation.getChangeType() == TradeConstant.TRANSFER) {
            //更新调账记录表
            reconciliationMapper.updateStatusById(reconciliation.getId(), TradeConstant.RECONCILIATION_FALID, name, remark);
            return "审核失败成功";
        }
        if (!enabled && reconciliation.getChangeType() == TradeConstant.FUND_FREEZING) {
            //更新调账记录表
            reconciliationMapper.updateStatusById(reconciliation.getId(), TradeConstant.FREEZE_FALID, name, remark);
            return "审核失败成功";
        }
        if (!enabled && reconciliation.getChangeType() == TradeConstant.THAWING_FUNDS) {
            //更新调账记录表
            reconciliationMapper.updateStatusById(reconciliation.getId(), TradeConstant.UNFREEZE_FALID, name, remark);
            return "审核失败成功";
        }
        //冻结
        if (reconciliation.getReconciliationType() == AsianWalletConstant.FREEZE) {
            //判断为冻结时
            if (!reconciliation.getFreezeType().equals(TradeConstant.RESERVATION_FREEZE)) {//不为预约时
                //判断余额
                if (reconciliation.getAmount().compareTo(account.getSettleBalance().subtract(account.getFreezeBalance())) == 1) {
                    throw new BusinessException(EResultEnum.BANLANCE_NOT_FOOL.getCode());
                }
            }
            FinancialFreezeDTO ffd = new FinancialFreezeDTO(reconciliation, account);
            BaseResponse response = clearingService.freezingFunds(ffd, null);
            if (response.getCode().equals(String.valueOf(AsianWalletConstant.HTTP_SUCCESS_STATUS))) {//请求成功
                FinancialFreezeVO fcv = (FinancialFreezeVO) response.getData();
                if (StringUtils.isEmpty(fcv.getRespCode()) || !fcv.getRespCode().equals(TradeConstant.CLEARING_SUCCESS)) {//业务失败
                    log.info("----------------- 冻结审核 上报队列 FREEZE_MQ_FAIL 业务失败 -------------- rabbitMassage : {} ", JSON.toJSON(fcv));
                    RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(ffd));
                    rabbitMQSender.send(AD3MQConstant.FREEZE_MQ_FAIL, JSON.toJSONString(rabbitMassage));
                    return "审核请求清结算失败";
                } else {//业务成功
                    //更新调账记录表
                    reconciliationMapper.updateStatusById(reconciliation.getId(), TradeConstant.FREEZE_SUCCESS, name, remark); //冻结成功
                }
            } else {//请求失败
                log.info("----------------- 调账审核 上报队列 FREEZE_MQ_FAIL 请求失败-------------- rabbitMassage : {} ", JSON.toJSON(response));
                RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(ffd));
                rabbitMQSender.send(AD3MQConstant.FREEZE_MQ_FAIL, JSON.toJSONString(rabbitMassage));
                return "审核请求清结算失败";
            }
            return null;
        }

        //解冻
        if (reconciliation.getReconciliationType() == AsianWalletConstant.UNFREEZE) {
            //判断冻结账户余额
            if (reconciliation.getAmount().compareTo(account.getFreezeBalance()) == 1) {
                throw new BusinessException(EResultEnum.INSUFFICIENT_FROZEN_ACCOUNT_BALANCE.getCode());
            }
            FinancialFreezeDTO ffd = new FinancialFreezeDTO(reconciliation, account);
            BaseResponse response = clearingService.freezingFunds(ffd, null);
            if (response.getCode().equals(String.valueOf(AsianWalletConstant.HTTP_SUCCESS_STATUS))) {//请求成功
                FinancialFreezeVO fcv = (FinancialFreezeVO) response.getData();
                if (StringUtils.isEmpty(fcv.getRespCode()) || !fcv.getRespCode().equals(TradeConstant.CLEARING_SUCCESS)) {//业务失败
                    log.info("----------------- 冻结审核 上报队列 UNFREEZE_MQ_FAIL 业务失败 -------------- rabbitMassage : {} ", JSON.toJSON(fcv));
                    RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(ffd));
                    rabbitMQSender.send(AD3MQConstant.UNFREEZE_MQ_FAIL, JSON.toJSONString(rabbitMassage));
                    return "审核请求清结算失败";
                } else {//业务成功
                    //更新调账记录表
                    reconciliationMapper.updateStatusById(reconciliation.getId(), TradeConstant.UNFREEZE_SUCCESS, name, remark); //冻结成功
                }
            } else {//请求失败
                log.info("----------------- 调账审核 上报队列 UNFREEZE_MQ_FAIL 请求失败-------------- rabbitMassage : {} ", JSON.toJSON(response));
                RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(ffd));
                rabbitMQSender.send(AD3MQConstant.UNFREEZE_MQ_FAIL, JSON.toJSONString(rabbitMassage));
                return "审核请求清结算失败";
            }
            return null;
        }

        //调账
        //判断余额
        if (reconciliation.getReconciliationType() == AsianWalletConstant.RECONCILIATION_OUT && reconciliation.getAmount().compareTo(account.getSettleBalance().subtract(account.getFreezeBalance())) == 1) {
            throw new BusinessException(EResultEnum.BANLANCE_NOT_FOOL.getCode());
        }
        FundChangeDTO fundChangeDTO = new FundChangeDTO(reconciliation);
        fundChangeDTO.setShouldDealtime(DateToolUtils.formatTimestamp.format(new Date()));
        BaseResponse cFundChange = clearingService.fundChange(fundChangeDTO, null);
        if (cFundChange.getCode().equals(String.valueOf(AsianWalletConstant.HTTP_SUCCESS_STATUS))) {//请求成功
            FundChangeVO fundChangeVO = (FundChangeVO) cFundChange.getData();
            if (StringUtils.isEmpty(fundChangeVO.getRespCode()) || !fundChangeVO.getRespCode().equals(TradeConstant.CLEARING_SUCCESS)) {//业务失败
                log.info("----------------- 调账审核 上报队列 TC_MQ_RECONCILIATION_DL 业务失败 -------------- rabbitMassage : {} ", JSON.toJSON(fundChangeVO));
                RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(reconciliation));
                rabbitMQSender.send(AD3MQConstant.TC_MQ_RECONCILIATION_DL, JSON.toJSONString(rabbitMassage));
                return "审核请求清结算失败";
            } else {//业务成功
                //更新调账记录表
                reconciliationMapper.updateStatusById(reconciliation.getId(), TradeConstant.RECONCILIATION_SUCCESS, name, remark);
            }
        } else {//请求失败
            log.info("----------------- 调账审核 上报队列 TC_MQ_RECONCILIATION_DL 请求失败-------------- rabbitMassage : {} ", JSON.toJSON(cFundChange));
            RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(reconciliation));
            rabbitMQSender.send(AD3MQConstant.TC_MQ_RECONCILIATION_DL, JSON.toJSONString(rabbitMassage));
            return "审核请求清结算失败";
        }
        return null;
    }


    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/25
     * @Descripate 资金变动操作
     **/
    @Override
    public String doReconciliation(String name, ReconOperDTO reconOperDTO) {
        //获取机构信息
        Institution institution = financeCommonService.getInstitutionInfo(reconOperDTO.getInstitutionCode());
        if (institution == null) {
            throw new BusinessException(EResultEnum.INSTITUTION_NOT_EXIST.getCode());//机构信息不存在
        }
        //机构已禁用
        if (!institution.getEnabled()) {
            throw new BusinessException(EResultEnum.INSTITUTION_IS_DISABLE.getCode());//机构已禁用
        }
        Account account = accountMapper.getAccount(institution.getInstitutionCode(), reconOperDTO.getCurrency());
        if (account == null) {//机构账户对应的币种不存在
            throw new BusinessException(EResultEnum.INSTITUTION_ACCOUNT_CURRENCY_IS_NOT_EXIST.getCode());
        }
        //机构账户对应的币种已禁用
        if (!account.getEnabled()) {
            throw new BusinessException(EResultEnum.INSTITUTION_ACCOUNT_CURRENCY_IS_DISABLE.getCode());//当前机构该币种的账户已禁用
        }
        //入账账户类型不能为空
        if(reconOperDTO.getAccountType()==null ||reconOperDTO.getAccountType()==0){
            throw new BusinessException(EResultEnum.ACCOUNT_TYPE_IS_NULL.getCode());//入账账户类型不能为空
        }
        if (reconOperDTO.getChangeType() == TradeConstant.TRANSFER && AsianWalletConstant.RECONCILIATION_IN == reconOperDTO.getType()) {//调入
            return doReconciliationIn(name, institution, reconOperDTO);
        } else if (reconOperDTO.getChangeType() == TradeConstant.TRANSFER && AsianWalletConstant.RECONCILIATION_OUT == reconOperDTO.getType()) {//调出
            //判断余额
            if (reconOperDTO.getAmount().compareTo(account.getSettleBalance().subtract(account.getFreezeBalance())) == 1) {
                throw new BusinessException(EResultEnum.BANLANCE_NOT_FOOL.getCode());
            }
            return doReconciliationOut(name, institution, reconOperDTO);

        } else if (reconOperDTO.getChangeType() == TradeConstant.FUND_FREEZING && AsianWalletConstant.FREEZE == reconOperDTO.getType()) {//冻结
            if (!reconOperDTO.getFreezeType().equals(TradeConstant.RESERVATION_FREEZE)) {//不为预约时
                //判断余额
                if (reconOperDTO.getAmount().compareTo(account.getSettleBalance().subtract(account.getFreezeBalance())) == 1) {
                    throw new BusinessException(EResultEnum.BANLANCE_NOT_FOOL.getCode());
                }
            }
            return doFreeze(name, institution, reconOperDTO, account.getId());
        } else if (reconOperDTO.getChangeType() == TradeConstant.THAWING_FUNDS && AsianWalletConstant.UNFREEZE == reconOperDTO.getType()) {//解冻
            //判断冻结户余额
            if (reconOperDTO.getAmount().compareTo(account.getFreezeBalance()) == 1) {
                throw new BusinessException(EResultEnum.BANLANCE_NOT_FOOL.getCode());
            }
            return doUnFreeze(name, institution, reconOperDTO, account.getId());
        }
        return null;
    }


    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/25
     * @Descripate 调入
     **/
    public String doReconciliationIn(String name, Institution institution, ReconOperDTO reconOperDTO) {
        Reconciliation reconciliation = createrOrder(AsianWalletConstant.RECONCILIATION_IN, name, institution, reconOperDTO, null);
        if (reconciliationMapper.insert(reconciliation) > 0) {
            return "success";
        } else {
            throw new BusinessException(EResultEnum.ERROR.getCode());
        }
    }

    /**
     * @return
     * @Author shenxinran
     * @Date 2019/3/25
     * @Descripate 冻结
     **/
    public String doFreeze(String name, Institution institution, ReconOperDTO reconOperDTO, String accountId) {
        Reconciliation reconciliation = createrOrder(AsianWalletConstant.FREEZE, name, institution, reconOperDTO, accountId);
        if (reconciliationMapper.insert(reconciliation) > 0) {
            return "success";
        } else {
            throw new BusinessException(EResultEnum.ERROR.getCode());
        }
    }

    /**
     * @return
     * @Author shenxinran
     * @Date 2019/3/25
     * @Descripate 解冻
     **/
    public String doUnFreeze(String name, Institution institution, ReconOperDTO reconOperDTO, String accountId) {
        Reconciliation reconciliation = createrOrder(AsianWalletConstant.UNFREEZE, name, institution, reconOperDTO, accountId);
        if (reconciliationMapper.insert(reconciliation) > 0) {
            return "success";
        } else {
            throw new BusinessException(EResultEnum.ERROR.getCode());
        }
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/25
     * @Descripate 调出
     **/
    public String doReconciliationOut(String name, Institution institution, ReconOperDTO reconOperDTO) {
        Reconciliation reconciliation = createrOrder(AsianWalletConstant.RECONCILIATION_OUT, name, institution, reconOperDTO, null);
        if (reconciliationMapper.insert(reconciliation) > 0) {
            return "success";
        } else {
            throw new BusinessException(EResultEnum.ERROR.getCode());
        }
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/25
     * @Descripate 创建订单与调账记录
     **/
    public Reconciliation createrOrder(int type, String name, Institution institution, ReconOperDTO reconOperDTO, String accountId) {
        //添加调账单
        Reconciliation reconciliation = new Reconciliation();
        if (StringUtils.isEmpty(reconOperDTO.getId())) {
            reconciliation.setId("T" + IDS.uniqueID());
        } else {
            reconciliation.setId(reconOperDTO.getId());
        }
        reconciliation.setCreator(name);
        reconciliation.setInstitutionOrderId(reconOperDTO.getChangeType() != TradeConstant.TRANSFER ? accountId : null);//当变动类型为冻结或者解冻时，将accountId放入到InstitutionOrderId
        reconciliation.setCreateTime(new Date());
        reconciliation.setAmount(reconOperDTO.getChangeType() == TradeConstant.THAWING_FUNDS ? reconOperDTO.getAmount().negate() : reconOperDTO.getAmount());
        reconciliation.setChangeType(reconOperDTO.getChangeType());//资金变动类型 1-调账 2-资金冻结 3-资金解冻
        reconciliation.setFreezeType(reconOperDTO.getFreezeType());//冻结类型 1-冻结 2-预约冻结
        reconciliation.setAccountType(reconOperDTO.getAccountType());//入账类型 1-冻结户 2-保证金户
        reconciliation.setTradeAmount(reconOperDTO.getAmount());
        reconciliation.setTradeAmount(reconOperDTO.getChangeType() == TradeConstant.THAWING_FUNDS ? reconOperDTO.getAmount().negate() : reconOperDTO.getAmount());
        reconciliation.setOrderCurrency(reconOperDTO.getCurrency());
        reconciliation.setTradeCurrency(reconOperDTO.getCurrency());
        reconciliation.setInstitutionCode(institution.getInstitutionCode());
        reconciliation.setInstitutionName(institution.getCnName());
        reconciliation.setReconciliationType(type);
        if (reconOperDTO.getChangeType().equals(TradeConstant.TRANSFER)) {
            reconciliation.setStatus(TradeConstant.RECONCILIATION_WAIT);//待调账
        } else if (reconOperDTO.getChangeType().equals(TradeConstant.FUND_FREEZING)) {
            reconciliation.setStatus(TradeConstant.FREEZE_WAIT);//待冻结
        } else if (reconOperDTO.getChangeType().equals(TradeConstant.THAWING_FUNDS)) {
            reconciliation.setStatus(TradeConstant.UNFREEZE_WAIT);//待解冻
        }
        reconciliation.setRemark(reconOperDTO.getRemark());
        return reconciliation;
    }
}
