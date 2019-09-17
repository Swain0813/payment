package com.payment.institution.service;

import com.payment.common.base.BaseService;
import com.payment.common.dto.DeviceBindingDTO;
import com.payment.common.vo.DeviceBindingVO;
import com.payment.common.entity.DeviceBinding;
import com.github.pagehelper.PageInfo;

/**
 * @author shenxinran
 * @Date: 2019/3/8 13:53
 * @Description: 设备绑定Service
 */
public interface DeviceBindingService extends BaseService<DeviceBinding> {

    /**
     * 新增设备绑定信息
     *
     * @param deviceBindingDTO
     * @return
     */
    int addDeviceBinding(DeviceBindingDTO deviceBindingDTO);

    /**
     * 解绑设备
     *
     * @param deviceBindingDTO
     * @return
     */
    int banDeviceBinding(DeviceBindingDTO deviceBindingDTO);

    /**
     * 查询设备绑定信息
     *
     * @param deviceBindingDTO
     * @return
     */
    PageInfo<DeviceBindingVO> pageDeviceBinding(DeviceBindingDTO deviceBindingDTO);
}
