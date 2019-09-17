package com.payment.permission.feign.institution.impl;

import com.payment.common.dto.PaymentModeDTO;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.permission.feign.institution.PaymentModeFeign;
import org.springframework.stereotype.Component;

/**
 * @author shenxinran
 * @Date: 2019/3/3 18:41
 * @Description: 支付方式熔断类
 */
@Component
public class PaymentModeFeignImpl implements PaymentModeFeign {
    /**
     * 添加支付方式的不同语言
     *
     * @param paymentModeDTO
     * @return
     */
    @Override
    public BaseResponse addOtherLanguage(PaymentModeDTO paymentModeDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 添加支付方式
     *
     * @param paymentModeDTO
     * @return
     */
    @Override
    public BaseResponse addPayinfo(PaymentModeDTO paymentModeDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 查询支付方式
     *
     * @param paymentModeDTO
     * @return
     */
    @Override
    public BaseResponse pagePayInfo(PaymentModeDTO paymentModeDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }
    /**
     * 查询所有支付方式
     *
     * @param paymentModeDTO
     * @return
     */
    @Override
    public BaseResponse getPayInfo(PaymentModeDTO paymentModeDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 禁用支付方式
     *
     * @param paymentModeDTO
     * @return
     */
    @Override
    public BaseResponse banPayInfo(PaymentModeDTO paymentModeDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 更新支付方式
     *
     * @param paymentModeDTO
     * @return
     */
    @Override
    public BaseResponse updatePayInfo(PaymentModeDTO paymentModeDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }
}
