package com.payment.trade.feign.Impl;

import com.payment.common.exception.BusinessException;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.trade.feign.MessageFeign;
import com.payment.trade.feign.SysUserFeign;
import org.springframework.stereotype.Component;

/**
 * @description:
 * @author: YangXu
 * @create: 2019-04-12 15:12
 **/
@Component
public class SysUserFeignImpl  implements SysUserFeign {

    @Override
    public BaseResponse checkPassword(String oldPassword, String password) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }
}
