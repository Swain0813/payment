package com.payment.permission.feign.trade.impl;
import com.payment.common.dto.OrderPaymentDTO;
import com.payment.common.dto.OrderPaymentExportDTO;
import com.payment.common.dto.PayOutDTO;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.permission.feign.trade.OrdersPaymentFeign;
import org.springframework.stereotype.Component;

/**
 * @description:
 * @author: YangXu
 * @create: 2019-08-07 16:20
 **/
@Component
public class OrdersPaymentFeignImpl implements OrdersPaymentFeign {
    @Override
    public BaseResponse pageFindOrderPayment(OrderPaymentDTO orderPaymentDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse getOrderPaymentDetail(String orderPaymentId, String language) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse operationsAudit(String name, String orderPaymentId, boolean enabled, String remark) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse institutionPayment(PayOutDTO payOutDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse institutionAudit(String name, String orderPaymentId, boolean enabled, String remark) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse exportOrderPayment(OrderPaymentExportDTO orderPaymentDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse artificialPayOutAudit(String name, String orderPaymentId, boolean enabled, String remark) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }


}
