package com.payment.institution.service.impl;
import com.payment.common.base.BaseServiceImpl;
import com.payment.common.config.AuditorProvider;
import com.payment.common.dto.DeviceBindingDTO;
import com.payment.common.entity.DeviceBinding;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.EResultEnum;
import com.payment.common.utils.IDS;
import com.payment.common.vo.DeviceBindingVO;
import com.payment.common.vo.DeviceInfoVO;
import com.payment.institution.dao.DeviceBindingMapper;
import com.payment.institution.dao.DeviceInfoMapper;
import com.payment.institution.service.DeviceBindingService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author shenxinran
 * @Date: 2019/3/8 14:08
 * @Description: 设备绑定Service
 */
@Service
@Transactional
public class DeviceBindingServiceImpl extends BaseServiceImpl<DeviceBinding> implements DeviceBindingService {

    @Autowired
    private DeviceBindingMapper deviceBindingMapper;

    @Autowired
    private DeviceInfoMapper deviceInfoMapper;

    @Autowired
    private AuditorProvider auditorProvider;


    /**
     * 新增设备绑定信息
     *
     * @param deviceBindingDTO
     * @return
     */
    @Override
    public int addDeviceBinding(DeviceBindingDTO deviceBindingDTO) {
        //判断参数
        if (deviceBindingDTO.getInstitutionCode() == null
                || deviceBindingDTO.getUseType() == null
                || deviceBindingDTO.getInstitutionName() == null) {
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        String institutionCode = deviceBindingDTO.getInstitutionCode();
        ArrayList<DeviceBinding> bindings = new ArrayList<>();
        List<String> infoId = deviceBindingDTO.getInfoId();

        int mark = 0;
        for (String id : infoId) {
            //判断是否可以绑定
            DeviceInfoVO deviceInfoVO = deviceInfoMapper.selectModelAndVendorAndInfoById(id);
            if (deviceInfoVO == null) {
                throw new BusinessException(EResultEnum.DEVICE_NOT_AVAILABLE.getCode());
            }
            //修改设备信息为绑定
            mark += deviceInfoMapper.updateById(id, true, new Date(), deviceBindingDTO.getCreator());
            DeviceBinding deviceBinding = new DeviceBinding();
            deviceBinding.setInstitutionCode(institutionCode);
            deviceBinding.setInfoId(id);
            deviceBinding.setInstitutionName(deviceBindingDTO.getInstitutionName());
            deviceBinding.setVendorName(deviceInfoVO.getVendorName());
            deviceBinding.setModelName(deviceInfoVO.getModelName());
            deviceBinding.setInfoName(deviceInfoVO.getName());
            deviceBinding.setImei(deviceInfoVO.getImei());
            deviceBinding.setSn(deviceInfoVO.getSn());
            deviceBinding.setBindingTime(new Date());
            deviceBinding.setEnabled(true);
            deviceBinding.setOperator(deviceBindingDTO.getCreator());
            deviceBinding.setCreateTime(new Date());
            deviceBinding.setId(IDS.uuid2());
            deviceBinding.setUseType(deviceBindingDTO.getUseType());
            deviceBinding.setCreator(deviceBindingDTO.getCreator());
            deviceBinding.setRemark(deviceBindingDTO.getRemark());
            bindings.add(deviceBinding);
        }
        //判断是否将所有的设备信息修改为已绑定
        if (mark != infoId.size()) {
            throw new BusinessException(EResultEnum.DEVICE_BINDING_FAILED.getCode());
        }
        return deviceBindingMapper.insertList(bindings);
    }


    /**
     * 解绑设备
     *
     * @param deviceBindingDTO
     * @return
     */
    @Override
    public int banDeviceBinding(DeviceBindingDTO deviceBindingDTO) {
        DeviceBinding deviceBinding = new DeviceBinding();
        deviceBinding.setId(deviceBindingDTO.getId());
        DeviceBinding binding = deviceBindingMapper.selectOne(deviceBinding);
        binding.setEnabled(false);
        binding.setUpdateTime(new Date());
        binding.setModifier(deviceBindingDTO.getModifier());
        //修改设备为未绑定
        if (deviceInfoMapper.updateById(binding.getInfoId(), false, new Date(), deviceBindingDTO.getModifier()) == 0) {
            throw new BusinessException(EResultEnum.DEVICE_UNBIND_FAILED.getCode());
        }
        return deviceBindingMapper.updateByPrimaryKeySelective(binding);
    }

    /**
     * 查询设备绑定信息
     *
     * @param deviceBindingDTO
     * @return
     */
    @Override
    public PageInfo<DeviceBindingVO> pageDeviceBinding(DeviceBindingDTO deviceBindingDTO) {
        //设置语言
        deviceBindingDTO.setLanguage(auditorProvider.getLanguage());
        return new PageInfo<DeviceBindingVO>(deviceBindingMapper.pageDeviceBinding(deviceBindingDTO));
    }
}
