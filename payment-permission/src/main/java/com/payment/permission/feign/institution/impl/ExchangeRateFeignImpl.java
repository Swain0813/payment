package com.payment.permission.feign.institution.impl;

import com.payment.common.dto.ExchangeRateDTO;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.permission.feign.institution.ExchangeRateFeign;
import org.springframework.stereotype.Component;

@Component
public class ExchangeRateFeignImpl implements ExchangeRateFeign {

    @Override
    public BaseResponse addExchangeRate(ExchangeRateDTO exchangeRateDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse banExchangeRate(String id ) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }


    @Override
    public BaseResponse getByMultipleConditions(ExchangeRateDTO exchangeRateDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

}
