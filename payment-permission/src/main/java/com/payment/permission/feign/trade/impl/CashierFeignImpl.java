package com.payment.permission.feign.trade.impl;

import com.payment.common.exception.BusinessException;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.permission.feign.trade.CashierFeign;
import org.springframework.stereotype.Component;

/**
 * @author shenxinran
 * @Date: 2019/4/1 22:32
 * @Description:
 */
@Component
public class CashierFeignImpl implements CashierFeign {


    @Override
    public BaseResponse cashier(String orderId) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }
}
