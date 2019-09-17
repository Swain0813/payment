package com.payment.finance.service.impl;

import com.payment.common.config.AuditorProvider;
import com.payment.common.dto.TradeCheckAccountDTO;
import com.payment.common.dto.TradeCheckAccountExportDTO;
import com.payment.common.dto.TradeCheckAccountSettleExportDTO;
import com.payment.common.entity.*;
import com.payment.common.utils.DateToolUtils;
import com.payment.common.utils.IDS;
import com.payment.common.vo.ExportSettleCheckAccountVO;
import com.payment.common.vo.ExportTradeAccountVO;
import com.payment.common.vo.SettleCheckAccountDetailVO;
import com.payment.common.vo.TradeAccountDetailVO;
import com.payment.finance.dao.*;
import com.payment.finance.feign.MessageFeign;
import com.payment.finance.service.InstitutionCheckAccountService;
import com.payment.finance.vo.CheckAccountListVO;
import com.payment.finance.vo.CheckAccountVO;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;

@Service
@Slf4j
@Transactional
public class InstitutionCheckAccountServiceImpl implements InstitutionCheckAccountService {

    @Autowired
    private TradeCheckAccountMapper tradeCheckAccountMapper;

    @Autowired
    private TradeCheckAccountDetailMapper tradeCheckAccountDetailMapper;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderRefundMapper orderRefundMapper;

    @Autowired
    private AuditorProvider auditorProvider;

    @Autowired
    private TcsStFlowMapper tcsStFlowMapper;

    @Autowired
    private SettleCheckAccountDetailMapper settleCheckAccountDetailMapper;

    @Autowired
    private SettleCheckAccountMapper settleCheckAccountMapper;

    @Autowired
    private MessageFeign messageFeign;

    @Value("${custom.developer.mobile}")
    private String developerMobile;

    @Value("${custom.developer.email}")
    private String developerEmail;


    /**
     * 机构交易信息对账
     *
     * @return
     */
    @Override
    public Object tradeAccountCheck() {
        List<TradeCheckAccount> tradeCheckAccounts = null;
        try {
            String yesterday = DateToolUtils.getYesterday();//昨日日期
            //交易订单与退款订单汇总信息计算
            List<CheckAccountVO> checkAccountVOS = ordersMapper.tradeAccountCheck(yesterday);
            if (checkAccountVOS == null || checkAccountVOS.size() == 0) {
                log.info("************机构交易信息对账************ 昨日的机构交易对账信息为空");
                return null;
            }
            //交易对账总表实体集合
            tradeCheckAccounts = new ArrayList<>();
            //设置交易对账总表实体属性
            for (CheckAccountVO checkAccountVO : checkAccountVOS) {
                TradeCheckAccount t = new TradeCheckAccount();
                List<CheckAccountListVO> checkAccountListVOS = checkAccountVO.getCheckAccountListVOS();
                for (CheckAccountListVO checkAccountListVO : checkAccountListVOS) {
                    if (checkAccountListVO.getType().equals("1")) {
                        //交易单
                        t.setFee(checkAccountListVO.getFee());//手续费
                        t.setTotalTradeAmount(checkAccountListVO.getTotalAmount());//交易总金额
                        t.setTotalTradeCount(checkAccountListVO.getTotalCount());//交易总笔数
                    } else {
                        //退款单
                        t.setTotalRefundAmount(checkAccountListVO.getRefundAmount());//退款总金额
                        t.setTotalRefundCount(checkAccountListVO.getRefundCount());//退款总笔数
                    }
                }
                t.setId(IDS.uuid2());//id
                t.setCreateTime(new Date());//创建时间
                t.setInstitutionCode(checkAccountVO.getInstitutionCode());//机构编号
                t.setCurrency(checkAccountVO.getOrderCurrency());//订单币种
                t.setBeginTime(DateToolUtils.getDateByStr(DateToolUtils.getYesterdayStart()));//昨日起始时间
                t.setEndTime(DateToolUtils.getDateByStr(DateToolUtils.getYesterdayEnd()));//昨日结束时间
                tradeCheckAccounts.add(t);
            }
            //交易订单
            List<Orders> ordersList = ordersMapper.selectByDate(yesterday);
            //退款订单
            List<OrderRefund> orderRefundList = orderRefundMapper.selectByDate(yesterday);
            //设置交易对账详细表信息实体
            List<TradeCheckAccountDetail> tradeCheckAccountDetails = new ArrayList<>();
            for (Orders order : ordersList) {
                TradeCheckAccountDetail td = new TradeCheckAccountDetail();
                BeanUtils.copyProperties(order, td);
                td.setId(IDS.uuid2());//id
                td.setCreateTime(new Date());//创建时间
                td.setOrderId(order.getId());//订单id
                td.setOrderCreateTime(order.getCreateTime());//订单创建时间
                td.setPayType(order.getPayMethod());//支付方式
                td.setPayFinishTime(order.getChannelCallbackTime());//支付完成时间
                tradeCheckAccountDetails.add(td);
            }
            for (OrderRefund orderRefund : orderRefundList) {
                TradeCheckAccountDetail td = new TradeCheckAccountDetail();
                BeanUtils.copyProperties(orderRefund, td);
                td.setId(IDS.uuid2());
                td.setCreateTime(new Date());
                td.setOrderId(orderRefund.getId());//订单id
                td.setOrderCreateTime(orderRefund.getCreateTime());//订单创建时间
                td.setPayType(orderRefund.getPayMethod());//支付方式
                td.setRefundStatus(orderRefund.getRefundStatus());//退款状态
                tradeCheckAccountDetails.add(td);
            }
            tradeCheckAccountMapper.insertList(tradeCheckAccounts);
            tradeCheckAccountDetailMapper.insertList(tradeCheckAccountDetails);
        } catch (Exception e) {
            log.error("************************************机构交易信息对账发生异常************************************", e);
            messageFeign.sendSimple(developerMobile, "昨日的机构交易对账定时任务出错!");//短信通知
            messageFeign.sendSimpleMail(developerEmail, "昨日的机构交易对账定时任务出错!", "昨日的机构交易对账定时任务出错!");//邮件通知
        }
        return tradeCheckAccounts;
    }

    /**
     * 机构结算信息对账
     *
     * @return
     */
    @Override
    public int settleAccountCheck(Date time) {
        List<TcsStFlow> tcsStFlowList = tcsStFlowMapper.selectTcsStFlow(DateToolUtils.addDay(time, -1));
        List<SettleCheckAccountDetail> settleCheckAccountDetails = new ArrayList<>();
        for (TcsStFlow tcsStFlow : tcsStFlowList) {
            SettleCheckAccountDetail settleCheckAccountDetail = new SettleCheckAccountDetail();
            BeanUtils.copyProperties(tcsStFlow, settleCheckAccountDetail);
            settleCheckAccountDetail.setId(IDS.uuid2());
            settleCheckAccountDetail.setCreateTime(new Date());
            settleCheckAccountDetails.add(settleCheckAccountDetail);
        }
        if (settleCheckAccountDetails.size() == 0) {
            log.info("------------- 统计结算单 ------------ settleCheckAccountDetails.size = {}", settleCheckAccountDetails.size());
            return 0;
        }
        settleCheckAccountDetailMapper.insertList(settleCheckAccountDetails);

        List<SettleCheckAccount> settleCheckAccounts = settleCheckAccountMapper.statistical(DateToolUtils.addDay(time, -1));
        for (SettleCheckAccount settleCheckAccount : settleCheckAccounts) {
            SettleCheckAccount s = settleCheckAccountMapper.selectByCurrencyAndInstitutionCode(settleCheckAccount.getCurrency(), settleCheckAccount.getInstitutionCode());
            if (s != null) {
                settleCheckAccount.setId(IDS.uuid2());
                settleCheckAccount.setInitialAmount(s.getFinalAmount());
                BigDecimal finalAmount = settleCheckAccount.getAmount().subtract(settleCheckAccount.getFee()).add(s.getFinalAmount());
                settleCheckAccount.setFinalAmount(finalAmount);
                settleCheckAccount.setCheckTime(DateToolUtils.addDay(time, -1));
                settleCheckAccount.setCreateTime(new Date());
            } else {
                settleCheckAccount.setId(IDS.uuid2());
                settleCheckAccount.setInitialAmount(BigDecimal.ZERO);
                BigDecimal finalAmount = settleCheckAccount.getAmount().subtract(settleCheckAccount.getFee());
                settleCheckAccount.setFinalAmount(finalAmount);
                settleCheckAccount.setCheckTime(DateToolUtils.addDay(time, -1));
                settleCheckAccount.setCreateTime(new Date());
            }
        }
        return settleCheckAccountMapper.insertList(settleCheckAccounts);
    }


    /**
     * 分页查询交易对账总表信息
     *
     * @param tradeCheckAccountDTO 交易对账输入实体
     * @return 交易对账实体集合
     */
    @Override
    public PageInfo<TradeCheckAccountDTO> pageTradeCheckAccount(TradeCheckAccountDTO tradeCheckAccountDTO) {
        return new PageInfo<>(tradeCheckAccountMapper.pageTradeCheckAccount(tradeCheckAccountDTO));
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/4/16
     * @Descripate 分页查询机构结算对账
     **/
    @Override
    public PageInfo<SettleCheckAccount> pageSettleAccountCheck(TradeCheckAccountDTO tradeCheckAccountDTO) {
        return new PageInfo<SettleCheckAccount>(settleCheckAccountMapper.pageSettleAccountCheck(tradeCheckAccountDTO));
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/4/16
     * @Descripate 分页查询机构结算对账详情
     **/
    @Override
    public PageInfo<SettleCheckAccountDetail> pageSettleAccountCheckDetail(TradeCheckAccountDTO tradeCheckAccountDTO) {
        return new PageInfo<SettleCheckAccountDetail>(settleCheckAccountDetailMapper.pageSettleAccountCheckDetail(tradeCheckAccountDTO));
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/4/16
     * @Descripate 导出机构结算对账
     **/
    @Override
    public Map<String, Object> exportSettleAccountCheck(TradeCheckAccountSettleExportDTO tradeCheckAccountDTO) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("Statement", settleCheckAccountMapper.exportSettleAccountCheck(tradeCheckAccountDTO));
        List<ExportSettleCheckAccountVO> list = settleCheckAccountDetailMapper.exportSettleAccountCheckDetail(tradeCheckAccountDTO);
        for (ExportSettleCheckAccountVO e : list) {
            map.put(e.getCurrency(), e.getList());
        }

        for (String key : map.keySet()) {
            double balance = 0;
            if (!StringUtils.isEmpty(tradeCheckAccountDTO.getStartDate())) {
                Date date = DateToolUtils.getDateByStr(tradeCheckAccountDTO.getStartDate());
                BigDecimal bigDecimal = settleCheckAccountMapper.getBalanceByTimeAndCurrencyAndInstitutionCode(date, key, tradeCheckAccountDTO.getInstitutionCode());
                balance = bigDecimal == null ? 0 : bigDecimal.doubleValue();
            }
            double afterBalance = 0;
            if (!key.equals("Statement")) {
                List<SettleCheckAccountDetailVO> list1 = (List<SettleCheckAccountDetailVO>) map.get(key);
                for (int i = list1.size() - 1; i >= 0; i--) {
                    SettleCheckAccountDetailVO settleCheckAccountDetail = list1.get(i);
                    afterBalance = balance + settleCheckAccountDetail.getTxnamount().doubleValue() - settleCheckAccountDetail.getFee().doubleValue();
                    settleCheckAccountDetail.setBalance(balance);
                    settleCheckAccountDetail.setAfterBalance(afterBalance);
                    balance = afterBalance;
                }
            }
        }

        return map;
    }

    /**
     * 导出交易对账总表信息
     *
     * @param tradeCheckAccountDTO
     * @return
     */
    @Override
    public ExportTradeAccountVO exportTradeCheckAccount(TradeCheckAccountExportDTO tradeCheckAccountDTO) {
        //获取当前请求语言
        tradeCheckAccountDTO.setLanguage(auditorProvider.getLanguage());
        //时间为空,默认为昨天
        if (StringUtils.isEmpty(tradeCheckAccountDTO.getStartDate()) && StringUtils.isEmpty(tradeCheckAccountDTO.getEndDate())) {
            String yesterday = DateToolUtils.getYesterday();//昨日日期
            tradeCheckAccountDTO.setStartDate(yesterday);
            tradeCheckAccountDTO.setEndDate(yesterday);
        }
        //总表信息
        List<TradeCheckAccount> tradeCheckAccounts = tradeCheckAccountMapper.exportTradeCheckAccount(tradeCheckAccountDTO);
        //详细表信息
        List<TradeAccountDetailVO> accountDetails = tradeCheckAccountDetailMapper.exportTradeCheckAccount(tradeCheckAccountDTO);
        ExportTradeAccountVO exportTradeAccountVO = new ExportTradeAccountVO();
        exportTradeAccountVO.setTradeCheckAccounts(tradeCheckAccounts);
        exportTradeAccountVO.setTradeAccountDetailVOS(accountDetails);
        return exportTradeAccountVO;
    }
}
