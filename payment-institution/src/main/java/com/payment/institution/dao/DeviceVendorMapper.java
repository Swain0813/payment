package com.payment.institution.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.dto.DeviceVendorDTO;
import com.payment.common.entity.DeviceVendor;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 设备厂商
 */

@Repository
public interface DeviceVendorMapper extends BaseMapper<DeviceVendor> {

    List<DeviceVendor> pageDeviceVendor(DeviceVendorDTO deviceVendorDTO);

    /**
     * 通过厂商id查询厂商个数
     *
     * @param vendorId
     * @return
     */
    int selectByVendorId(@Param("vendorId") String vendorId);

    /**
     * 查询类别
     *
     * @param
     * @return
     */
    List<DeviceVendor> queryVendorCategory();
}
