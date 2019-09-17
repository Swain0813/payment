package com.payment.clearing.service;

import com.payment.common.entity.ShareBenefitLogs;

import java.util.List;

public interface CalculateShareBenefitService {

    /**
     * 计算分润服务
     * @param agentCode
     * @param currency
     * @param slist
     */
    void calculateShareForMerchantGroup2(String agentCode, String currency, List<ShareBenefitLogs> slist);
}
