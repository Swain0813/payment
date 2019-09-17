package com.payment.permission.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.dto.DeviceBindingDTO;
import com.payment.common.entity.DeviceBinding;
import com.payment.common.vo.DeviceBindingVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 设备绑定
 */
@Repository
public interface DeviceBindingMapper extends BaseMapper<DeviceBinding> {

    /**
     * 查询当前机构code和设备号在设备绑定是否绑定
     * @param institutionCode
     * @param imei
     * @return
     */
    @Select("select count(1) from device_binding where institution_code = #{institutionCode} and imei = #{imei} and enabled = 1")
    int selectCountByCodeAndImei(@Param("institutionCode") String institutionCode,@Param("imei") String imei);
}
