package com.payment.finance.service.impl;

import com.alibaba.fastjson.JSON;
import com.payment.common.constant.AD3MQConstant;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.constant.TradeConstant;
import com.payment.common.dto.SearchAccountCheckDTO;
import com.payment.common.entity.OrderRefund;
import com.payment.common.entity.Orders;
import com.payment.common.entity.RabbitMassage;
import com.payment.common.exception.BusinessException;
import com.payment.common.redis.RedisService;
import com.payment.common.response.EResultEnum;
import com.payment.common.utils.DateToolUtils;
import com.payment.common.utils.IDS;
import com.payment.finance.constant.FinaceConstant;
import com.payment.finance.dao.*;
import com.payment.finance.dto.AD3CheckAccountDTO;
import com.payment.finance.entity.CheckAccount;
import com.payment.finance.entity.CheckAccountAudit;
import com.payment.finance.entity.CheckAccountAuditHistory;
import com.payment.finance.entity.CheckAccountLog;
import com.payment.finance.rabbitmq.RabbitMQSender;
import com.payment.finance.service.AccountCheckService;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @description: 对账业务接口实现类
 * @author: XuWenQi
 * @create: 2019-03-26 9:55
 **/
@Slf4j
@Service
@Transactional
public class AccountCheckServiceImpl implements AccountCheckService {

    @Value("${custom.merchantCode}")
    private String merchantCode;//商户编号

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderRefundMapper orderRefundMapper;

    @Autowired
    private CheckAccountMapper checkAccountMapper;

    @Autowired
    private CheckAccountLogMapper checkAccountLogMapper;

    @Autowired
    private CheckAccountAuditMapper checkAccountAuditMapper;

    @Autowired
    private CheckAccountAuditHistoryMapper checkAccountAuditHistoryMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private RabbitMQSender rabbitMQSender;


    /**
     * ad3通道对账
     *
     * @param searchAccountCheckDTO 分页查询对账管理
     * @return
     */
    @Override
    public PageInfo<CheckAccountLog> pageAccountCheckLog(SearchAccountCheckDTO searchAccountCheckDTO) {
        return new PageInfo<CheckAccountLog>(checkAccountLogMapper.pageAccountCheckLog(searchAccountCheckDTO));
    }

    /**
     * ad3通道对账
     *
     * @param searchAccountCheckDTO 分页查询对账管理详情
     * @return
     */
    @Override
    public PageInfo<CheckAccount> pageAccountCheck(SearchAccountCheckDTO searchAccountCheckDTO) {
        return new PageInfo<CheckAccount>(checkAccountMapper.pageAccountCheck(searchAccountCheckDTO));
    }


    /**
     * ad3通道对账
     *
     * @param searchAccountCheckDTO 分页查询对账管理复核详情
     * @return
     */
    @Override
    public PageInfo<CheckAccountAudit> pageAccountCheckAudit(SearchAccountCheckDTO searchAccountCheckDTO) {
        return new PageInfo<CheckAccountAudit>(checkAccountAuditMapper.pageAccountCheckAudit(searchAccountCheckDTO));
    }

    /**
     * ad3通道对账
     *
     * @param searchAccountCheckDTO 导出对账管理复核详情
     * @return
     */
    @Override
    public List<CheckAccountAudit> exportAccountCheckAudit(SearchAccountCheckDTO searchAccountCheckDTO) {
        return checkAccountAuditMapper.exportAccountCheckAudit(searchAccountCheckDTO);
    }

    /**
     * ad3通道对账
     *
     * @param searchAccountCheckDTO 导出对账管理详情
     * @return
     */
    @Override
    public List<CheckAccount> exportAccountCheck(SearchAccountCheckDTO searchAccountCheckDTO) {
        return checkAccountMapper.exportAccountCheck(searchAccountCheckDTO);
    }

    /**
     * 差错处理
     *
     * @param
     * @return
     */
    @Override
    public int updateCheckAccount(String checkAccountId, String remark) {
        if (checkAccountAuditMapper.selectByPrimaryKey(checkAccountId) != null) {
            throw new BusinessException(EResultEnum.AUDIT_INFO_EXIENT.getCode());
        }
        CheckAccount checkAccount = checkAccountMapper.selectByPrimaryKey(checkAccountId);
        CheckAccountAudit checkAccountAudit = new CheckAccountAudit();
        BeanUtils.copyProperties(checkAccount, checkAccountAudit);
        checkAccountAudit.setAuditStatus(FinaceConstant.AUDIT_WAIT);
        checkAccountAudit.setErrorType(FinaceConstant.FINACE_SUCCESS);
        checkAccountAudit.setRemark1(remark);
        return checkAccountAuditMapper.insert(checkAccountAudit);


    }

    /**
     * 差错复核
     *
     * @param
     * @return
     */
    @Override
    public int auditCheckAccount(String checkAccountId, Boolean enable, String remark) {
        CheckAccountAudit ck = checkAccountAuditMapper.selectByPrimaryKey(checkAccountId);
        if (enable) {
            if (ck.getErrorType() == FinaceConstant.FINACE_CACUO) { //差错处理
                CheckAccount checkAccount = new CheckAccount();
                checkAccount.setId(checkAccountId);
                checkAccount.setErrorType(FinaceConstant.FINACE_SUCCESS);
                checkAccount.setRemark1(ck.getRemark1());
                checkAccountAuditMapper.deleteByPrimaryKey(checkAccountId);
                return checkAccountMapper.updateByPrimaryKeySelective(checkAccount);
            } else {//补单
                CheckAccount checkAccount = new CheckAccount();
                if (ck.getTradeType() == 1) {
                    ordersMapper.supplementStatus(ck.getUOrderId(), TradeConstant.ORDER_PAY_SUCCESS, "补单成功");
                    checkAccount.setUStatus(TradeConstant.ORDER_PAY_SUCCESS);
                } else {
                    Orders orders = ordersMapper.selectByPrimaryKey(ck.getUOrderId());
                    if (orders == null) {//订单不存在的场合的判断
                        throw new BusinessException(EResultEnum.ORDER_NOT_EXIST.getCode());
                    }
                    if (TradeConstant.ORDER_CANNELING.equals(orders.getTradeStatus()) || TradeConstant.ORDER_CANNEL_FALID.equals(orders.getTradeStatus())) {
                        ordersMapper.supplementStatus(ck.getUOrderId(), TradeConstant.ORDER_CANNEL_SUCCESS, "补单成功");
                    }
                    orderRefundMapper.supplementStatus(ck.getUOrderId(), TradeConstant.ORDER_PAY_SUCCESS, "补单成功");
                    checkAccount.setUStatus(TradeConstant.ORDER_PAY_SUCCESS);
                }
                checkAccount.setId(checkAccountId);
                checkAccount.setErrorType(FinaceConstant.FINACE_SUCCESS);
                checkAccount.setRemark1(ck.getRemark1());
                checkAccountAuditMapper.deleteByPrimaryKey(checkAccountId);
                return checkAccountMapper.updateByPrimaryKeySelective(checkAccount);
            }
        } else {
            CheckAccount checkAccount = new CheckAccount();
            checkAccount.setId(checkAccountId);
            checkAccount.setRemark2(remark);
            checkAccountMapper.updateByPrimaryKeySelective(checkAccount);

            ck.setAuditStatus(FinaceConstant.AUDIT_FAIL);
            ck.setRemark2(remark);
            CheckAccountAuditHistory checkAccountAuditHistory = new CheckAccountAuditHistory();
            BeanUtils.copyProperties(ck, checkAccountAuditHistory);
            checkAccountAuditHistory.setId(IDS.uuid2());
            checkAccountAuditHistory.setCId(ck.getId());
            checkAccountAuditHistoryMapper.insert(checkAccountAuditHistory);
            return checkAccountAuditMapper.deleteByPrimaryKey(checkAccountId);
        }

    }

    /**
     * ad3通道对账
     *
     * @param file 上传文件
     * @return
     */
    @Override
    public Object ad3ChannelAccountCheck(MultipartFile file) {
        //获取文件名
        String fileName = file.getOriginalFilename();
        String[] name = fileName.split("\\.")[0].split("_");

        //校验文件是否上传
        if (StringUtils.isNotEmpty(redisService.get(fileName))) {
            throw new BusinessException(EResultEnum.FILE_EXIST.getCode());
        } else {
            redisService.set(fileName, fileName, 24 * 60 * 60);
        }

        //校验文件格式
        if (StringUtils.isEmpty(fileName) || !fileName.toLowerCase().matches("^.+\\.(?i)(txt)$")) {
            throw new BusinessException(EResultEnum.FILE_FORMAT_ERROR.getCode());
        }
        //校验文件名称 (AD3_20190404.txt)
        if (!FinaceConstant.FinaceChannelNameMap.containsKey(name[0])) {
            throw new BusinessException(EResultEnum.NAME_ERROR.getCode());
        }
        try {
            //获取文件输入流
            InputStream inputStream = file.getInputStream();
            //字符缓冲流
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "GBK"));
            //文件内容Map
            Map<String, AD3CheckAccountDTO> ad3SDMap = new HashMap<>();//收单订单
            Map<String, AD3CheckAccountDTO> ad3TKMap = new HashMap<>();//退款订单
            int num = 0;//订单条数
            BigDecimal sdAmount = BigDecimal.ZERO;//收单金额
            BigDecimal tkAmount = BigDecimal.ZERO;//退款金额
            BigDecimal sdFee = BigDecimal.ZERO;//收单手续费
            String str;
            while ((str = br.readLine()) != null) {
                if (num == 0) {
                    //校验文件内容
                    if (!str.contains("系统流水号") || !str.contains("交易币种") || !str.contains("交易金额")) {
                        //文件内容错误
                        throw new BusinessException(EResultEnum.FILE_CONTENT_ERROR.getCode());
                    }
                } else {
                    if (!StringUtils.isEmpty(str)) {
                        String[] s = str.split("\\,");
                        if (s[0].equals(merchantCode)) {
                            //判断内容是否重复
                            if (StringUtils.isNotEmpty(redisService.get(s[3] + "_" + DateToolUtils.getReqDateE()))) {
                                throw new BusinessException(EResultEnum.FILE_EXIST.getCode(), s[3]);
                            }
                            if (s[1].equals("收单")) {
                                AD3CheckAccountDTO ad3CheckAccountDTO = new AD3CheckAccountDTO(s);
                                ad3SDMap.put(s[3], ad3CheckAccountDTO);
                                sdAmount = sdAmount.add(BigDecimal.valueOf(Double.parseDouble(s[5].trim())));
                                sdFee = sdFee.add(BigDecimal.valueOf(Double.parseDouble(s[6].trim())));
                            } else if (s[1].equals("退款")) {
                                AD3CheckAccountDTO ad3CheckAccountDTO = new AD3CheckAccountDTO(s);
                                ad3TKMap.put(s[3], ad3CheckAccountDTO);
                                tkAmount = tkAmount.add(BigDecimal.valueOf(Double.parseDouble(s[5].trim())));
                            }

                        } else {
                            log.info("----------------商户对账单商户编号异常记录-----------------订单【{}】", str);
                        }
                    }
                }
                num++;
            }
            //check账单
            this.docheck(ad3SDMap, ad3TKMap, name);
        } catch (IOException e) {
            log.error("***********************ad3通道对账发生异常*******************", e);
        }
        return null;
    }

    public static void main(String[] args) {
        BigDecimal bigDecimal = new BigDecimal("11");
        BigDecimal add = bigDecimal.add(new BigDecimal("2"));
        System.out.println(bigDecimal);
    }

    @Async
    public void docheck(Map<String, AD3CheckAccountDTO> ad3SDMap, Map<String, AD3CheckAccountDTO> ad3TKMap, String[] name) {

        //获取昨天起始时间
        Date startTime = DateToolUtils.getDayStart(DateToolUtils.addDay(DateToolUtils.StringToDate(name[1]), -1));
        //获取昨天结束时间
        Date endTime = DateToolUtils.getDayEnd(DateToolUtils.addDay(DateToolUtils.StringToDate(name[1]), -1));

        //文件对应的通道编号
        List<String> list = FinaceConstant.FinaceChannelNameMap.get(name[0]);

        List<String> tkList = Lists.newArrayList();//退款补单队列
        List<String> sdList = Lists.newArrayList();//收单补单队列


        List<CheckAccount> checkAccountList = checkAccountMapper.getDataByType(FinaceConstant.FINACE_WAIT, startTime, endTime);//获取前一天未对账状态的对账单数据
        List<Orders> ordersList = ordersMapper.getYesterDayDate(startTime, endTime, list);
        List<OrderRefund> orderRefundList = orderRefundMapper.getYesterDayDate(startTime, endTime, list);

        int cCount = ad3SDMap.size() + ad3TKMap.size();
        int sCount = ordersList.size() + orderRefundList.size();

        BigDecimal uTotalAmount = BigDecimal.ZERO;//平台交易总金额
        BigDecimal cTotalAmount = BigDecimal.ZERO;//通道交易总金额
        BigDecimal uTotalFee = BigDecimal.ZERO;//平台交易手续费
        BigDecimal cTotalFee = BigDecimal.ZERO;//通道交易手续费
        /****************************************************   校验收单   ************************************************************/
        List<CheckAccount> calist = Lists.newArrayList();
        for (Orders orders : ordersList) {

            if (!TradeConstant.ORDER_WAIT_PAY.equals(orders.getTradeStatus())) {
                uTotalAmount = uTotalAmount.add(orders.getTradeAmount() == null ? BigDecimal.ZERO : orders.getTradeAmount());//累计平台交易金额
                uTotalFee = uTotalFee.add(orders.getChannelFee() == null ? BigDecimal.ZERO : orders.getChannelFee());//累计平台通道手续费
            }
            if (ad3SDMap.containsKey(orders.getId())) {
                redisService.set(orders.getId() + "_" + DateToolUtils.getReqDateE(), orders.getId(), 24 * 60 * 60);
                cTotalAmount = cTotalAmount.add(ad3SDMap.get(orders.getId()).getTradeAmount());//累计上游交易金额
                cTotalFee = cTotalFee.add(ad3SDMap.get(orders.getId()).getChannelRate());//累计上游通道手续费

                CheckAccount checkAccount = new CheckAccount(orders, ad3SDMap.get(orders.getId()));
                checkAccount.setCreateTime(new Date());
                checkAccount.setId(IDS.uuid2());
                checkAccount.setChannelCode(orders.getChannelCode());
                //币种不等
                if (!orders.getTradeCurrency().equals(ad3SDMap.get(orders.getId()).getTradeCurrency())) {
                    checkAccount.setErrorType(FinaceConstant.FINACE_CACUO);
                    checkAccount.setRemark("币种不等");
                    calist.add(checkAccount);
                    ad3SDMap.remove(orders.getId());//移除通道map数据
                    continue;
                }
                //金额不等
                if (orders.getTradeAmount().compareTo(ad3SDMap.get(orders.getId()).getTradeAmount()) != 0) {
                    checkAccount.setErrorType(FinaceConstant.FINACE_CACUO);
                    checkAccount.setRemark("交易金额不等");
                    calist.add(checkAccount);
                    ad3SDMap.remove(orders.getId());//移除通道map数据
                    continue;
                }
                //手续费金额不等
                BigDecimal oChanFee = orders.getChannelFee() == null ? BigDecimal.ZERO : orders.getChannelFee();
                if (oChanFee.compareTo(ad3SDMap.get(orders.getId()).getChannelRate()) != 0) {
                    checkAccount.setErrorType(FinaceConstant.FINACE_CACUO);
                    checkAccount.setRemark("手续费金额不等");
                    calist.add(checkAccount);
                    ad3SDMap.remove(orders.getId());//移除通道map数据
                    continue;
                }
                if (!TradeConstant.ORDER_PAY_SUCCESS.equals(orders.getTradeStatus())) {
                    checkAccount.setErrorType(FinaceConstant.FINACE_BUDAN);
                    checkAccount.setRemark("订单状态异常");
                    calist.add(checkAccount);
                    ad3SDMap.remove(orders.getId());//移除通道map数据
                    sdList.add(orders.getId());//添加到补单队列
                    continue;
                }
                checkAccount.setErrorType(FinaceConstant.FINACE_SUCCESS);
                calist.add(checkAccount);
            } else {
                //平台有数据，通道无数据
                if (!TradeConstant.ORDER_PAYING.equals(orders.getTradeStatus()) && !TradeConstant.ORDER_CANNEL_SUCCESS.equals(orders.getCancelStatus())) {
                    CheckAccount checkAccount = new CheckAccount(orders);
                    checkAccount.setCreateTime(new Date());
                    checkAccount.setId(IDS.uuid2());
                    checkAccount.setErrorType(FinaceConstant.FINACE_WAIT);
                    calist.add(checkAccount);
                }
            }
            ad3SDMap.remove(orders.getId());//移除通道map数据

        }

        /****************************************************   校验退款单   ************************************************************/
        for (OrderRefund orders : orderRefundList) {

            //if (orders.getRefundStatus() == TradeConstant.REFUND_SUCCESS) {
            uTotalAmount = uTotalAmount.subtract(orders.getTradeAmount() == null ? BigDecimal.ZERO : orders.getTradeAmount());//累计平台交易金额
            uTotalFee = uTotalFee.subtract(orders.getChannelFee() == null ? BigDecimal.ZERO : orders.getChannelFee());//累计平台通道手续费
            //}
            if (ad3TKMap.containsKey(orders.getOrderId())) {
                redisService.set(orders.getOrderId() + "_" + DateToolUtils.getReqDateE(), orders.getOrderId(), 24 * 60 * 60);
                cTotalAmount = cTotalAmount.subtract(ad3TKMap.get(orders.getOrderId()).getTradeAmount());//累计上游交易金额
                cTotalFee = cTotalFee.subtract(ad3TKMap.get(orders.getOrderId()).getChannelRate());//累计上游通道手续费

                CheckAccount checkAccount = new CheckAccount(orders, ad3TKMap.get(orders.getOrderId()));
                checkAccount.setId(IDS.uuid2());
                checkAccount.setChannelCode(orders.getChannelCode());
                checkAccount.setCreateTime(new Date());
                //币种不等
                if (!orders.getTradeCurrency().equals(ad3TKMap.get(orders.getOrderId()).getTradeCurrency())) {
                    checkAccount.setErrorType(FinaceConstant.FINACE_CACUO);
                    checkAccount.setRemark("币种不等");
                    calist.add(checkAccount);
                    ad3TKMap.remove(orders.getOrderId());//移除通道map数据
                    continue;
                }
                //金额不等
                if (orders.getTradeAmount().compareTo(ad3TKMap.get(orders.getOrderId()).getTradeAmount()) != 0) {
                    checkAccount.setErrorType(FinaceConstant.FINACE_CACUO);
                    checkAccount.setRemark("交易金额不等");
                    calist.add(checkAccount);
                    ad3TKMap.remove(orders.getOrderId());//移除通道map数据
                    continue;
                }
                //手续费金额不等
                BigDecimal oChanFee = orders.getChannelFee() == null ? BigDecimal.ZERO : orders.getChannelFee();
                if (oChanFee.compareTo(ad3TKMap.get(orders.getOrderId()).getChannelRate()) != 0) {
                    checkAccount.setErrorType(FinaceConstant.FINACE_CACUO);
                    checkAccount.setRemark("手续费金额不等");
                    calist.add(checkAccount);
                    ad3TKMap.remove(orders.getOrderId());//移除通道map数据
                    continue;
                }
                //订单状态 (退款成功)
                if (!TradeConstant.REFUND_SUCCESS.equals(orders.getRefundStatus())) {
                    checkAccount.setErrorType(FinaceConstant.FINACE_BUDAN);
                    checkAccount.setRemark("订单状态异常");
                    calist.add(checkAccount);
                    ad3TKMap.remove(orders.getOrderId());//移除通道map数据
                    tkList.add(orders.getOrderId());//添加到补单队列
                    continue;
                }
                checkAccount.setErrorType(FinaceConstant.FINACE_SUCCESS);
                calist.add(checkAccount);
            } else {
                //平台有数据，通道无数据
                CheckAccount checkAccount = new CheckAccount(orders);
                checkAccount.setCreateTime(new Date());
                checkAccount.setId(IDS.uuid2());
                checkAccount.setErrorType(FinaceConstant.FINACE_WAIT);
                calist.add(checkAccount);
            }
            ad3TKMap.remove(orders.getOrderId());//移除通道map数据

        }

        /****************************************************    校验前一天对账记录 - 平台无数据，通道有数据   ******************************************/
        for (CheckAccount checkAccount : checkAccountList) {
            if (ad3SDMap.containsKey(checkAccount.getUOrderId())) { //未结算订单
                redisService.set(checkAccount.getUOrderId() + "_" + DateToolUtils.getReqDateE(), checkAccount.getUOrderId(), 24 * 60 * 60);
                cTotalAmount = cTotalAmount.add(ad3SDMap.get(checkAccount.getUOrderId()).getTradeAmount());//累计上游交易金额
                cTotalFee = cTotalFee.add(ad3SDMap.get(checkAccount.getUOrderId()).getChannelRate());//累计上游通道手续费
                //币种不等
                if (!checkAccount.getUChannelNumber().equals(ad3SDMap.get(checkAccount.getUOrderId()).getTradeCurrency())) {
                    checkAccount.setErrorType(FinaceConstant.FINACE_CACUO);
                    checkAccount.setRemark("币种不等");
                    checkAccount.setCChannelNumber(ad3SDMap.get(checkAccount.getUOrderId()).getChannelNumber());//通道业务流水号
                    checkAccount.setCFee(ad3SDMap.get(checkAccount.getUOrderId()).getChannelRate());//通道手续费
                    checkAccount.setCOrderId(ad3SDMap.get(checkAccount.getUOrderId()).getOrderId());//通道商户流水号
                    checkAccount.setCTradeAmount(ad3SDMap.get(checkAccount.getUOrderId()).getTradeAmount());//通道交易金额
                    checkAccount.setCTradeCurrency(ad3SDMap.get(checkAccount.getUOrderId()).getTradeCurrency());//通道交易币种
                    ad3SDMap.remove(checkAccount.getUOrderId());//移除通道map数据
                    checkAccountMapper.updateByPrimaryKeySelective(checkAccount);
                    continue;
                }
                //金额不等
                if (checkAccount.getUTradeAmount().compareTo(ad3SDMap.get(checkAccount.getUOrderId()).getTradeAmount()) != 0) {
                    checkAccount.setErrorType(FinaceConstant.FINACE_CACUO);
                    checkAccount.setRemark("交易金额不等");
                    checkAccount.setCChannelNumber(ad3SDMap.get(checkAccount.getUOrderId()).getChannelNumber());//通道业务流水号
                    checkAccount.setCFee(ad3SDMap.get(checkAccount.getUOrderId()).getChannelRate());//通道手续费
                    checkAccount.setCOrderId(ad3SDMap.get(checkAccount.getUOrderId()).getOrderId());//通道商户流水号
                    checkAccount.setCTradeAmount(ad3SDMap.get(checkAccount.getUOrderId()).getTradeAmount());//通道交易金额
                    checkAccount.setCTradeCurrency(ad3SDMap.get(checkAccount.getUOrderId()).getTradeCurrency());//通道交易币种
                    ad3SDMap.remove(checkAccount.getUOrderId());//移除通道map数据
                    checkAccountMapper.updateByPrimaryKeySelective(checkAccount);
                    continue;
                }
                //手续费金额不等
                if (checkAccount.getUFee().compareTo(ad3SDMap.get(checkAccount.getUOrderId()).getChannelRate()) != 0) {
                    checkAccount.setErrorType(FinaceConstant.FINACE_CACUO);
                    checkAccount.setRemark("手续费金额不等");
                    checkAccount.setCChannelNumber(ad3SDMap.get(checkAccount.getUOrderId()).getChannelNumber());//通道业务流水号
                    checkAccount.setCFee(ad3SDMap.get(checkAccount.getUOrderId()).getChannelRate());//通道手续费
                    checkAccount.setCOrderId(ad3SDMap.get(checkAccount.getUOrderId()).getOrderId());//通道商户流水号
                    checkAccount.setCTradeAmount(ad3SDMap.get(checkAccount.getUOrderId()).getTradeAmount());//通道交易金额
                    checkAccount.setCTradeCurrency(ad3SDMap.get(checkAccount.getUOrderId()).getTradeCurrency());//通道交易币种
                    ad3SDMap.remove(checkAccount.getUOrderId());//移除通道map数据
                    checkAccountMapper.updateByPrimaryKeySelective(checkAccount);
                    continue;
                }
                //订单状态 (付款成功，退款，撤销中，撤销失败，撤销成功)
                if (!TradeConstant.ORDER_PAY_SUCCESS.equals(checkAccount.getUStatus())) {
                    checkAccount.setErrorType(FinaceConstant.FINACE_BUDAN);
                    checkAccount.setRemark("订单状态异常");
                    checkAccount.setCChannelNumber(ad3SDMap.get(checkAccount.getUOrderId()).getChannelNumber());//通道业务流水号
                    checkAccount.setCFee(ad3SDMap.get(checkAccount.getUOrderId()).getChannelRate());//通道手续费
                    checkAccount.setCOrderId(ad3SDMap.get(checkAccount.getUOrderId()).getOrderId());//通道商户流水号
                    checkAccount.setCTradeAmount(ad3SDMap.get(checkAccount.getUOrderId()).getTradeAmount());//通道交易金额
                    checkAccount.setCTradeCurrency(ad3SDMap.get(checkAccount.getUOrderId()).getTradeCurrency());//通道交易币种
                    ad3SDMap.remove(checkAccount.getUOrderId());//移除通道map数据
                    checkAccountMapper.updateByPrimaryKeySelective(checkAccount);
                    sdList.add(checkAccount.getUOrderId());//添加到补单队列
                    continue;
                }
                checkAccount.setErrorType(FinaceConstant.FINACE_SUCCESS);
                checkAccount.setCChannelNumber(ad3SDMap.get(checkAccount.getUOrderId()).getChannelNumber());//通道业务流水号
                checkAccount.setCFee(ad3SDMap.get(checkAccount.getUOrderId()).getChannelRate());//通道手续费
                checkAccount.setCOrderId(ad3SDMap.get(checkAccount.getUOrderId()).getOrderId());//通道商户流水号
                checkAccount.setCTradeAmount(ad3SDMap.get(checkAccount.getUOrderId()).getTradeAmount());//通道交易金额
                checkAccount.setCTradeCurrency(ad3SDMap.get(checkAccount.getUOrderId()).getTradeCurrency());//通道交易币种
                ad3SDMap.remove(checkAccount.getUOrderId());//移除通道map数据
                checkAccountMapper.updateByPrimaryKeySelective(checkAccount);

            } else if (ad3TKMap.containsKey(checkAccount.getUOrderId())) {//未结算退款单
                redisService.set(checkAccount.getUOrderId() + "_" + DateToolUtils.getReqDateE(), checkAccount.getUOrderId(), 24 * 60 * 60);
                cTotalAmount = cTotalAmount.subtract(ad3SDMap.get(checkAccount.getUOrderId()).getTradeAmount());//累计上游交易金额
                cTotalFee = cTotalFee.subtract(ad3SDMap.get(checkAccount.getUOrderId()).getChannelRate());//累计上游通道手续费
                //币种不等
                if (!checkAccount.getUChannelNumber().equals(ad3TKMap.get(checkAccount.getUOrderId()).getTradeCurrency())) {
                    checkAccount.setErrorType(FinaceConstant.FINACE_CACUO);
                    checkAccount.setRemark("币种不等");
                    checkAccount.setCChannelNumber(ad3TKMap.get(checkAccount.getUOrderId()).getChannelNumber());//通道业务流水号
                    checkAccount.setCFee(ad3TKMap.get(checkAccount.getUOrderId()).getChannelRate());//通道手续费
                    checkAccount.setCOrderId(ad3TKMap.get(checkAccount.getUOrderId()).getOrderId());//通道商户流水号
                    checkAccount.setCTradeAmount(ad3TKMap.get(checkAccount.getUOrderId()).getTradeAmount());//通道交易金额
                    checkAccount.setCTradeCurrency(ad3TKMap.get(checkAccount.getUOrderId()).getTradeCurrency());//通道交易币种
                    ad3TKMap.remove(checkAccount.getUOrderId());//移除通道map数据
                    checkAccountMapper.updateByPrimaryKeySelective(checkAccount);
                    continue;
                }
                //金额不等
                if (checkAccount.getUTradeAmount().compareTo(ad3TKMap.get(checkAccount.getUOrderId()).getTradeAmount()) != 0) {
                    checkAccount.setErrorType(FinaceConstant.FINACE_CACUO);
                    checkAccount.setRemark("交易金额不等");
                    checkAccount.setCChannelNumber(ad3TKMap.get(checkAccount.getUOrderId()).getChannelNumber());//通道业务流水号
                    checkAccount.setCFee(ad3TKMap.get(checkAccount.getUOrderId()).getChannelRate());//通道手续费
                    checkAccount.setCOrderId(ad3TKMap.get(checkAccount.getUOrderId()).getOrderId());//通道商户流水号
                    checkAccount.setCTradeAmount(ad3TKMap.get(checkAccount.getUOrderId()).getTradeAmount());//通道交易金额
                    checkAccount.setCTradeCurrency(ad3TKMap.get(checkAccount.getUOrderId()).getTradeCurrency());//通道交易币种
                    ad3TKMap.remove(checkAccount.getUOrderId());//移除通道map数据
                    checkAccountMapper.updateByPrimaryKeySelective(checkAccount);
                    continue;
                }
                //手续费金额不等
                if (checkAccount.getUFee().compareTo(ad3TKMap.get(checkAccount.getUOrderId()).getChannelRate()) != 0) {
                    checkAccount.setErrorType(FinaceConstant.FINACE_CACUO);
                    checkAccount.setRemark("手续费金额不等");
                    checkAccount.setCChannelNumber(ad3TKMap.get(checkAccount.getUOrderId()).getChannelNumber());//通道业务流水号
                    checkAccount.setCFee(ad3TKMap.get(checkAccount.getUOrderId()).getChannelRate());//通道手续费
                    checkAccount.setCOrderId(ad3TKMap.get(checkAccount.getUOrderId()).getOrderId());//通道商户流水号
                    checkAccount.setCTradeAmount(ad3TKMap.get(checkAccount.getUOrderId()).getTradeAmount());//通道交易金额
                    checkAccount.setCTradeCurrency(ad3TKMap.get(checkAccount.getUOrderId()).getTradeCurrency());//通道交易币种
                    ad3TKMap.remove(checkAccount.getUOrderId());//移除通道map数据
                    checkAccountMapper.updateByPrimaryKeySelective(checkAccount);
                    continue;
                }
                //订单状态 (退款成功)
                if (!TradeConstant.REFUND_SUCCESS.equals(checkAccount.getUStatus())) {
                    checkAccount.setErrorType(FinaceConstant.FINACE_BUDAN);
                    checkAccount.setRemark("订单状态异常");
                    checkAccount.setCChannelNumber(ad3TKMap.get(checkAccount.getUOrderId()).getChannelNumber());//通道业务流水号
                    checkAccount.setCFee(ad3TKMap.get(checkAccount.getUOrderId()).getChannelRate());//通道手续费
                    checkAccount.setCOrderId(ad3TKMap.get(checkAccount.getUOrderId()).getOrderId());//通道商户流水号
                    checkAccount.setCTradeAmount(ad3TKMap.get(checkAccount.getUOrderId()).getTradeAmount());//通道交易金额
                    checkAccount.setCTradeCurrency(ad3TKMap.get(checkAccount.getUOrderId()).getTradeCurrency());//通道交易币种
                    ad3TKMap.remove(checkAccount.getUOrderId());//移除通道map数据
                    checkAccountMapper.updateByPrimaryKeySelective(checkAccount);
                    tkList.add(checkAccount.getUOrderId());//添加到补单队列
                    continue;
                }
                checkAccount.setErrorType(FinaceConstant.FINACE_SUCCESS);
                checkAccount.setCChannelNumber(ad3TKMap.get(checkAccount.getUOrderId()).getChannelNumber());//通道业务流水号
                checkAccount.setCFee(ad3TKMap.get(checkAccount.getUOrderId()).getChannelRate());//通道手续费
                checkAccount.setCOrderId(ad3TKMap.get(checkAccount.getUOrderId()).getOrderId());//通道商户流水号
                checkAccount.setCTradeAmount(ad3TKMap.get(checkAccount.getUOrderId()).getTradeAmount());//通道交易金额
                checkAccount.setCTradeCurrency(ad3TKMap.get(checkAccount.getUOrderId()).getTradeCurrency());//通道交易币种
                ad3TKMap.remove(checkAccount.getUOrderId());//移除通道map数据
                checkAccountMapper.updateByPrimaryKeySelective(checkAccount);
            }
        }

        /******************************************     通道有数据，平台无数据      ***************************************/
        for (String key : ad3SDMap.keySet()) {
            redisService.set(key + "_" + DateToolUtils.getReqDateE(), key, 24 * 60 * 60);
            CheckAccount checkAccount = new CheckAccount(1, ad3SDMap.get(key));
            checkAccount.setCreateTime(new Date());
            checkAccount.setId(IDS.uuid2());
            checkAccount.setCStatus(TradeConstant.ORDER_PAY_SUCCESS);
            checkAccount.setErrorType(FinaceConstant.FINACE_CACUO);
            checkAccount.setRemark("通道有数据平台无数据");
            calist.add(checkAccount);
            cTotalAmount = cTotalAmount.add(ad3SDMap.get(key).getTradeAmount());//累计上游交易金额
            cTotalFee = cTotalFee.add(ad3SDMap.get(key).getChannelRate());//累计上游通道手续费
        }
        for (String key : ad3TKMap.keySet()) {
            redisService.set(key + "_" + DateToolUtils.getReqDateE(), key, 24 * 60 * 60);
            CheckAccount checkAccount = new CheckAccount(2, ad3TKMap.get(key));
            checkAccount.setCreateTime(new Date());
            checkAccount.setId(IDS.uuid2());
            checkAccount.setCStatus(TradeConstant.ORDER_PAY_SUCCESS);
            checkAccount.setErrorType(FinaceConstant.FINACE_CACUO);
            checkAccount.setRemark("通道有数据平台无数据");
            calist.add(checkAccount);
            cTotalAmount = cTotalAmount.subtract(ad3TKMap.get(key).getTradeAmount());//累计上游交易金额
            cTotalFee = cTotalFee.subtract(ad3TKMap.get(key).getChannelRate());//累计上游通道手续费
        }

        checkAccountMapper.insertList(calist);//保存详细记录
        /****************************************************   保存日志记录   ************************************************************/
        CheckAccountLog checkAccountLog = new CheckAccountLog();
        checkAccountLog.setCheckTime(DateToolUtils.addDay(new Date(), -1));
        checkAccountLog.setCreateTime(new Date());
        checkAccountLog.setChaTotalAmount(cTotalAmount);
        checkAccountLog.setChaTotalFee(cTotalFee);
        checkAccountLog.setChaTradeCount(cCount);
        checkAccountLog.setSysTradeAmount(uTotalAmount);
        checkAccountLog.setSysTradeFee(uTotalFee);
        checkAccountLog.setSysTradeCount(sCount);
        checkAccountLog.setErrorAmount(uTotalAmount.subtract(cTotalAmount));
        checkAccountLog.setErrorCount(checkAccountMapper.getErrorCount(new Date()));
        checkAccountLog.setCheckFileName(name[0]);
        checkAccountLogMapper.insert(checkAccountLog);

        doSupplement(tkList, sdList);//系统补单
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/4/10
     * @Descripate 系统补单队列
     **/
    public void doSupplement(List<String> tkList, List<String> sdList) {
        for (String s : tkList) {//退款补单队列
            log.info("------------------ 退款补单队列 -------------- 订单号 ： {}", s);
            rabbitMQSender.send(AD3MQConstant.TC_MQ_FINANCE_TKBUDAN_DL, s);
        }
        for (String s : sdList) {//收单补单队列
            RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, s);
            log.info("------------------ 收单补单队列 -------------- 订单号 ： {}", s);
            rabbitMQSender.send(AD3MQConstant.TC_MQ_FINANCE_SDBUDAN_DL, JSON.toJSONString(rabbitMassage));
        }

    }


}
