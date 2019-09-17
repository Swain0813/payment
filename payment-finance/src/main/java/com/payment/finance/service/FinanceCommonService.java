package com.payment.finance.service;

import com.payment.common.entity.Institution;

/**
 * 对账用的共通模块
 */
public interface FinanceCommonService {
    /**
     * 从缓存里获取机构信息
     * @param institutionCode
     * @return
     */
    Institution getInstitutionInfo(String institutionCode);
}
