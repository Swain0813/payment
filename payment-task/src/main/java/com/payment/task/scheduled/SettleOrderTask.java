package com.payment.task.scheduled;
import com.alibaba.fastjson.JSON;
import com.payment.common.constant.AD3MQConstant;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.constant.TradeConstant;
import com.payment.common.dto.FundChangeDTO;
import com.payment.common.entity.*;
import com.payment.common.redis.RedisService;
import com.payment.common.response.BaseResponse;
import com.payment.common.utils.DateToolUtils;
import com.payment.common.utils.IDS;
import com.payment.common.vo.FundChangeVO;
import com.payment.task.dao.*;
import com.payment.task.feign.MessageFeign;
import com.payment.task.rabbitmq.RabbitMQSender;
import com.payment.task.service.ClearingService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 定时跑批生成结算交易
 * 已经废除转移到清结算服务里面了
 */
@Component
@Slf4j
@Api(value = "生成结算交易定时任务")
public class SettleOrderTask {

    @Autowired
    private MessageFeign messageFeign;

    @Value("${custom.developer.mobile}")
    private String developerMobile;

    @Value("${custom.developer.email}")
    private String developerEmail;

    @Autowired
    private SettleOrderMapper settleOrderMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private BankCardMapper bankCardMapper;

    @Autowired
    private ClearingService clearingService;

    @Autowired
    private RabbitMQSender rabbitMQSender;

    @Autowired
    private InstitutionMapper institutionMapper;

    @Autowired
    private SettleControlMapper settleControlMapper;

    /**
     *定时跑批生成结算交易
     * 自动提款代码重新在清结算服务开发，该定时任务暂停使用
     */
    //@Scheduled(cron = "0/10 * * * * ? ")//每10秒执行一次 测试用
    //@Scheduled(cron = "0 0 7 ? * *")//每天早上7点跑批一次
    @Transactional
    public  void  getSettleOrders(){
        log.info("************开始定时跑批生成结算交易****************");
        //判断当日是否已经执行了
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
        if(redisService.get(TradeConstant.FLAY_KEY.concat("_").concat(sdf1.format(new Date())))!=null){
            return;
        }
        try{
          //获取账户表中(结算账户余额-冻结账户余额)大于0的数据
            List<Account> lists = accountMapper.getAccounts();
            for(Account list:lists){
                //机构结算金额
                BigDecimal outMoney = list.getSettleBalance().subtract(list.getFreezeBalance());//自动提现金额即结算金额-冻结金额
                //根据账号id查询账户关联表信息
                SettleControl settleControl = settleControlMapper.selectByAccountId(list.getId());
                //账户关联信息不存在的场合
                if(settleControl==null){
                    log.info("*******机构对应的账户id的关联信息不存在****** InstitutionId:{}，AccountId:{}",list.getInstitutionId(),list.getId());
                    continue;
                }
                //存在的场合但是可提现金额小于用户设置的最小提现金额
                if (settleControl!=null && outMoney.compareTo(settleControl.getMinSettleAmount()) == -1) {
                    continue;
                }
                //获取银行卡信息
                BankCard bankCard = bankCardMapper.getBankCard(list.getInstitutionId(),list.getAccountCode(),list.getCurrency());
                if(bankCard!=null && bankCard.getBankAccountCode()!=null && bankCard.getBankCodeCurrency()!=null){//银行卡卡号和银行卡币种是必填的
                    //根据机构code和银行卡code获取机构交易表中当日第一条数据的批次号
                    String batchNo = settleOrderMapper.getBatchNo(list.getInstitutionId(),bankCard.getBankCode());
                    //机构结算表的数据的设置
                    SettleOrder settleOrder = new SettleOrder();
                    settleOrder.setId("J"+ IDS.uniqueID());//结算交易的流水号
                    if(StringUtils.isEmpty(batchNo)){
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");//根据年月日时分秒毫秒生成批次号
                        settleOrder.setBatchNo("P"+sdf.format(new Date()));//批次号
                    }else {//非空的场合
                        settleOrder.setBatchNo(batchNo);//批次号
                    }
                    settleOrder.setInstitutionCode(list.getInstitutionId());//机构编号
                    Institution institutionInfo = this.getInstitutionInfo(list.getInstitutionId());
                    //机构地址
                    settleOrder.setInstitutionAdress(Objects.requireNonNull(institutionInfo).getInstitutionAdress());
                    //机构邮编
                    settleOrder.setInstitutionPostalCode(institutionInfo.getInstitutionPostalCode());
                    settleOrder.setInstitutionName(list.getInstitutionName());//机构名称
                    settleOrder.setTxncurrency(list.getCurrency());//交易币种
                    settleOrder.setTxnamount(outMoney);//交易金额即结算金额-冻结金额
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
                    settleOrder.setIntermediaryOtherCode(bankCard.getIntermediaryOtherCode());//中间行银行其他code
                    settleOrder.setTradeStatus(AsianWalletConstant.SETTLING);//结算中
                    settleOrder.setSettleType(AsianWalletConstant.SETTLE_AUTO);//自动结算
                    settleOrder.setCreateTime(new Date());//创建时间
                    settleOrder.setCreator("定时跑批生成结算交易任务");//创建人
                    //将机构结算金额通过调账调出去上报清结算
                    FundChangeDTO fundChangeDTO = new FundChangeDTO(settleOrder);
                    //应结算日期
                    fundChangeDTO.setShouldDealtime(DateToolUtils.formatTimestamp.format(new Date()));
                    BaseResponse cFundChange = clearingService.fundChange(fundChangeDTO, null);
                    if (cFundChange.getCode().equals(String.valueOf(AsianWalletConstant.HTTP_SUCCESS_STATUS))) {//请求成功
                        //返回结果
                        FundChangeVO fundChangeVO = (FundChangeVO) cFundChange.getData();
                        if (StringUtils.isEmpty(fundChangeVO.getRespCode()) || !fundChangeVO.getRespCode().equals(TradeConstant.CLEARING_SUCCESS)) {//业务失败
                            log.info("----------------- 机构结算提款 上报队列 TC_MQ_WD_DL 业务失败 -------------- rabbitMassage : {} ", JSON.toJSON(fundChangeVO));
                            RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(settleOrder));
                            rabbitMQSender.send(AD3MQConstant.TC_MQ_WD_DL, JSON.toJSONString(rabbitMassage));
                        } else {//业务成功
                            //插入数据到机构结算表
                            settleOrderMapper.insertSelective(settleOrder);
                        }
                    } else {//请求失败
                        log.info("----------------- 机构结算提款 上报队列 TC_MQ_WD_DL 请求失败-------------- rabbitMassage : {} ", JSON.toJSON(cFundChange));
                        RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(settleOrder));
                        rabbitMQSender.send(AD3MQConstant.TC_MQ_WD_DL, JSON.toJSONString(rabbitMassage));
                    }
                }else{
                    log.info("机构结算交易对应的银行卡信息不存在：institutionCode={},accountCode={},currency={}",list.getInstitutionId(),list.getAccountCode(),list.getCurrency());
                }
            }
       }catch (Exception e){
            log.error("定时跑批生成结算交易数据发生异常==={}", e.getMessage());
            messageFeign.sendSimple(developerMobile, "定时跑批生成结算交易数据发生异常!");
            messageFeign.sendSimpleMail(developerEmail, "定时跑批生成结算交易数据发生异常", "定时跑批生成结算交易数据发生异常");
        }finally {
            //定时跑批生成结算交易完，在redis里记录一下，说明当日已经跑批
            redisService.set(TradeConstant.FLAY_KEY.concat("_").concat(sdf1.format(new Date())),"true",24 * 60 * 60);//24小时后失效
        }
        log.info("************结束定时跑批生成结算交易****************");
    }

    /**
     * 根据机构code获取机构名称
     *
     * @param institutionCode
     * @return
     */
    private Institution getInstitutionInfo(String institutionCode) {
        //查询机构信息,先从redis获取
        Institution institution = JSON.parseObject(redisService.get(AsianWalletConstant.INSTITUTION_CACHE_KEY.concat("_").concat(institutionCode)), Institution.class);
        if (institution == null) {
            //redis不存在,从数据库获取
            institution = institutionMapper.selectByInstitutionCode(institutionCode);
            if (institution == null) {
                //机构信息不存在
                return null;
            }
            //同步redis
            redisService.set(AsianWalletConstant.INSTITUTION_CACHE_KEY.concat("_").concat(institution.getInstitutionCode()), JSON.toJSONString(institution));
        }
        return institution;
    }

}
