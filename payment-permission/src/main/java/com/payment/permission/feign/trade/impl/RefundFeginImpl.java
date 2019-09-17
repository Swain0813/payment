package com.payment.permission.feign.trade.impl;

import com.payment.common.dto.RefundDTO;
import com.payment.common.dto.SearchOrderDTO;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.permission.feign.trade.RefundFegin;
import org.springframework.stereotype.Component;

/**
 * @description:
 * @author: YangXu
 * @create: 2019-03-13 15:07
 **/
@Component
public class RefundFeginImpl implements RefundFegin {
    @Override
    public BaseResponse artificialRefund(String refundOrderId,Boolean enabled,String remark) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse pageRefundOrder(SearchOrderDTO searchOrderDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse exportRefundOrder(SearchOrderDTO searchOrderDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse refundOrderSys(RefundDTO refundDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

}
