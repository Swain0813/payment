package com.payment.clearing.service.impl;
import com.payment.clearing.constant.Const;
import com.payment.clearing.dao.AccountMapper;
import com.payment.clearing.dao.ShareBenefitLogsMapper;
import com.payment.clearing.dao.TcsStFlowMapper;
import com.payment.clearing.dao.TmMerChTvAcctBalanceMapper;
import com.payment.clearing.service.CalculateShareBenefitService;
import com.payment.clearing.utils.ComDoubleUtil;
import com.payment.common.constant.TradeConstant;
import com.payment.common.entity.Account;
import com.payment.common.entity.ShareBenefitLogs;
import com.payment.common.entity.TcsStFlow;
import com.payment.common.entity.TmMerChTvAcctBalance;
import com.payment.common.redis.RedisService;
import com.payment.common.utils.IDS;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.StringUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @description: 计算分润服务
 * @author: YangXu
 * @create: 2019-08-23 16:17
 **/
@Slf4j
@Service
public class CalculateShareBenefitServiceImpl implements CalculateShareBenefitService {

    @Autowired
    private RedisService redisService;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private ShareBenefitLogsMapper shareBenefitLogsMapper;

    @Autowired
    private TmMerChTvAcctBalanceMapper tmMerChTvAcctBalanceMapper;

    @Autowired
    private TcsStFlowMapper tcsStFlowMapper;

    /**
     * @return
     * @Author YangXu
     * @Date 2019/8/23
     * @Descripate 根据流程梳理要求优化的以组为单位分润并提交事物
     **/
    @Override
    @Transactional
    public void calculateShareForMerchantGroup2(String agentCode, String currency, List<ShareBenefitLogs> list) {
        String key = Const.Redis.CLEARING_KEY + "_" + agentCode + "_" + currency;
        log.info("************ CLEARING_KEY *************** key:{}", key);
        if (redisService.lock(key, Const.Redis.expireTime)) {
            log.info("***************** get lock success key :【{}】 ************** ", key);
            try {
                log.info("**************** calculateShareForMerchantGroup2 单组分润 **************# 开始执行代理商户号为:{}，币种为:{}的待分润数据,时间:{}", agentCode, currency, new Date());
                if (StringUtils.isEmpty(agentCode) || list == null || list.size() == 0 || StringUtils.isEmpty(currency)) {
                    log.info("**************** calculateShareForMerchantGroup2 单组分润 **************# 执行代理商户号为:{}，币种为:{}的数据过程输入参数为空，时间：", agentCode, currency, new Date());
                    return;
                }
                Account account = accountMapper.selectByInstitutionIdAndCode(agentCode, currency);
                //代理商分润为负数的时候出现挂账现象等待下一笔分润累计，所以代理商账户的结算户可能为负，不用判断
                if (account == null) {
                    log.info("**************** calculateShareForMerchantGroup2 单组分润 **************代理商户:{},币种：{} 不存在，不结算", agentCode, currency);
                    return;
                }
                double beforeBalance = account.getSettleBalance().doubleValue();//期初结算余额
                log.info("**************** calculateShareForMerchantGroup2 单组分润 **************# 代理商户为:{}，币种为:{}的期初结算余额为:{}", agentCode, currency, beforeBalance);
                double afterBalance = 0.0D; //期末余额
                List<TmMerChTvAcctBalance> list1 = new ArrayList<>();//账户变动流水
                List<TcsStFlow> list3 = new ArrayList<>();//结算账户流水
                int num = 0;
                for (ShareBenefitLogs sl : list) {
                    if (sl == null || StringUtils.isEmpty(sl.getAgentCode()) || StringUtils.isEmpty(sl.getTradeCurrency())
                            || StringUtils.isEmpty(sl.getShareBenefit())) {
                        log.info("*************** calculateSharebenefit 单调分润 ************** 接收处理数据，分润流水信息不全 流水号 ：{}", sl.getId());
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                        return;
                    }
                    if (shareBenefitLogsMapper.selectCountbyIdAndIsShare(sl.getId(),TradeConstant.SHARE_BENEFIT_WAIT) == 0) {
                        log.info("*************** calculateSharebenefit 单调分润 ************** 编号为：{} 流水确认不通过", sl.getId());
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                        return;
                    }
                    log.info("*************** calculateSharebenefit 单调分润 **************,准备执行编号为：{} 分润 ：{} 的待流水记录，期初余额：{}", sl.getId(), sl.getShareBenefit(), beforeBalance);
                    afterBalance = ComDoubleUtil.addBySize(beforeBalance,sl.getShareBenefit(),2);
                    log.info("*************** calculateSharebenefit 单调分润 **************,准备执行编号为：{} 分润 ：{} 的待流水记录，期末余额：{}", sl.getId(), sl.getShareBenefit(), afterBalance);

                    //插入结算户流水 --- 已结算记录
                    TcsStFlow tcsStFlow = new TcsStFlow();
                    tcsStFlow.setSTFlow("SF"+IDS.uniqueID());
                    tcsStFlow.setRefcnceFlow(sl.getOrderId());
                    tcsStFlow.setTradetype("SP");
                    tcsStFlow.setMerchantid(sl.getAgentCode());
                    tcsStFlow.setMerOrderNo(sl.getId());
                    tcsStFlow.setTxncurrency(sl.getTradeCurrency());
                    tcsStFlow.setTxnamount(sl.getShareBenefit());
                    tcsStFlow.setFee(0.0D);
                    tcsStFlow.setFeecurrency(sl.getTradeCurrency());
                    tcsStFlow.setChannelCost(0.0D);
                    tcsStFlow.setChannelcostcurrency(sl.getTradeCurrency());
                    tcsStFlow.setRevokemount(0.0D);
                    tcsStFlow.setBusinessType(Integer.parseInt(sl.getExtend1()));//1 收单 2 付款;
                    tcsStFlow.setBalancetype(1);
                    tcsStFlow.setAccountNo(account.getId());
                    tcsStFlow.setSTstate(2);
                    tcsStFlow.setShouldSTtime(new Date());
                    tcsStFlow.setActualSTtime(new Date());
                    tcsStFlow.setSysorderid(sl.getOrderId());
                    tcsStFlow.setAddDatetime(new Date());
                    tcsStFlow.setSltamount(sl.getShareBenefit());
                    tcsStFlow.setSltcurrency(sl.getTradeCurrency());
                    tcsStFlow.setTxnexrate(0.0D);
                    tcsStFlow.setGatewayFee(0.0D);
                    tcsStFlow.setNeedClear(1);
                    if (sl.getTradeAmount() < 0.0D) {
                        tcsStFlow.setRemark("挂账");
                    }
                    list3.add(tcsStFlow);

                    //插入商户账户流水表（结算户）--分润流水
                    TmMerChTvAcctBalance tma = new TmMerChTvAcctBalance();
                    tma.setFlow("MV" + IDS.uniqueID());
                    tma.setMerchantid(sl.getAgentCode());
                    tma.setVaccounId(account.getId());
                    tma.setType(2);//结算户
                    tma.setBussinesstype(Integer.parseInt(sl.getExtend1()));//1 收单 2 付款
                    tma.setBalancetype(1);
                    tma.setCurrency(sl.getTradeCurrency());
                    tma.setReferenceflow(sl.getOrderId());
                    tma.setTradetype("SP");
                    tma.setTxnamount(sl.getTradeAmount());
                    tma.setSltamount(sl.getTradeAmount());
                    tma.setSltcurrency(sl.getTradeCurrency());
                    tma.setIncome(sl.getShareBenefit());
                    tma.setOutcome(0.0D);
                    tma.setFee(sl.getFee());
                    tma.setBalance(beforeBalance);
                    tma.setAfterbalance(afterBalance);
                    tma.setSysAddDate(new Date());
                    tma.setBalanceTimestamp(new Date());
                    tma.setSltexrate(0.0D);
                    tma.setGatewayFee(0.0D);
                    if (sl.getTradeAmount() < 0.0D) {
                        tma.setRemark("挂账");
                    }
                    list1.add(tma);
                    beforeBalance = afterBalance;
                    //更新分润记录表变成已分润
                    num += shareBenefitLogsMapper.updateByIsShare(sl.getId(),TradeConstant.SHARE_BENEFIT_SUCCESS);
                }
                log.info("**************** calculateShareForMerchantGroup2 单组分润 **************# 代理商户为:{}，币种为:{}的期末结算余额为:{}", agentCode, currency, afterBalance);
                int result3 = tcsStFlowMapper.insertList(list3);
                int result = tmMerChTvAcctBalanceMapper.insertList(list1);
                account.setSettleBalance(new BigDecimal(afterBalance));
                int result1 = accountMapper.updateSPAMTByPrimaryKey(account);
                if (result != list1.size() || num != list.size() || result1 == 0 || result3 != list3.size()) {
                    //回滚
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return;
                }
            } catch (Exception e) {
                log.error("**************** calculateShareForMerchantGroup2 单组分润 ************** 代理商户号：{} ， 币种 ：{} ，异常：{}", agentCode, currency, e);
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            } finally {
                while (!redisService.releaseLock(key)) {
                    log.info("******************* release lock failed ******************** ：{} ", key);
                }
                log.info("********************* release lock success ******************** : {}", key);
            }
        } else {
            log.info("********************* get lock failed ******************** : {} : " + key);
        }

    }
}
