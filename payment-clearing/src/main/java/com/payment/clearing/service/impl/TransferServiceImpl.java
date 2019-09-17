package com.payment.clearing.service.impl;

import com.payment.clearing.constant.Const;
import com.payment.clearing.dao.AccountMapper;
import com.payment.clearing.dao.TcsStFlowMapper;
import com.payment.clearing.dao.TmMerChTvAcctBalanceMapper;
import com.payment.clearing.service.TransferService;
import com.payment.clearing.utils.ComDoubleUtil;
import com.payment.clearing.utils.DateUtil;
import com.payment.common.dto.TransferFundDTO;
import com.payment.common.entity.Account;
import com.payment.common.entity.TcsStFlow;
import com.payment.common.entity.TmMerChTvAcctBalance;
import com.payment.common.exception.BusinessException;
import com.payment.common.redis.RedisService;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.common.utils.IDS;
import com.payment.common.utils.MD5;
import com.payment.common.utils.SignTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @description: 转账服务
 * @author: YangXu
 * @create: 2019-08-02 14:10
 **/
@Slf4j
@Service
public class TransferServiceImpl implements TransferService {


    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private TcsStFlowMapper tcsStFlowMapper;

    @Autowired
    private TmMerChTvAcctBalanceMapper tmMerChTvAcctBalanceMapper;

    @Autowired
    private RedisService redisService;

    /**
     * @return DataMessage    返回类型
     * @Date:2018年8月7日 下午4:35:32
     * @Desc:转账申请输入参数校验
     */
    @Override
    public BaseResponse verificationAPIInputParamter(TransferFundDTO cstar, String md5key) {
        log.info("****************** verificationAPIInputParamter **************** #开始，时间：{}", new Date());
        BaseResponse message = new BaseResponse();
        message.setCode(Const.Code.FAILED);// 默认失败
        message.setMsg(Const.Code.FAILED_MSG);
        Map<String, String> m = new HashMap<String, String>();//签名map
        DecimalFormat decimalFormat = new DecimalFormat("###0.00");//格式化设置
        DecimalFormat decimalFormat5 = new DecimalFormat("###0.00000");//汇率格式化设置
        if (cstar == null) {
            //输入参数为空
            log.info("****************** verificationAPIInputParamter **************** # 输入参数为空，时间：{}", new Date());
            return message;
        }
        //version
        if (cstar.getVersion() == null || cstar.getVersion().equals("") || !"v1.0".equalsIgnoreCase(cstar.getVersion())) {
            //版本号有误
            log.info("****************** verificationAPIInputParamter **************** # version字段不合法，时间：{}", new Date());
            message.setCode(Const.Code.CODE_VersionIllegal);
            message.setMsg(Const.Code.MSG_VersionIllegal);
            return message;
        }
        //判断字符集inputCharset
        if (cstar.getInputCharset() == null || cstar.getInputCharset().equals("")) {
            log.info("****************** verificationAPIInputParamter **************** # InputCharset字段不合法，时间：{}", new Date());
            message.setCode(Const.Code.CODE_InputCharsetIllegal);
            message.setMsg(Const.Code.MSG_InputCharsetIllegal + ":InputCharset为空");
            return message;
        } else {
            if (!cstar.getInputCharset().equals("1") && !cstar.getInputCharset().equals("2") && !cstar.getInputCharset().equals("3")) {
                log.info("****************** verificationAPIInputParamter **************** # InputCharset字段不合法，时间：{}", new Date());
                message.setCode(Const.Code.CODE_InputCharsetIllegal);
                message.setMsg(Const.Code.MSG_InputCharsetIllegal + ":InputCharset必须为：1/2/3");
                return message;
            }
        }
        //判断language
        if (cstar.getLanguage() == null || cstar.getLanguage().equals("")) {
            log.info("****************** verificationAPIInputParamter **************** # language字段不合法，时间：{}", new Date());
            message.setCode(Const.Code.CODE_LanguageIllegal);
            message.setMsg(Const.Code.MSG_LanguageIllegal + ":language为空");
            return message;
        } else {
            if (!cstar.getLanguage().equals("1")) {
                log.info("****************** verificationAPIInputParamter **************** # language字段不合法，时间：{}", new Date());
                message.setCode(Const.Code.CODE_LanguageIllegal);
                message.setMsg(Const.Code.MSG_LanguageIllegal + ":language目前只能为1");
                return message;
            }
        }
        //判断fromMerchantId
        if (cstar.getFromMerchantId() == null || cstar.getFromMerchantId().equals("")) {
            log.info("****************** verificationAPIInputParamter **************** # language字段不合法，时间：{}", new Date());
            message.setCode(Const.Code.CODE_MerchantIdIllegal);
            message.setMsg(Const.Code.MSG_MerchantIdIllegal + ":MerchantId字段不合法");
            return message;
        }
        //判断fromVAccountNo
        if (cstar.getFromVAccountNo() == null || cstar.getFromVAccountNo().equals("")) {
            log.info("****************** verificationAPIInputParamter **************** # fromVAccountNo校验失败，时间：{}", new Date());
            message.setCode(Const.Code.CODE_ValidateFail);
            message.setMsg(Const.Code.MSG_ValidateFail + ":fromVAccountNo为空");
            return message;
        }
        //判断type
        if (!cstar.getType().equals("2")) {
            log.info("****************** verificationAPIInputParamter **************** # 清算户（不可用），时间：{}", new Date());
            message.setCode(Const.Code.CODE_ValidateFail);
            message.setMsg(Const.Code.MSG_ValidateFail + ":type目前只能为2");
            return message;
        }

        //判断toMerchantId
        if (cstar.getToMerchantId() == null || cstar.getToMerchantId().equals("")) {
            log.info("****************** verificationAPIInputParamter **************** # MerchantId字段不合法，时间：{}", new Date());
            message.setCode(Const.Code.CODE_MerchantIdIllegal);
            message.setMsg(Const.Code.MSG_MerchantIdIllegal + ":toMerchantId为空");
            return message;
        }

        //判断toAccountNo
        if (cstar.getToAccountNo() == null || cstar.getToAccountNo().equals("")) {
            log.info("****************** verificationAPIInputParamter **************** # toAccountNo字段不合法，时间：{}", new Date());
            message.setCode(Const.Code.CODE_ValidateFail);
            message.setMsg(Const.Code.MSG_ValidateFail + ":toAccountNo为空");
            return message;
        }
        //判断refcnceFlow是否为空，长度是否超过35
        if (cstar.getRefcnceFlow() == null || cstar.getRefcnceFlow().equals("") || cstar.getRefcnceFlow().length() > 35) {
            log.info("****************** verificationAPIInputParamter **************** # refcnceFlow字段不合法，时间：{}", new Date());
            message.setCode(Const.Code.CODE_ValidateFail);
            message.setMsg(Const.Code.MSG_ValidateFail + ":refcnceFlow不合法");
            return message;
        }
        //判断tradetype
        if (cstar.getTradetype() == null || cstar.getTradetype().equals("")) {
            log.info("****************** verificationAPIInputParamter **************** # tradetype为空，时间：{}", new Date());
            message.setCode(Const.Code.CODE_ValidateFail);
            message.setMsg(Const.Code.MSG_ValidateFail + ":tradetype为空");
            return message;
        } else {
            if (!cstar.getTradetype().equals("TA")) {
                log.info("****************** verificationAPIInputParamter **************** # tradetype目前只能为:TA，时间：{}", new Date());
                message.setCode(Const.Code.CODE_ValidateFail);
                message.setMsg(Const.Code.MSG_ValidateFail + ":tradetype目前只能为:TA");
                return message;
            }
        }
        //判断merOrderNo是否为空，长度是否超过35
        if (cstar.getMerOrderNo() == null || cstar.getMerOrderNo().equals("") || cstar.getMerOrderNo().length() > 35) {
            log.info("****************** verificationAPIInputParamter **************** # merOrderNo不合法，时间：{}", new Date());
            message.setCode(Const.Code.CODE_ValidateFail);
            message.setMsg(Const.Code.MSG_ValidateFail + ":merOrderNo不合法");
            return message;
        }

        //判断outtxncurrency
        if (cstar.getOuttxncurrency() == null || cstar.getOuttxncurrency().equals("") || cstar.getOuttxncurrency().length() != 3) {
            log.info("****************** verificationAPIInputParamter **************** # outtxncurrency不合法，时间：{}", new Date());
            message.setCode(Const.Code.CODE_ValidateFail);
            message.setMsg(Const.Code.MSG_ValidateFail + ":outtxncurrency不合法");
            return message;
        }

        //判断outtxnamount
        if (Double.parseDouble(cstar.getOuttxnamount()) >= 0) {
            log.info("****************** verificationAPIInputParamter **************** # outtxnamount必须小于0，时间：{}", new Date());
            message.setCode(Const.Code.CODE_ValidateFail);
            message.setMsg(Const.Code.MSG_ValidateFail + ":outtxnamount必须小于0");
            return message;
        }
        //判断txnexRate
        if (Double.parseDouble(cstar.getTxnexRate()) <= 0) {
            log.info("****************** verificationAPIInputParamter **************** # txnexRate必须大于0，时间：{}", new Date());
            message.setCode(Const.Code.CODE_ValidateFail);
            message.setMsg(Const.Code.MSG_ValidateFail + ":txnexRate必须大于0");
            return message;
        }
        //判断intxncurrency
        if (cstar.getIntxncurrency() == null || cstar.getIntxncurrency().equals("") || cstar.getIntxncurrency().length() != 3) {
            log.info("****************** verificationAPIInputParamter **************** # intxncurrency不合法，时间：{}", new Date());
            message.setCode(Const.Code.CODE_ValidateFail);
            message.setMsg(Const.Code.MSG_ValidateFail + ":intxncurrency不合法");
            return message;
        }
        //判断intxnamount
        if (Double.parseDouble(cstar.getIntxnamount()) <= 0) {
            log.info("****************** verificationAPIInputParamter **************** # intxnamount必须大于0，时间：{}", new Date());
            message.setCode(Const.Code.CODE_ValidateFail);
            message.setMsg(Const.Code.MSG_ValidateFail + ":intxnamount必须大于0");
            return message;
        }

        //判断state
        if (!cstar.getState().equals("1")) {
            log.info("****************** verificationAPIInputParamter **************** # state目前只能为:1，时间：{}", new Date());
            message.setCode(Const.Code.CODE_ValidateFail);
            message.setMsg(Const.Code.MSG_ValidateFail + ":state目前只能为:1");
            return message;
        }
        //判断shouldDealtime 是否为空，日期eg:20180808
        if (cstar.getShouldDealtime() == null || cstar.getShouldDealtime().equals("") || cstar.getShouldDealtime().length() != 8) {
            log.info("****************** verificationAPIInputParamter **************** # shouldDealtime不合法，时间：{}", new Date());
            message.setCode(Const.Code.CODE_ValidateFail);
            message.setMsg(Const.Code.MSG_ValidateFail + ":shouldDealtime不合法,eg:yyyyMMdd");
            return message;
        }
        //判断sysorderid 是否为空，
        if (cstar.getSysorderid() == null || cstar.getSysorderid().equals("") || cstar.getSysorderid().length() > 35) {
            log.info("****************** verificationAPIInputParamter **************** # sysorderid不合法，时间：{}", new Date());
            message.setCode(Const.Code.CODE_ValidateFail);
            message.setMsg(Const.Code.MSG_ValidateFail + ":sysorderid不合法");
            return message;
        }
        //判断fee
        if (Double.parseDouble(cstar.getFee()) < 0) {
            log.info("****************** verificationAPIInputParamter **************** # fee必须大于等于0，时间：{}", new Date());
            message.setCode(Const.Code.CODE_ValidateFail);
            message.setMsg(Const.Code.MSG_ValidateFail + ":fee必须大于等于0");
            return message;
        }

        //判断channelCost
        if (Double.parseDouble(cstar.getChannelCost()) < 0) {
            log.info("****************** verificationAPIInputParamter **************** # channelCost不合法，时间：{}", new Date());
            message.setCode(Const.Code.CODE_ValidateFail);
            message.setMsg(Const.Code.MSG_ValidateFail + ":channelCost不合法");
            return message;
        }
        //判断balancetype
        if (Integer.parseInt(cstar.getBalancetype()) != 1) {
            log.info("****************** verificationAPIInputParamter **************** # balancetype目前只能为:1，时间：{}", new Date());
            message.setCode(Const.Code.CODE_ValidateFail);
            message.setMsg(Const.Code.MSG_ValidateFail + ":balancetype目前只能为:1");
            return message;
        }

        //判断signMsg 是否为空，
        if (cstar.getSignMsg() == null || cstar.getSignMsg().equals("")) {
            log.info("****************** verificationAPIInputParamter **************** # signMsg不合法，时间：{}", new Date());
            message.setCode(Const.Code.CODE_ValidateFail);
            message.setMsg(Const.Code.MSG_ValidateFail + ":signMsg不合法");
            return message;
        }

        //判断feeParty 是否为空，1:表示转出方，2表示转入方
        if (Integer.parseInt(cstar.getFeeParty()) != 1 && Integer.parseInt(cstar.getFeeParty()) != 2) {
            log.info("****************** verificationAPIInputParamter **************** # feeParty不合法，时间：{}", new Date());
            message.setCode(Const.Code.CODE_ValidateFail);
            message.setMsg(Const.Code.MSG_ValidateFail + ":feeParty不合法,取值只能是：1 or 2");
            return message;
        }

        /******* 验签 *****/
        m.put("version", cstar.getVersion());
        m.put("inputCharset", cstar.getInputCharset());
        m.put("language", cstar.getLanguage());
        m.put("fromMerchantId", cstar.getFromMerchantId());
        m.put("fromVAccountNo", cstar.getFromVAccountNo());
        m.put("type", cstar.getType() + "");
        m.put("toMerchantId", cstar.getToMerchantId());
        m.put("toAccountNo", cstar.getToAccountNo());
        m.put("refcnceFlow", cstar.getRefcnceFlow());
        m.put("tradetype", cstar.getTradetype());
        m.put("merOrderNo", cstar.getMerOrderNo());
        m.put("outtxncurrency", cstar.getOuttxncurrency());
        m.put("outtxnamount", decimalFormat.format(cstar.getOuttxnamount()));
        m.put("txnexRate", decimalFormat5.format(cstar.getTxnexRate()));//汇率的小数要保留5位
        m.put("intxncurrency", cstar.getIntxncurrency());
        m.put("intxnamount", decimalFormat.format(cstar.getIntxnamount()));
        m.put("state", cstar.getState() + "");
        m.put("shouldDealtime", cstar.getShouldDealtime());
        m.put("sysorderid", cstar.getSysorderid());
        m.put("fee", decimalFormat.format(cstar.getFee()));
        m.put("channelCost", decimalFormat.format(cstar.getChannelCost()));
        m.put("balancetype", cstar.getBalancetype() + "");
        m.put("feeParty", cstar.getFeeParty() + "");
        m.put("remark", cstar.getRemark());
        //排序
        //signTools st=new signTools();
        String mysignmsg = SignTools.getSignStr(m);
        if (md5key == null || md5key.equals("")) {
            //获取交易系统MD5Key为空
            log.info("****************** verificationAPIInputParamter **************** # 获取交易系统MD5Key为空，验证不通过,时间：" + new Date());
            return message;
        }
        log.info("****************** verificationAPIInputParamter **************** #商户转账提交信息签名前的明文：{}", md5key + mysignmsg);
        String sign = MD5.MD5Encode(md5key + mysignmsg);
        log.info("****************** verificationAPIInputParamter **************** #商户转账提交信息签名前的密文：{}", sign);
        if (!cstar.getSignMsg().equals(sign)) {
            message.setCode(Const.CSCode.CODE_CS0008);
            message.setMsg(Const.CSCode.MSG_CS0008);
            log.info("****************** verificationAPIInputParamter **************** # 验证签名不通过");
            return message;
        }

        //出账
        Date stdate = DateUtil.parse(cstar.getShouldDealtime(), "yyyyMMdd");//应结时间
        TcsStFlow outst = new TcsStFlow();
        outst.setSTFlow("SF" + IDS.uniqueID());
        outst.setRefcnceFlow(cstar.getRefcnceFlow());
        outst.setTradetype(cstar.getTradetype());
        outst.setMerchantid(cstar.getFromMerchantId());
        outst.setMerOrderNo(cstar.getMerOrderNo());
        outst.setTxncurrency(cstar.getOuttxncurrency());
        outst.setTxnamount(Double.parseDouble(cstar.getOuttxnamount()));
        outst.setFeecurrency(cstar.getOuttxncurrency());
        outst.setChannelCost(Double.parseDouble(cstar.getChannelCost()));
        outst.setChannelcostcurrency(cstar.getOuttxncurrency());
        outst.setBusinessType(1);
        outst.setBalancetype(2);//出账需要冻结，所以资金类型需要改为冻结资金
        outst.setAccountNo(cstar.getFromVAccountNo());
        outst.setSTstate(Integer.parseInt(cstar.getState()));
        outst.setShouldSTtime(stdate);
        outst.setAddDatetime(new Date());
        outst.setSysorderid(cstar.getSysorderid());
        outst.setSltamount(Double.parseDouble(cstar.getOuttxnamount()));
        outst.setSltcurrency(cstar.getOuttxncurrency());
        outst.setTxnexrate(Double.parseDouble("1"));
        outst.setGatewayFee(Double.parseDouble("0"));
        outst.setNeedClear(1);//不需要清算
        outst.setRemark("转账出款：" + cstar.getRemark());
        //入账
        TcsStFlow inst = new TcsStFlow();
        inst.setSTFlow("SF" + IDS.uniqueID());
        inst.setRefcnceFlow(cstar.getRefcnceFlow());
        inst.setTradetype(cstar.getTradetype());
        inst.setMerchantid(cstar.getToMerchantId());
        inst.setMerOrderNo(cstar.getMerOrderNo());
        inst.setTxncurrency(cstar.getIntxncurrency());
        inst.setTxnamount(Double.parseDouble(cstar.getIntxnamount()));
        inst.setFeecurrency(cstar.getIntxncurrency());
        inst.setChannelCost(Double.parseDouble(cstar.getChannelCost()));
        inst.setChannelcostcurrency(cstar.getIntxncurrency());
        inst.setBusinessType(1);
        inst.setBalancetype(Integer.parseInt(cstar.getBalancetype()));
        inst.setAccountNo(cstar.getToAccountNo());
        inst.setSTstate(Integer.parseInt(cstar.getState()));
        inst.setShouldSTtime(stdate);
        inst.setAddDatetime(new Date());
        inst.setSysorderid(cstar.getSysorderid());
        inst.setSltamount(Double.parseDouble(cstar.getIntxnamount()));
        inst.setSltcurrency(cstar.getIntxncurrency());
        inst.setTxnexrate(Double.parseDouble("1"));
        inst.setGatewayFee(Double.parseDouble("0"));
        inst.setNeedClear(1);//不需要清算
        inst.setRemark("转账入款：" + cstar.getRemark());
        //根据费用承担方来判断手续费
        if (Integer.parseInt(cstar.getFeeParty()) == 1) {
            //转出方承担
            outst.setFee(Double.parseDouble(cstar.getFee()));
            inst.setFee(Double.parseDouble("0"));
        } else if (Integer.parseInt(cstar.getFeeParty()) == 2) {
            //转入方承担
            outst.setFee(Double.parseDouble("0"));
            inst.setFee(Double.parseDouble(cstar.getFee()));
        }
        message.setCode(Const.Code.OK);
        message.setMsg(Const.Code.OK_MSG);
        message.setData(new Object[]{outst, inst});
        return message;

    }

    @Override
    @Transactional
    public BaseResponse stTransferAccount(TransferFundDTO cstar, TcsStFlow outst, TcsStFlow inst) {
        log.info("****************** stTransferAccount **************** # 开始，时间：{}", new Date());
        BaseResponse message = new BaseResponse();
        message.setCode(Const.Code.FAILED);// 默认失败
        message.setMsg(Const.Code.FAILED_MSG);

        String fromkey = Const.Redis.CLEARING_KEY + "_" + cstar.getFromMerchantId() + "_" + cstar.getOuttxncurrency();
        String tokey = Const.Redis.CLEARING_KEY + "_" + cstar.getToMerchantId() + "_" + cstar.getIntxncurrency();
        log.info("************ CLEARING_KEY *************** tokey:{}", tokey);
        log.info("************ CLEARING_KEY *************** fromkey:{}", fromkey);
        if (redisService.lock(tokey, Const.Redis.expireTime) && redisService.lock(fromkey, Const.Redis.expireTime)) {
            log.info("***************** get lock success tokey :【{}】 ************** ", tokey);
            log.info("***************** get lock success fromkey :【{}】 ************** ", fromkey);
            try {
                /************* 逻辑校检 ************/
                //转出币种，金额，汇率是否等于转入币种，金额，
                double inamt = ComDoubleUtil.mul(Double.parseDouble(cstar.getOuttxnamount()), Double.parseDouble(cstar.getTxnexRate()), 2);
                if ((inamt + Double.parseDouble(cstar.getIntxnamount())) != 0) {
                    message.setCode(Const.Code.CODE_ValidateFail);
                    message.setMsg(Const.Code.MSG_ValidateFail + ":转出币种，金额，汇率计算后不等于转入金额，币种");
                    log.info("****************** stTransferAccount **************** # 转出币种，金额，汇率计算后不等于转入金额，币种，时间：{}", new Date());
                    return message;
                }
                Account mvaout = new Account();
                mvaout.setInstitutionId(cstar.getFromMerchantId());
                mvaout.setCurrency(cstar.getOuttxncurrency());
                mvaout.setId(cstar.getFromVAccountNo());
                mvaout.setEnabled(true);//表示正常
                Account mva_out = accountMapper.selectOne(mvaout);
                if (mva_out == null || mva_out.getSettleBalance() == null || mva_out.getFreezeBalance() == null) {
                    message.setCode(Const.Code.CODE_ValidateFail);
                    message.setMsg(Const.Code.MSG_ValidateFail + ":申请机构，商户号，币种对应结算户校验不存在");
                    log.info("****************** stTransferAccount **************** # 转出币种，金额，汇率计算后不等于转入金额，币种，时间：{}", new Date());
                    return message;
                }
                //转出账户的可用余额减去申请金额必须大于0
                double avabalance = 0;//可用余额
                if (Integer.parseInt(cstar.getFeeParty()) == 1) {
                    //表示转出方承担手续费
                    avabalance = mva_out.getSettleBalance().doubleValue() - mva_out.getFreezeBalance().doubleValue() - Double.parseDouble(cstar.getOuttxnamount()) - Double.parseDouble(cstar.getFee());//可用余额
                } else {
                    avabalance = mva_out.getSettleBalance().doubleValue() - mva_out.getFreezeBalance().doubleValue() - Double.parseDouble(cstar.getOuttxnamount());//可用余额
                }
                if (avabalance < 0) {
                    message.setCode(Const.Code.CODE_ValidateFail);
                    message.setMsg(Const.Code.MSG_ValidateFail + ":申请结算户的资金不足以操作本次转出");
                    log.info("****************** stTransferAccount **************** #申请结算户的资金不足以操作本次转出，时间：{}", new Date());
                    return message;
                }
                //校验转入机构，商户号，币种对应结算户是否存在
                Account mvain = new Account();
                mvain.setInstitutionId(cstar.getToMerchantId());
                mvain.setCurrency(cstar.getIntxncurrency());
                mvain.setId(cstar.getToAccountNo());
                mvain.setEnabled(true);//表示正常
                Account mva_in = accountMapper.selectOne(mvain);
                if (mva_in == null || mva_in.getSettleBalance() == null || mva_in.getFreezeBalance() == null) {
                    message.setCode(Const.Code.CODE_ValidateFail);
                    message.setMsg(Const.Code.MSG_ValidateFail + ":转入机构，商户号，币种对应结算户校验不存在");
                    log.info("****************** stTransferAccount **************** #转入机构，商户号，币种对应结算户校验不存在 ,时间：{}", new Date());
                    return message;
                }
                //转出账户的可用余额减去申请金额必须大于0
                double avabalance_in = 0;//可用余额
                if (Integer.parseInt(cstar.getFeeParty()) == 2) {
                    //表示转入方承担
                    avabalance_in = mva_in.getSettleBalance().doubleValue() - mva_in.getFreezeBalance().doubleValue() + Double.parseDouble(cstar.getIntxnamount()) - Double.parseDouble(cstar.getFee());//可用余额
                } else {
                    avabalance_in = mva_in.getSettleBalance().doubleValue() - mva_in.getFreezeBalance().doubleValue() + Double.parseDouble(cstar.getIntxnamount());//可用余额
                }
                if (avabalance_in < 0) {
                    message.setCode(Const.Code.CODE_ValidateFail);
                    message.setMsg(Const.Code.MSG_ValidateFail + ":转入结算户的资金不足以操作本次转出");
                    log.info("****************** stTransferAccount **************** #转入结算户的资金不足以操作本次转出，时间：{}", new Date());
                    return message;
                }
                int outrows = tcsStFlowMapper.insertSelective(outst);
                int inrows = tcsStFlowMapper.insertSelective(inst);
                //update账户冻结资金
                double beforebalance = mva_out.getFreezeBalance().doubleValue();//冻结前余额
                double afterbalance = beforebalance - outst.getSltamount();//冻结后余额
                Account mva = new Account();
                mva.setFreezeBalance(new BigDecimal(-1 * outst.getSltamount()));//出账金额为负数，加冻结就需要乘以负一
                mva.setId(outst.getAccountNo());
                mva.setUpdateTime(new Date());
                mva.setVersion(mva_out.getVersion());
                //先冻结出账资金
                int frorows = accountMapper.updateFrozenBalance(mva);

                //插入冻结资金流水
                TmMerChTvAcctBalance mab = new TmMerChTvAcctBalance();
                mab.setFlow("MV" + IDS.uniqueID());
                mab.setAfterbalance(afterbalance);
                mab.setOrganId(outst.getOrganId());//所属机构号
                mab.setMerchantid(outst.getMerchantid());
                mab.setVaccounId(outst.getAccountNo());
                mab.setBalance(beforebalance);
                Date date = new Date();
                Timestamp nowTS = new Timestamp(date.getTime());
                mab.setBalanceTimestamp(nowTS);
                mab.setSysAddDate(new Date());
                mab.setBalancetype(outst.getBalancetype());
                mab.setBussinesstype(outst.getBusinessType());
                mab.setFee(outst.getFee());
                mab.setGatewayFee(outst.getGatewayFee());//20170615添加的网关状态手续费
                mab.setCurrency(outst.getTxncurrency());
                mab.setSltcurrency(outst.getSltcurrency());
                mab.setSltexrate(outst.getTxnexrate());
                //冻结资金流水在加冻结的时候都是收入
                mab.setIncome(-1 * outst.getSltamount());
                mab.setOutcome(Double.parseDouble("0"));
                mab.setSltamount(-1 * outst.getSltamount());
                mab.setTxnamount(-1 * outst.getTxnamount());
                mab.setReferenceflow(outst.getRefcnceFlow());
                mab.setRemark(outst.getRemark());
                mab.setTradetype(outst.getTradetype());
                mab.setType(2);
                int fr_flow = tmMerChTvAcctBalanceMapper.insertSelective(mab);
                if (outrows == 1 && inrows == 1 && frorows == 1 && fr_flow == 1) {
                    //处理成功
                    message.setCode(Const.Code.OK);
                    message.setMsg(Const.Code.OK_MSG);
                    log.info("****************** stTransferAccount **************** #处理成功，结束，时间：{}", new Date());
                } else {
                    //处理失败
                    log.info("****************** stTransferAccount ****************#处理失败，结束，时间：{}", new Date());
                    throw new BusinessException(EResultEnum.ERROR.getCode());
                }
            } catch (Exception e) {
                log.info("****************** stTransferAccount ****************# 异常时间：{},Exception :{}", new Date(), e);
                throw new BusinessException(EResultEnum.ERROR.getCode());
            } finally {
                while (!redisService.releaseLock(fromkey)) {
                    log.info("******************* release lock failed ******************** fromkey ：{} ", fromkey);
                }
                log.info("********************* release lock success ******************** fromkey : {}", fromkey);
                while (!redisService.releaseLock(tokey)) {
                    log.info("******************* release lock failed ******************** tokey ：{} ", tokey);
                }
                log.info("********************* release lock success ******************** tokey : {}", tokey);
            }
        } else {
            log.info("********************* get lock failed ******************** fromkey : {} , tokey : {} ", fromkey, tokey);
        }

        return message;
    }
}
