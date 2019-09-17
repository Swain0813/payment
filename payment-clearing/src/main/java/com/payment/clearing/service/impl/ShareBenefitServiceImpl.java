package com.payment.clearing.service.impl;

import com.payment.clearing.dao.ShareBenefitLogsMapper;
import com.payment.clearing.service.CalculateShareBenefitService;
import com.payment.clearing.service.ShareBenefitService;
import com.payment.common.entity.ShareBenefitLogs;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.EResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @description: 分润服务
 * @author: YangXu
 * @create: 2019-08-23 15:50
 **/
@Slf4j
@Service
public class ShareBenefitServiceImpl implements ShareBenefitService {

    @Autowired
    private ShareBenefitLogsMapper shareBenefitLogsMapper;

    @Autowired
    private CalculateShareBenefitService calculateShareBenefitService;


    /**
     * @return
     * @Author YangXu
     * @Date 2019/8/23
     * @Descripate 分润批次处理
     **/
    @Override
    public void ShareBenefitForBatch() {
        log.info("**************** ShareBenefitForBatch 批次分润 ************** #开始时间：{}", new Date());
        try {
            //查询已结算未分润的流水
            List<ShareBenefitLogs> list = shareBenefitLogsMapper.selectbyStStatusAndIsShare();
            if (list == null || list.size() == 0) {
                log.info("**************** ShareBenefitForBatch 批次分润 ************** 没有查询到符合可以结算的数据");
                return;
            }
            log.info("**************** ShareBenefitForBatch 批次分润 ************** 查询到符合可以结算的数据 【{}】 条", list.size());

            // 第二步按照商户id来封装
            TreeMap<String, List<ShareBenefitLogs>> mermap = new TreeMap<String, List<ShareBenefitLogs>>();
            for (ShareBenefitLogs shareBenefitLogs : list) {
                //要按照币种来分润
                String key = shareBenefitLogs.getAgentCode() + "_" + shareBenefitLogs.getTradeCurrency();
                if ((!mermap.containsKey(key)) && mermap.get(key) == null) {
                    List<ShareBenefitLogs> slist = new ArrayList<>();
                    slist.add(shareBenefitLogs);
                    mermap.put(key, slist);
                } else if (mermap.containsKey(key) && mermap.get(key) != null) {
                    List<ShareBenefitLogs> slist = mermap.get(key);
                    slist.add(shareBenefitLogs);
                    mermap.put(key, slist);
                }
            }
            // 第三步，按照代理商为单位进行处理。
            Set<String> ks = mermap.keySet();
            if (ks == null || ks.size() == 0) {
                log.info("**************** ShareBenefitForBatch 批次分润 ************** 以商户为单位排序无数据");
                return;
            }
            Iterator it = ks.iterator();
            while (it.hasNext()) {
                String key = (String) it.next();
                String[] keystr = key.split("_");
                String agentCode = keystr[0];
                String currency = keystr[1];
                if (key == null || key.equals("")) {
                    continue;
                }
                List<ShareBenefitLogs> slist = mermap.get(key);
                log.info("**************** ShareBenefitForBatch 批次分润 ************** #代理商户号为:【{}】 的待结算数据 【{}】 条", agentCode, slist.size());
                if (slist != null && slist.size() > 0) {
                    log.info("**************** ShareBenefitForBatch 批次分润 ************** 开始执行代理商户号为:【{}】，币种为：【{}】的待结算数据", agentCode, currency);
                    calculateShareBenefitService.calculateShareForMerchantGroup2(agentCode, currency, slist);//以商户+交易币种为组进行结算，以组提交事物
                    log.info("**************** ShareBenefitForBatch 批次分润 ************** 结束执行代理商户号为:【{}】，币种为：【{}】的待结算数据", agentCode, currency);
                }
            }
        } catch (Exception e) {
            log.error("**************** ShareBenefitForBatch 批次分润 ************** 异常：{}", e);
            throw new BusinessException(EResultEnum.ERROR.getCode());
        }


    }
}
