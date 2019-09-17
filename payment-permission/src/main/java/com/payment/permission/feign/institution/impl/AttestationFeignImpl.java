package com.payment.permission.feign.institution.impl;

import com.payment.common.dto.AttestationDTO;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.permission.feign.institution.AttestationFeign;
import org.springframework.stereotype.Component;

/**
 * @author shenxinran
 * @Date: 2019/2/18 18:24
 * @Description: 密钥管理Feign熔断器
 */
@Component
public class AttestationFeignImpl implements AttestationFeign {
    /**
     * 生成RSA公私钥
     *
     * @return
     */
    @Override
    public BaseResponse getRSA() {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 添加密钥
     *
     * @param attestationDTO
     * @return
     */
    @Override
    public BaseResponse addKey(AttestationDTO attestationDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 查询商户密钥列表
     *
     * @param attestationDTO
     * @return
     */
    @Override
    public BaseResponse pageKeyInfo(AttestationDTO attestationDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 查询所有的密钥，包括平台的私钥
     *
     * @param attestationDTO
     * @return
     */
    @Override
    public BaseResponse pageAllKeyInfo(AttestationDTO attestationDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 更新密钥
     *
     * @param attestationDTO
     * @return
     */
    @Override
    public BaseResponse updateKeyInfo(AttestationDTO attestationDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 启用禁用密钥
     *
     * @param attestationDTO
     * @return
     */
    @Override
    public BaseResponse banKeyInfo(AttestationDTO attestationDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }
}
