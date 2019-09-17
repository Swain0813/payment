package com.payment.permission.feign.institution.impl;

import com.payment.common.dto.ChargesTypeDTO;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.permission.feign.institution.ChargesFeign;
import org.springframework.stereotype.Component;

/**
 * @author shenxinran
 * @Date: 2019/1/28 09:21
 * @Description: 算费Feign熔断类
 */
@Component
public class ChargesFeignImpl implements ChargesFeign {

    /**
     * 分页查询所有算费
     *
     * @param chargesTypeDTO
     * @return
     */
    @Override
    public BaseResponse pageChargesCondition(ChargesTypeDTO chargesTypeDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 根据ID 查询
     *
     * @param id
     * @return
     */
    @Override
    public BaseResponse getChargesInfo(ChargesTypeDTO chargesTypeDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 新增算费
     *
     * @param chargesTypeDTO
     * @return
     */
    @Override
    public BaseResponse addChargesType(ChargesTypeDTO chargesTypeDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 更新算费
     *
     * @param chargesTypeDTO
     * @return
     */
    @Override
    public BaseResponse updateChargesType(ChargesTypeDTO chargesTypeDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 启用禁用算费
     *
     * @param
     * @return
     */
    @Override
    public BaseResponse banChargesType(ChargesTypeDTO chargesTypeDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }
}
