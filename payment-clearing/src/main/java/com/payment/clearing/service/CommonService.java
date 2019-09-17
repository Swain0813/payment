package com.payment.clearing.service;

import com.payment.common.entity.Institution;

/**
 * 共通方法
 */
public interface CommonService {
    /**
     * 获得机构信息从redis里获取
     *
     * @param institutionCode
     * @return
     */
    Institution getInstitutionInfo(String institutionCode);

}
