package com.payment.permission.feign.institution.impl;

import com.payment.common.dto.DeviceModelDTO;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.permission.feign.institution.DeviceModelFeign;
import org.springframework.stereotype.Component;

/**
 * @author shenxinran
 * @Date: 2019/3/6 14:24
 * @Description: 设备型号管理 Feign
 */
@Component
public class DeviceModelFeignImpl implements DeviceModelFeign {
    /**
     * 添加设备型号
     *
     * @param deviceModelDTO
     * @return
     */
    @Override
    public BaseResponse addDeviceModel(DeviceModelDTO deviceModelDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 启用警用禁用设备型号
     *
     * @param deviceModelDTO
     * @return
     */
    @Override
    public BaseResponse banDeviceModel(DeviceModelDTO deviceModelDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 更新设备型号
     *
     * @param deviceModelDTO
     * @return
     */
    @Override
    public BaseResponse updateDeviceModel(DeviceModelDTO deviceModelDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 查询设备型号
     *
     * @param deviceModelDTO
     * @return
     */
    @Override
    public BaseResponse pageDeviceModel(DeviceModelDTO deviceModelDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 查询设备类别
     *
     * @return
     */
    @Override
    public BaseResponse queryModelCategory() {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }
}
