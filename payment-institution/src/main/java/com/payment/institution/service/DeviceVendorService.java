package com.payment.institution.service;

import com.payment.common.base.BaseService;
import com.payment.common.dto.DeviceVendorDTO;
import com.payment.common.entity.DeviceVendor;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * 设备厂商service
 */
public interface DeviceVendorService extends BaseService<DeviceVendor> {
    /**
     * 添加设备厂商信息
     *
     * @param deviceVendorDTO
     * @return
     */
    int addDeviceVendor(DeviceVendorDTO deviceVendorDTO);

    /**
     * 启用禁用设备厂商
     *
     * @param deviceVendorDTO
     * @return
     */
    int banDeviceVendor(DeviceVendorDTO deviceVendorDTO);

    /**
     * 查询设备厂商
     *
     * @param deviceVendorDTO
     * @return
     */
    PageInfo<DeviceVendor> pageDeviceVendor(DeviceVendorDTO deviceVendorDTO);

    /**
     * 修改设备厂商信息
     *
     * @param deviceVendorDTO
     * @return
     */
    int updateDeviceVendor(DeviceVendorDTO deviceVendorDTO);

    /**
     * 查询设备厂商类别
     *
     * @param
     * @return
     */
    List<DeviceVendor> queryVendorCategory();
}
