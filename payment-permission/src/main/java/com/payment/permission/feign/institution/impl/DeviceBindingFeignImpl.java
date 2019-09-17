package com.payment.permission.feign.institution.impl;

import com.payment.common.dto.DeviceBindingDTO;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.permission.feign.institution.DeviceBindingFeign;
import org.springframework.stereotype.Component;

/**
 * 设备绑定Feign短路器
 */
@Component
public class DeviceBindingFeignImpl implements DeviceBindingFeign {
    @Override
    public BaseResponse addDeviceBinding(DeviceBindingDTO deviceBindingDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse banDeviceBinding(DeviceBindingDTO deviceBindingDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse pageDeviceBinding(DeviceBindingDTO deviceBindingDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }
}
