package com.payment.clearing.service.impl;
import com.payment.clearing.constant.Const;
import com.payment.clearing.dao.*;
import com.payment.clearing.service.CommonService;
import com.payment.clearing.service.SettleOrdersService;
import com.payment.clearing.utils.ComDoubleUtil;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.constant.TradeConstant;
import com.payment.common.entity.*;
import com.payment.common.redis.RedisService;
import com.payment.common.utils.DateToolUtils;
import com.payment.common.utils.IDS;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@Slf4j
@Service
public class SettleOrdersServiceImpl implements SettleOrdersService {

    @Autowired
    private RedisService redisService;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private BankCardMapper bankCardMapper;

    @Autowired
    private SettleOrderMapper settleOrderMapper;

    @Autowired
    private CommonService commonService;

    @Autowired
    private TmMerChTvAcctBalanceMapper tmMerChTvAcctBalanceMapper;

    @Autowired
    private TcsStFlowMapper tcsStFlowMapper;

    /**
     * 自动提款功能
     */
    @Override
    @Transactional
    public void getSettleOrders(String institutionCode, String currency, List<Account> lists){
        String key = Const.Redis.CLEARING_KEY + "_" + institutionCode + "_" + currency;
        log.info("************自动提款功能 CLEARING_KEY *************** key:{}", key);
        if (redisService.lock(key, Const.Redis.expireTime)) {
            log.info("*****************自动提款功能 get lock success key :【{}】 ************** ", key);
            try {
                log.info("**************** getSettleOrders 单组自动提款 **************# 开始执行机构编号为:{}，币种为:{}的待提款的数据,时间:{}", institutionCode, currency, new Date());
                if (org.springframework.util.StringUtils.isEmpty(institutionCode) || lists == null || lists.size() == 0 || StringUtils.isEmpty(currency)) {
                    log.info("**************** getSettleOrders 单组自动 **************# 执行机构编号为:{}，币种为:{}的数据过程输入参数为空，时间：", institutionCode, currency, new Date());
                    return;
                }
                //根据机构编号和币种查询账户信息
                Account account = accountMapper.selectByInstitutionIdAndCode(institutionCode,currency);
                if(account==null){
                    log.info("*************自动提款功能 getSettleOrders*************机构编号:{},币种：{} 不存在，不能进行自动提款结算",institutionCode,currency);
                }
                double beforeBalance = account.getSettleBalance().doubleValue();//期初结算余额
                log.info("**************** 自动提款功能 getSettleOrders **************# 机构编号:{}，币种为:{}的期初结算余额为:{}", institutionCode,currency,beforeBalance);
                double afterBalance = 0.0D; //期末余额
                List<TmMerChTvAcctBalance> tmMerChTvAcctBalanceLists = new ArrayList<>();//账户变动流水
                List<SettleOrder> settleOrderLists = new ArrayList<>();//账户变动流水
                List<TcsStFlow> tcsStFlowLists = new ArrayList<>();//结算账户流水
                for(Account list:lists){
                    if (list == null || StringUtils.isEmpty(list.getInstitutionId()) || StringUtils.isEmpty(list.getCurrency())
                            || StringUtils.isEmpty(list.getFreezeBalance()) || StringUtils.isEmpty(list.getFreezeBalance())
                            || list.getSettleBalance().subtract(list.getFreezeBalance()).compareTo(BigDecimal.ZERO)==-1) {
                        log.info("*************** 自动提款功能 getSettleOrders ************** 接收处理数据，结算金额-冻结金额小于0或者必要的参数为空*********机构编号为:{},币种为:{}",institutionCode,currency);
                        return;
                    }
                    //机构结算金额
                    BigDecimal outMoney = list.getSettleBalance().subtract(list.getFreezeBalance());//自动提现金额即结算金额-冻结金额
                    //获取银行卡信息
                    BankCard bankCard = bankCardMapper.getBankCard(list.getInstitutionId(),list.getAccountCode(),list.getCurrency());
                    if(bankCard!=null && bankCard.getBankAccountCode()!=null && bankCard.getBankCodeCurrency()!=null){//银行卡卡号和银行卡币种是必填的
                        //根据机构code和银行卡code获取机构交易表中当日第一条数据的批次号
                        String batchNo = settleOrderMapper.getBatchNo(list.getInstitutionId(),bankCard.getBankCode());
                        //机构结算表的数据的设置
                        SettleOrder settleOrder = new SettleOrder();
                        settleOrder.setId("J"+ IDS.uniqueID());//结算交易的流水号
                        if(StringUtils.isEmpty(batchNo)){
                            //根据年月日时分秒毫秒生成批次号
                            settleOrder.setBatchNo("P".concat(DateToolUtils.currentTime()));//批次号
                        }else {//非空的场合
                            settleOrder.setBatchNo(batchNo);//批次号
                        }
                        settleOrder.setInstitutionCode(list.getInstitutionId());//机构编号
                        //根据机构编号获取机构信息
                        Institution institutionInfo = null;
                        try{
                            institutionInfo = this.commonService.getInstitutionInfo(list.getInstitutionId());
                        }catch (Exception e){
                            //机构信息不存在
                            log.info("*******************自动提款功能 getSettleOrders 机构信息不存在或者是机构已经被禁用******************* institutionCode :{}", list.getInstitutionId());
                        }
                        //机构信息存在的场合
                        if(institutionInfo!=null){
                            //机构地址
                            settleOrder.setInstitutionAdress(institutionInfo.getInstitutionAdress());
                            //机构邮编
                            settleOrder.setInstitutionPostalCode(institutionInfo.getInstitutionPostalCode());
                        }
                        settleOrder.setInstitutionName(list.getInstitutionName());//机构名称
                        settleOrder.setTxncurrency(list.getCurrency());//交易币种
                        settleOrder.setTxnamount(outMoney);//结算金额即结算金额-冻结金额
                        settleOrder.setAccountCode(bankCard.getBankAccountCode());//结算账户即银行卡账号
                        settleOrder.setAccountName(bankCard.getAccountName());//账户名即开户名称
                        settleOrder.setBankName(bankCard.getBankName());//银行名称即开户行名称
                        settleOrder.setSwiftCode(bankCard.getSwiftCode());//Swift Code
                        settleOrder.setIban(bankCard.getIban());//Iban
                        settleOrder.setBankCode(bankCard.getBankCode());//bank code
                        settleOrder.setBankCurrency(bankCard.getBankCurrency());//结算币种
                        settleOrder.setBankCodeCurrency(bankCard.getBankCodeCurrency());//银行卡币种
                        //中间行相关字段
                        settleOrder.setIntermediaryBankCode(bankCard.getIntermediaryBankCode());//中间行银行编码
                        settleOrder.setIntermediaryBankName(bankCard.getIntermediaryBankName());//中间行银行名称
                        settleOrder.setIntermediaryBankAddress(bankCard.getIntermediaryBankAddress());//中间行银行地址
                        settleOrder.setIntermediaryBankAccountNo(bankCard.getIntermediaryBankAccountNo());//中间行银行账户
                        settleOrder.setIntermediaryBankCountry(bankCard.getIntermediaryBankCountry());//中间行银行城市
                        settleOrder.setIntermediaryOtherCode(bankCard.getIntermediaryOtherCode()); //中间行银行其他code
                        //结算中
                        settleOrder.setTradeStatus(AsianWalletConstant.SETTLING);
                        //自动结算
                        settleOrder.setSettleType(AsianWalletConstant.SETTLE_AUTO);
                        //创建时间
                        settleOrder.setCreateTime(new Date());
                        //创建人
                        settleOrder.setCreator("定时跑批生成结算交易任务");
                        settleOrderLists.add(settleOrder);
                        log.info("*************** getSettleOrders 自动提款前 **************,准备执行机构编号为：{} 自动提款金额 ：{} 的待结算记录，期初余额：{}", list.getInstitutionId(),outMoney,beforeBalance);
                        //期初余额-结算金额
                        afterBalance = ComDoubleUtil.subBySize(beforeBalance,outMoney.doubleValue(),2);
                        log.info("*************** getSettleOrders 自动提款后 **************,准备执行机构编号为：{} 自动提款金额 ：{} 的待结算记录，期末余额：{}", list.getInstitutionId(), outMoney, afterBalance);
                        //插入结算户流水 --- 已结算记录
                        TcsStFlow tcsStFlow = new TcsStFlow();
                        tcsStFlow.setSTFlow("SF"+IDS.uniqueID());
                        tcsStFlow.setRefcnceFlow(settleOrder.getId());
                        tcsStFlow.setTradetype(TradeConstant.WD);
                        tcsStFlow.setMerchantid(settleOrder.getInstitutionCode());
                        tcsStFlow.setMerOrderNo(settleOrder.getId());
                        tcsStFlow.setTxncurrency(settleOrder.getTxncurrency());
                        //提款金额
                        tcsStFlow.setTxnamount(-1*settleOrder.getTxnamount().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                        tcsStFlow.setFee(0.0D);
                        tcsStFlow.setFeecurrency(settleOrder.getTxncurrency());
                        tcsStFlow.setChannelCost(0.0D);
                        tcsStFlow.setChannelcostcurrency(settleOrder.getTxncurrency());
                        tcsStFlow.setRevokemount(0.0D);
                        //1正常资金
                        tcsStFlow.setBalancetype(1);
                        tcsStFlow.setAccountNo(account.getId());
                        //结算状态 2已结算
                        tcsStFlow.setSTstate(2);
                        tcsStFlow.setBusinessType(1);
                        tcsStFlow.setShouldSTtime(new Date());
                        tcsStFlow.setActualSTtime(new Date());
                        tcsStFlow.setSysorderid(settleOrder.getId());
                        tcsStFlow.setAddDatetime(new Date());
                        tcsStFlow.setSltamount(-1*settleOrder.getTxnamount().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                        tcsStFlow.setSltcurrency(settleOrder.getTxncurrency());
                        tcsStFlow.setTxnexrate(0.0D);
                        tcsStFlow.setGatewayFee(0.0D);
                        //是否需要处理清除 清算表 不需要
                        tcsStFlow.setNeedClear(1);
                        tcsStFlow.setTxndesc("提款");
                        tcsStFlow.setRemark("自动提款生成结算交易");
                        tcsStFlowLists.add(tcsStFlow);

                        //插入商户账户流水表（结算户）--提款记录
                        TmMerChTvAcctBalance tma = new TmMerChTvAcctBalance();
                        tma.setFlow("MV" + IDS.uniqueID());
                        tma.setMerchantid(settleOrder.getInstitutionCode());
                        tma.setVaccounId(account.getId());
                        //结算户
                        tma.setType(2);
                        //1正常资金
                        tma.setBalancetype(1);
                        tma.setBussinesstype(1);
                        tma.setCurrency(settleOrder.getTxncurrency());
                        tma.setReferenceflow(settleOrder.getId());
                        tma.setTradetype(TradeConstant.WD);
                        tma.setTxnamount(settleOrder.getTxnamount().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                        tma.setSltamount(settleOrder.getTxnamount().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                        tma.setSltcurrency(settleOrder.getTxncurrency());
                        tma.setIncome(0.0D);
                        tma.setOutcome(settleOrder.getTxnamount().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                        tma.setFee(0.0D);
                        //原账户余额
                        tma.setBalance(beforeBalance);
                        //变动后账户余额
                        tma.setAfterbalance(afterBalance);
                        tma.setSltexrate(Double.parseDouble("1"));
                        tma.setSysAddDate(new Date());
                        tma.setBalanceTimestamp(new Date());
                        tma.setGatewayFee(0.0D);
                        tma.setRemark("自动提款生成结算交易");
                        tmMerChTvAcctBalanceLists.add(tma);
                        beforeBalance = afterBalance;
                    }else{
                        log.info("机构结算交易对应的银行卡信息不存在：institutionCode={},accountCode={},currency={}",list.getInstitutionId(),list.getAccountCode(),list.getCurrency());
                        //回滚
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                        return;
                    }
                }
                //数据库操作
                //变更后的账户信息
                account.setId(account.getId());
                account.setVersion(account.getVersion());
                account.setSettleBalance(new BigDecimal(afterBalance));
                account.setUpdateTime(new Date());
                account.setRemark("定时任务自动提款更新");
                int result2 =  tcsStFlowMapper.insertList(tcsStFlowLists);
                int result3 = tmMerChTvAcctBalanceMapper.insertList(tmMerChTvAcctBalanceLists);
                int result4 = settleOrderMapper.insertList(settleOrderLists);
                int result = accountMapper.updateAccountByPrimaryKey(account);
                if(result2!=tcsStFlowLists.size() || result3!=tmMerChTvAcctBalanceLists.size() || result==0 || result4!=settleOrderLists.size()){
                    //回滚
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return;
                }
            }catch (Exception e){
                log.error("**************** getSettleOrders 单组自动提款功能 ************** 机构编号：{} ， 币种 ：{} ，异常：{}", institutionCode, currency, e);
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }finally {
                while (!redisService.releaseLock(key)) {
                    log.info("******************* 自动提款功能 release lock failed ******************** ：{} ", key);
                }
                log.info("********************* 自动提款功能 release lock success ******************** : {}", key);
            }
        }else {
            log.info("********************* 自动提款功能 get lock failed ******************** : {} : " + key);
        }
    }
}
