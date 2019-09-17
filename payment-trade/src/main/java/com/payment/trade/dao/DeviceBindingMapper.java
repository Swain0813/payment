package com.payment.trade.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.entity.DeviceBinding;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * 设备绑定
 */
@Repository
public interface DeviceBindingMapper extends BaseMapper<DeviceBinding> {


    /**
     * 根据机构code和imei编号查询设备信息
     *
     * @param institutionCode 机构code
     * @param imei            imei编号
     * @return 机构绑定实体
     */
    DeviceBinding selectByInstitutionCodeAndImei(@Param("institutionCode") String institutionCode, @Param("imei") String imei);
}
