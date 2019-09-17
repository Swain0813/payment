package com.payment.finance.service;

import com.payment.common.dto.FinancialFreezeDTO;
import com.payment.common.dto.FundChangeDTO;
import com.payment.common.response.BaseResponse;

import java.util.Map;

/**
 * 清结算相关接口
 */
public interface ClearingService {

    /**
     * 资金变动接口
     * 场景支付成功后上报清结算系统
     *
     * @return
     */
    BaseResponse fundChange(FundChangeDTO fundChangeDTO, Map<String, Object> headerMap);


    /**
     * 资金冻结接口
     *
     * @return
     */
    BaseResponse freezingFunds(FinancialFreezeDTO financialFreezeDTO, Map<String, Object> headerMap);
}
